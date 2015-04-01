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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.wso2.carbon.metrics.common.MetricsConfigException;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.data.service.dao.MetricDataProcessor;
import org.wso2.carbon.metrics.data.service.dao.ReporterDAO;
import org.wso2.carbon.utils.CarbonUtils;

public class MetricsDataService extends AbstractAdmin implements Lifecycle {

    private static final Logger logger = LoggerFactory.getLogger(MetricsDataService.class);

    private ReporterDAO reporterDAO;

    private final Pattern fromPattern = Pattern.compile("(\\-?\\d+)([hdm])");

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
        // final String JDBC_REPORTING_SOURCE = "Reporting.JDBC.Source";

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

        if (logger.isInfoEnabled()) {
            logger.info(String.format("Creating Metrics Data Service with data source '%s'", dataSourceName));
        }

        reporterDAO = new ReporterDAO(dataSource);
    }

    private long getStartTime(String from) {
        if (from == null || from.isEmpty()) {
            return -1;
        }

        long currentTimeMillis = System.currentTimeMillis();

        Matcher matcher = fromPattern.matcher(from);

        long startTime = -1;

        if (matcher.find()) {
            int count = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            if ("m".equals(unit)) {
                startTime = currentTimeMillis + (count * 1000 * 60);
            } else if ("h".equals(unit)) {
                startTime = currentTimeMillis + (count * 1000 * 60 * 60);
            } else if ("d".equals(unit)) {
                startTime = currentTimeMillis + (count * 1000 * 60 * 60 * 24);
            }
        } else if (from.matches("\\d+")) {
            startTime = Integer.parseInt(from);
        }

        return startTime;
    }

    public List<String> getAllSources() {
        return reporterDAO.queryAllSources();
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

    /**
     * Convert value
     */
    private interface ValueConverter {
        BigDecimal convert(BigDecimal value);
    }

    private static final ValueConverter MEMORY_VALUE_CONVERTER = new ValueConverter() {

        private final BigDecimal BYTES_IN_ONE_MEGABYTE = BigDecimal.valueOf(1024 * 1024);

        @Override
        public BigDecimal convert(BigDecimal value) {
            return value.divide(BYTES_IN_ONE_MEGABYTE, 2, RoundingMode.CEILING);
        }
    };

    private static final ValueConverter PERCENTAGE_VALUE_CONVERTER = new ValueConverter() {

        private final BigDecimal HUNDRED = BigDecimal.valueOf(100);

        @Override
        public BigDecimal convert(BigDecimal value) {
            return value.multiply(HUNDRED).setScale(2, RoundingMode.CEILING);
        }
    };

    private static final ValueConverter DUMB_VALUE_CONVERTER = new ValueConverter() {

        @Override
        public BigDecimal convert(BigDecimal value) {
            return value;
        }
    };

    private MetricData getResults(List<String> metrics, List<String> displayNames,
            List<ValueConverter> valueConverters, String source, long startTime, long endTime) {
        String[] names = metrics.toArray(new String[metrics.size()]);
        JVMMetricDataProcessor processor = new JVMMetricDataProcessor(names,
                displayNames.toArray(new String[displayNames.size()]),
                valueConverters.toArray(new ValueConverter[valueConverters.size()]));
        reporterDAO.queryMetrics(MetricType.GAUGE, names, MetricAttribute.VALUE, source, startTime, endTime, processor);
        return processor.getResult();
    }

    // Method overloading doesn't support in Axis2.
    public MetricData findLastJMXMemoryMetrics(String source, String from) {
        long startTime = getStartTime(from);
        if (startTime == -1) {
            return null;
        }
        long endTime = System.currentTimeMillis();
        return findJMXMemoryMetricsByTimePeriod(source, startTime, endTime);
    }

    public MetricData findJMXMemoryMetricsByTimePeriod(String source, long startTime, long endTime) {
        List<String> metrics = new ArrayList<String>();
        List<String> displayNames = new ArrayList<String>();
        List<ValueConverter> valueConverters = new ArrayList<ValueConverter>();
        addMemoryMetrics(metrics, displayNames, valueConverters, "heap", "Heap");
        addMemoryMetrics(metrics, displayNames, valueConverters, "non-heap", "Non-Heap");
        return getResults(metrics, displayNames, valueConverters, source, startTime, endTime);
    }

    private void addMemoryMetrics(List<String> metrics, List<String> displayNames,
            List<ValueConverter> valueConverters, String type, String displayType) {
        metrics.add(String.format("jvm.memory.%s.init", type));
        metrics.add(String.format("jvm.memory.%s.used", type));
        metrics.add(String.format("jvm.memory.%s.committed", type));
        metrics.add(String.format("jvm.memory.%s.max", type));

        valueConverters.add(MEMORY_VALUE_CONVERTER);
        valueConverters.add(MEMORY_VALUE_CONVERTER);
        valueConverters.add(MEMORY_VALUE_CONVERTER);
        valueConverters.add(MEMORY_VALUE_CONVERTER);

        displayNames.add(String.format("%s Init", displayType));
        displayNames.add(String.format("%s Used", displayType));
        displayNames.add(String.format("%s Committed", displayType));
        displayNames.add(String.format("%s Max", displayType));
    }

    public MetricData findLastJMXCPULoadMetrics(String source, String from) {
        long startTime = getStartTime(from);
        if (startTime == -1) {
            return null;
        }
        long endTime = System.currentTimeMillis();
        return findJMXCPULoadMetricsByTimePeriod(source, startTime, endTime);
    }

    public MetricData findJMXCPULoadMetricsByTimePeriod(String source, long startTime, long endTime) {
        List<String> metrics = new ArrayList<String>();
        List<String> displayNames = new ArrayList<String>();
        List<ValueConverter> valueConverters = new ArrayList<ValueConverter>();

        metrics.add("jvm.os.cpu.load.process");
        metrics.add("jvm.os.cpu.load.system");

        displayNames.add("Process CPU Load");
        displayNames.add("System CPU Load");

        valueConverters.add(PERCENTAGE_VALUE_CONVERTER);
        valueConverters.add(PERCENTAGE_VALUE_CONVERTER);

        return getResults(metrics, displayNames, valueConverters, source, startTime, endTime);
    }

    public MetricData findLastJMXLoadAverageMetrics(String source, String from) {
        long startTime = getStartTime(from);
        if (startTime == -1) {
            return null;
        }
        long endTime = System.currentTimeMillis();
        return findJMXLoadAverageMetricsByTimePeriod(source, startTime, endTime);
    }

    public MetricData findJMXLoadAverageMetricsByTimePeriod(String source, long startTime, long endTime) {
        List<String> metrics = new ArrayList<String>();
        List<String> displayNames = new ArrayList<String>();
        List<ValueConverter> valueConverters = new ArrayList<ValueConverter>();

        metrics.add("jvm.os.system.load.average");

        displayNames.add("System Load Average");

        valueConverters.add(DUMB_VALUE_CONVERTER);

        return getResults(metrics, displayNames, valueConverters, source, startTime, endTime);
    }
}
