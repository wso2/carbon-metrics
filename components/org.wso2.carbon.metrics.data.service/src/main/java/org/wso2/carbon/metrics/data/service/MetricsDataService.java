/*
 * Copyright 2015 WSO2 Inc. (http://wso2.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.metrics.data.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.service.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.metrics.common.DefaultSourceValueProvider;
import org.wso2.carbon.metrics.common.MetricsConfigException;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.data.common.Metric;
import org.wso2.carbon.metrics.data.common.MetricAttribute;
import org.wso2.carbon.metrics.data.common.MetricDataFormat;
import org.wso2.carbon.metrics.data.common.MetricList;
import org.wso2.carbon.metrics.data.common.MetricType;
import org.wso2.carbon.metrics.data.service.dao.MetricDataProcessor;
import org.wso2.carbon.metrics.data.service.dao.ReporterDAO;
import org.wso2.carbon.metrics.data.service.dao.converter.DumbConverter;
import org.wso2.carbon.metrics.data.service.dao.converter.MemoryConverter;
import org.wso2.carbon.metrics.data.service.dao.converter.PercentageConverter;
import org.wso2.carbon.metrics.data.service.dao.converter.ValueConverter;
import org.wso2.carbon.utils.CarbonUtils;

public class MetricsDataService extends AbstractAdmin implements Lifecycle {

    private static final Logger logger = LoggerFactory.getLogger(MetricsDataService.class);

    private ReporterDAO reporterDAO;

    private final Pattern fromPattern = Pattern.compile("(\\-?\\d+)([hdm])");

    private String currentJDBCReportingSource;

    private static final ValueConverter MEMORY_VALUE_CONVERTER = new MemoryConverter();

    private static final ValueConverter PERCENTAGE_VALUE_CONVERTER = new PercentageConverter();

    private static final ValueConverter DUMB_VALUE_CONVERTER = new DumbConverter();

    public MetricsDataService() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis2.service.Lifecycle#init(org.apache.axis2.context.ServiceContext)
     */
    @Override
    public void init(ServiceContext serviceContext) throws AxisFault {
        MetricsConfiguration configuration = new MetricsConfiguration();
        String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "metrics.xml";
        try {
            configuration.load(filePath);
        } catch (MetricsConfigException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error reading configuration from " + filePath, e);
            }
        }

        init(configuration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis2.service.Lifecycle#destroy(org.apache.axis2.context.ServiceContext)
     */
    @Override
    public void destroy(ServiceContext serviceContext) {
    }

    void init(MetricsConfiguration configuration) {
        final String JDBC_REPORTING_DATASOURCE_NAME = "Reporting.JDBC.DataSourceName";
        final String JDBC_REPORTING_SOURCE = "Reporting.JDBC.Source";

        String dataSourceName = configuration.getFirstProperty(JDBC_REPORTING_DATASOURCE_NAME);

        if (dataSourceName == null || dataSourceName.trim().length() == 0) {
            String msg = "Data Source Name is not specified for Metrics Data Service";
            if (logger.isWarnEnabled()) {
                logger.warn(msg);
            }
            throw new IllegalStateException(msg);
        }

        DataSource dataSource = null;
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            String msg = String.format(
                    "Error when looking up the Data Source: '%s'. Cannot instantiate the Metrics Data Service",
                    dataSourceName);
            if (logger.isWarnEnabled()) {
                logger.warn(msg);
            }
            throw new IllegalStateException(msg);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Creating Metrics Data Service with data source '%s'", dataSourceName));
        }

        reporterDAO = new ReporterDAO(dataSource);

        currentJDBCReportingSource = configuration.getFirstProperty(JDBC_REPORTING_SOURCE,
                new DefaultSourceValueProvider());
    }

    private long getStartTime(String from) {
        if (from == null || from.isEmpty()) {
            return -1;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Get Start Time. From Value: %s", from));
        }

        long currentTimeMillis = System.currentTimeMillis();

        Matcher matcher = fromPattern.matcher(from);

        long startTime = -1;

        if (matcher.find()) {
            long count = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            if ("m".equals(unit)) {
                startTime = currentTimeMillis + (count * 1000 * 60);
            } else if ("h".equals(unit)) {
                startTime = currentTimeMillis + (count * 1000 * 60 * 60);
            } else if ("d".equals(unit)) {
                startTime = currentTimeMillis + (count * 1000 * 60 * 60 * 24);
            }
        } else if (from.matches("\\d+")) {
            startTime = Long.parseLong(from);
        }

        return startTime;
    }

    public String[] getAllSources() {
        List<String> sourcesList;
        Set<String> sources = reporterDAO.queryAllSources();
        if (sources == null) {
            sourcesList = new ArrayList<String>(1);
            sourcesList.add(currentJDBCReportingSource);
        } else {
            // Remove current source from set
            sources.remove(currentJDBCReportingSource);
            // Add the current source to top
            // This will make sure that the current source is selected by default
            sourcesList = new ArrayList<String>(sources.size());
            sourcesList.add(currentJDBCReportingSource);
            sourcesList.addAll(sources);
        }
        return sourcesList.toArray(new String[sourcesList.size()]);
    }

    private class JVMMetricDataProcessor implements MetricDataProcessor<MetricData> {

        private final Map<Long, BigDecimal[]> dataMap = new HashMap<Long, BigDecimal[]>();

        /**
         * Timestamp data must be kept in order
         */
        private final List<BigDecimal[]> orderedList = new ArrayList<BigDecimal[]>();

        private final Map<MetricGroup, MetricGroup> metricGroupMap;

        private final String[] dataTypes;

        private final String[] displayNames;

        private JVMMetricDataProcessor(Map<MetricGroup, MetricGroup> metricGroupMap) {
            this.metricGroupMap = metricGroupMap;
            dataTypes = new String[metricGroupMap.size() + 1];
            displayNames = new String[metricGroupMap.size() + 1];

            // Initialize types and display names
            dataTypes[0] = "T";
            displayNames[0] = "Time";
            for (MetricGroup metricGroup : metricGroupMap.values()) {
                int index = metricGroup.index;
                dataTypes[index + 1] = "N";
                displayNames[index + 1] = metricGroup.displayName;
            }
        }

        @Override
        public void process(String source, long timestamp, MetricType metricType, String metricName,
                MetricAttribute metricAttribute, BigDecimal value) {
            BigDecimal[] data = dataMap.get(timestamp);
            if (data == null) {
                data = new BigDecimal[metricGroupMap.size() + 1];
                dataMap.put(timestamp, data);
                orderedList.add(data);
                // First element is the timestamp
                data[0] = BigDecimal.valueOf(timestamp);
            }

            MetricGroup metricGroupKey = new MetricGroup(metricType, metricName, metricAttribute);

            MetricGroup metricGroup = metricGroupMap.get(metricGroupKey);

            int index = metricGroup.index;

            data[index + 1] = metricGroup.valueConverter.convert(value);
        }

        @Override
        public MetricData getResult() {
            if (logger.isDebugEnabled()) {
                logger.debug(String
                        .format("Metrics Search Results. Display Names: %s, Data Types: %s, Columns %d, Rows: %d, Total Data Points: %d",
                                Arrays.asList(displayNames), Arrays.asList(dataTypes), displayNames.length,
                                orderedList.size(), displayNames.length * orderedList.size()));
            }

            return new MetricData(new Metadata(displayNames, dataTypes), orderedList.toArray(new BigDecimal[orderedList
                    .size()][]));
        }
    }

    private class MetricGroup {

        private final MetricType metricType;
        private final String metricName;
        private final MetricAttribute metricAttribute;

        private int index;
        private String displayName;
        private ValueConverter valueConverter;

        public MetricGroup(MetricType metricType, String metricName, MetricAttribute metricAttribute) {
            super();
            this.metricType = metricType;
            this.metricName = metricName;
            this.metricAttribute = metricAttribute;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((metricAttribute == null) ? 0 : metricAttribute.hashCode());
            result = prime * result + ((metricType == null) ? 0 : metricType.hashCode());
            result = prime * result + ((metricName == null) ? 0 : metricName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof MetricGroup)) {
                return false;
            }
            MetricGroup other = (MetricGroup) obj;
            if (metricAttribute != other.metricAttribute) {
                return false;
            }
            if (metricType != other.metricType) {
                return false;
            }
            if (metricName == null) {
                if (other.metricName != null) {
                    return false;
                }
            } else if (!metricName.equals(other.metricName)) {
                return false;
            }
            return true;
        }
    }

    public MetricData findLastMetrics(MetricList metrics, String source, String from) {
        long startTime = getStartTime(from);
        if (startTime == -1) {
            return null;
        }
        long endTime = System.currentTimeMillis();
        return findMetricsByTimePeriod(metrics, source, startTime, endTime);
    }

    public MetricData findMetricsByTimePeriod(MetricList metrics, String source, long startTime, long endTime) {
        Metric[] list = null;
        if (metrics == null || (list = metrics.getMetric()) == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Metric List is not available. Returning null");
            }
            // No metrics
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format(
                    "Find Metrics by time period. Metric List Count: %d, Source: %s, Start Time: %d, End Time: %d",
                    list.length, source, startTime, endTime));
        }

        Map<MetricGroup, MetricGroup> metricGroupMap = new HashMap<MetricGroup, MetricGroup>();
        Map<String, List<MetricAttribute>> nameGroupMap = new HashMap<String, List<MetricAttribute>>();
        Map<MetricAttribute, List<String>> attributeGroupMap = new HashMap<MetricAttribute, List<String>>();
        int index = 0;
        for (int i = 0; i < list.length; i++) {
            Metric metric = list[i];
            String metricName = metric.getName();
            String displayName = metric.getDisplayName();
            MetricType metricType = MetricType.valueOf(metric.getType());
            MetricAttribute metricAttribute = MetricAttribute.valueOf(metric.getAttr());
            MetricDataFormat metricDataFormat = null;
            if (metric.getFormat() != null) {
                metricDataFormat = MetricDataFormat.valueOf(metric.getFormat());
            }

            List<MetricAttribute> attributes = nameGroupMap.get(metricName);
            if (attributes == null) {
                attributes = new ArrayList<MetricAttribute>();
                nameGroupMap.put(metricName, attributes);
            }
            List<String> names = attributeGroupMap.get(metricAttribute);
            if (names == null) {
                names = new ArrayList<String>();
                attributeGroupMap.put(metricAttribute, names);
            }
            attributes.add(metricAttribute);
            names.add(metricName);

            ValueConverter valueConverter;

            if (metricDataFormat != null) {
                switch (metricDataFormat) {
                case P:
                    valueConverter = PERCENTAGE_VALUE_CONVERTER;
                    break;
                case B:
                    valueConverter = MEMORY_VALUE_CONVERTER;
                    break;
                default:
                    valueConverter = DUMB_VALUE_CONVERTER;
                    break;
                }
            } else {
                valueConverter = DUMB_VALUE_CONVERTER;
            }

            MetricGroup metricGroup = new MetricGroup(metricType, metricName, metricAttribute);
            if (!metricGroupMap.containsKey(metricGroup)) {
                // Put only if there is no existing metric group. Important for determining correct index
                metricGroupMap.put(metricGroup, metricGroup);
                metricGroup.index = index++;
                metricGroup.displayName = displayName;
                metricGroup.valueConverter = valueConverter;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Metric Group Map Size: %d", metricGroupMap.size()));
        }

        JVMMetricDataProcessor processor = new JVMMetricDataProcessor(metricGroupMap);

        Set<MetricGroup> processedMetricGroups = new HashSet<MetricGroup>();

        for (MetricGroup metricGroup : metricGroupMap.values()) {
            if (!processedMetricGroups.contains(metricGroup)) {
                MetricType metricType = metricGroup.metricType;
                List<String> names = attributeGroupMap.get(metricGroup.metricAttribute);
                List<MetricAttribute> attributes = nameGroupMap.get(metricGroup.metricName);

                List<String> metricNames;
                List<MetricAttribute> metricAttributes;

                if (names.size() > attributes.size()) {
                    metricNames = names;
                    metricAttributes = new ArrayList<MetricAttribute>(1);
                    metricAttributes.add(metricGroup.metricAttribute);
                } else {
                    metricAttributes = attributes;
                    metricNames = new ArrayList<String>(1);
                    metricNames.add(metricGroup.metricName);
                }

                reporterDAO.queryMetrics(metricType, metricNames, metricAttributes, source, startTime, endTime,
                        processor);

                for (String metricName : metricNames) {
                    for (MetricAttribute metricAttribute : metricAttributes) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.format("Processed. Metric Type: %s, Name: %s, Attribute: %s",
                                    metricType, metricName, metricAttribute));
                        }
                        processedMetricGroups.add(new MetricGroup(metricType, metricName, metricAttribute));
                    }
                }

            }
        }
        return processor.getResult();
    }

}
