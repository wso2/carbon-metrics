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
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.metrics.common.MetricsConfigException;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.data.service.dao.ReporterDAO;
import org.wso2.carbon.utils.CarbonUtils;

public class MetricsDataService extends AbstractAdmin {

    private static final Logger logger = LoggerFactory.getLogger(MetricsDataService.class);

    private final ReporterDAO reporterDAO;

    public MetricsDataService() {
        // TODO:
        // logger.error("Extend " + AbstractAdmin.class);

        final String JDBC_REPORTING_DATASOURCE_NAME = "Reporting.JDBC.DataSourceName";
        MetricsConfiguration configuration = new MetricsConfiguration();
        String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "metrics.xml";
        try {
            configuration.load(filePath);
        } catch (MetricsConfigException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error reading configuration from " + filePath, e);
            }
        }

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

}
