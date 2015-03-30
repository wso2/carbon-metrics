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
import java.util.List;
import java.util.Map;

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
        // TODO Auto-generated method stub

    }

    void init(MetricsConfiguration configuration) {
        final String JDBC_REPORTING_DATASOURCE_NAME = "Reporting.JDBC.DataSourceName";
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

    private MetricType toMetricType(String metricType) {
        metricType = metricType.toUpperCase();
        if ("GAUGE".equals(metricType) || "COUNTER".equals(metricType) || "METER".equals(metricType)
                || "HISTOGRAM".equals(metricType) || "TIMER".equals(metricType)) {
            return MetricType.valueOf(metricType);
        }
        throw new IllegalArgumentException("Invalid Metric Type: " + metricType);
    }

    public List<MetricNameSearchResult> searchMetricNames(String metricType, String searchName) {
        return reporterDAO.searchMetricNames(toMetricType(metricType), searchName);
    }

    private class JVMMetricDataProcessor implements MetricDataProcessor<MetricData> {

        private final Map<Long, BigDecimal[]> dataMap = new HashMap<Long, BigDecimal[]>();

        private final String[] names;

        private final String[] displayNames;

        private JVMMetricDataProcessor(String[] names, String[] displayNames) {
            this.names = names;
            this.displayNames = displayNames;
        }

        @Override
        public void process(String source, String name, long timestamp, BigDecimal value) {
            // BigDecimal[][] data = new BigDecimal[2][1];
            // data[0] = new BigDecimal[] { BigDecimal.ZERO };
            // data[1] = new BigDecimal[] { BigDecimal.ONE };
            BigDecimal[] data = dataMap.get(timestamp);
            if (data == null) {
                data = new BigDecimal[names.length + 1];
                dataMap.put(timestamp, data);
                // First element is the timestamp
                data[0] = BigDecimal.valueOf(timestamp);
            }

            int index = indexOf(name);

            if (index >= 0) {
                data[index + 1] = value;
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
            names.add("Timestamp");
            names.addAll(Arrays.asList(displayNames));

            return new MetricData(new Metadata(names.toArray(new String[names.size()]), types), dataMap.values()
                    .toArray(new BigDecimal[dataMap.size()][]));
        }

    }

    public MetricData searchJMXMemory() {
        List<String> metrics = new ArrayList<String>();
        List<String> displayNames = new ArrayList<String>();
        addMemoryMetrics(metrics, displayNames, "heap", "Heap");
        addMemoryMetrics(metrics, displayNames, "non-heap", "Non-Heap");
        String[] names = metrics.toArray(new String[metrics.size()]);

        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        long startTime = currentTimeSeconds - (7 * 24 * 60 * 60);
        long endTime = currentTimeSeconds;

        JVMMetricDataProcessor processor = new JVMMetricDataProcessor(names,
                displayNames.toArray(new String[displayNames.size()]));
        reporterDAO.queryMetrics(MetricType.GAUGE, names, MetricAttribute.VALUE, startTime, endTime, processor);
        return processor.getResult();
    }

    private void addMemoryMetrics(List<String> metrics, List<String> displayNames, String type, String displayType) {
        metrics.add(String.format("jvm.memory.%s.init", type));
        metrics.add(String.format("jvm.memory.%s.used", type));
        metrics.add(String.format("jvm.memory.%s.committed", type));
        metrics.add(String.format("jvm.memory.%s.max", type));

        displayNames.add(String.format("%s Init", displayType));
        displayNames.add(String.format("%s Used", displayType));
        displayNames.add(String.format("%s Committed", displayType));
        displayNames.add(String.format("%s Max", displayType));
    }

}
