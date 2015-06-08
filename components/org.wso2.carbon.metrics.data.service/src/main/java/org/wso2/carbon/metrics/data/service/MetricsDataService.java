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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<String> getAllSources() {
        List<String> sources = reporterDAO.queryAllSources();
        if (sources == null) {
            sources = new ArrayList<String>();
        }
        if (sources.isEmpty()) {
            sources.add(currentJDBCReportingSource);
        }
        return sources;
    }

    private class JVMMetricDataProcessor implements MetricDataProcessor<MetricData> {

        private final Map<Long, BigDecimal[]> dataMap = new HashMap<Long, BigDecimal[]>();

        /**
         * Timestamp data must be kept in order
         */
        private final List<BigDecimal[]> orderedList = new ArrayList<BigDecimal[]>();

        private final String[] names;

        private final String[] displayNames;

        private final ValueConverter[] valueConverters;

        private JVMMetricDataProcessor(String[] names, String[] displayNames, ValueConverter[] valueConverters) {
            this.names = names;
            this.displayNames = displayNames;
            this.valueConverters = valueConverters;
        }

        @Override
        public void process(String source, String name, long timestamp, BigDecimal value) {
            BigDecimal[] data = dataMap.get(timestamp);
            if (data == null) {
                data = new BigDecimal[names.length + 1];
                dataMap.put(timestamp, data);
                orderedList.add(data);
                // First element is the timestamp
                data[0] = BigDecimal.valueOf(timestamp);
            }

            int index = indexOf(name);

            if (index >= 0) {
                data[index + 1] = valueConverters[index].convert(value);
            }
        }

        private int indexOf(String name) {
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(name)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public MetricData getResult() {
            String[] types = new String[displayNames.length + 1];
            types[0] = "T";
            for (int i = 1; i < types.length; i++) {
                types[i] = "N";
            }

            List<String> names = new ArrayList<String>(displayNames.length + 1);
            names.add("Time");
            names.addAll(Arrays.asList(displayNames));

            return new MetricData(new Metadata(names.toArray(new String[names.size()]), types),
                    orderedList.toArray(new BigDecimal[orderedList.size()][]));
        }
    }

    private static final ValueConverter MEMORY_VALUE_CONVERTER = new MemoryConverter();

    private static final ValueConverter PERCENTAGE_VALUE_CONVERTER = new PercentageConverter();

    private static final ValueConverter DUMB_VALUE_CONVERTER = new DumbConverter();

    private final class MetricGroupKey {

        private MetricType metricType;
        private MetricAttribute metricAttribute;

        public MetricGroupKey(MetricType metricType, MetricAttribute metricAttribute) {
            super();
            this.metricType = metricType;
            this.metricAttribute = metricAttribute;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((metricAttribute == null) ? 0 : metricAttribute.hashCode());
            result = prime * result + ((metricType == null) ? 0 : metricType.hashCode());
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
            if (!(obj instanceof MetricGroupKey)) {
                return false;
            }
            MetricGroupKey other = (MetricGroupKey) obj;
            if (metricAttribute != other.metricAttribute) {
                return false;
            }
            if (metricType != other.metricType) {
                return false;
            }
            return true;
        }
    }

    private class MetricGroup {
        private MetricType metricType;
        private MetricAttribute metricAttribute;
        private List<String> names = new ArrayList<String>();
        private List<String> displayNames = new ArrayList<String>();
        private List<ValueConverter> valueConverters = new ArrayList<ValueConverter>();
    }

    private MetricData getResults(Collection<MetricGroup> metricGroups, String source, long startTime, long endTime) {
        List<String> names = new ArrayList<String>();
        List<String> displayNames = new ArrayList<String>();
        List<ValueConverter> valueConverters = new ArrayList<ValueConverter>();

        for (MetricGroup metricGroup : metricGroups) {
            names.addAll(metricGroup.names);
            displayNames.addAll(metricGroup.displayNames);
            valueConverters.addAll(metricGroup.valueConverters);
        }

        JVMMetricDataProcessor processor = new JVMMetricDataProcessor(names.toArray(new String[names.size()]),
                displayNames.toArray(new String[displayNames.size()]),
                valueConverters.toArray(new ValueConverter[valueConverters.size()]));

        for (MetricGroup metricGroup : metricGroups) {
            reporterDAO.queryMetrics(metricGroup.metricType, metricGroup.names, metricGroup.metricAttribute, source,
                    startTime, endTime, processor);
        }
        return processor.getResult();
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
        Collection<MetricGroup> metricGroups = getMetricGroups(metrics);
        if (metricGroups == null) {
            return null;
        }
        return getResults(metricGroups, source, startTime, endTime);
    }

    private Collection<MetricGroup> getMetricGroups(MetricList metrics) {
        Metric[] list = null;
        if (metrics == null || (list = metrics.getMetric()) == null) {
            // No metrics
            return null;
        }
        Map<MetricGroupKey, MetricGroup> map = new HashMap<MetricGroupKey, MetricGroup>();
        for (int i = 0; i < list.length; i++) {
            Metric metric = list[i];
            MetricType metricType = MetricType.valueOf(metric.getType());
            MetricAttribute metricAttribute = MetricAttribute.valueOf(metric.getAttr());
            MetricDataFormat metricDataFormat = null;
            if (metric.getFormat() != null) {
                metricDataFormat = MetricDataFormat.valueOf(metric.getFormat());
            }
            MetricGroupKey metricGroupKey = new MetricGroupKey(metricType, metricAttribute);
            MetricGroup metricGroup = map.get(metricGroupKey);
            if (metricGroup == null) {
                metricGroup = new MetricGroup();
                map.put(metricGroupKey, metricGroup);
            }
            metricGroup.metricType = metricType;
            metricGroup.metricAttribute = metricAttribute;
            // Metric name should be unique!
            if (metricGroup.names.contains(metric.getName())) {
                continue;
            }
            metricGroup.names.add(metric.getName());
            metricGroup.displayNames.add(metric.getDisplayName());
            if (metricDataFormat != null) {
                switch (metricDataFormat) {
                case P:
                    metricGroup.valueConverters.add(PERCENTAGE_VALUE_CONVERTER);
                    break;
                case B:
                    metricGroup.valueConverters.add(MEMORY_VALUE_CONVERTER);
                    break;
                default:
                    metricGroup.valueConverters.add(DUMB_VALUE_CONVERTER);
                    break;
                }
            } else {
                metricGroup.valueConverters.add(DUMB_VALUE_CONVERTER);
            }
        }

        return map.values();
    }

}
