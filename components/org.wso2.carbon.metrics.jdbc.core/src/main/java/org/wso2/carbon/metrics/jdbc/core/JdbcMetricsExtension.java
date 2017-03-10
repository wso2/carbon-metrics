/*
 * Copyright 2016 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.jdbc.core;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.spi.MetricsExtension;
import org.wso2.carbon.metrics.jdbc.core.config.model.JdbcReporterConfig;
import org.wso2.carbon.metrics.jdbc.core.config.model.MetricsConfig;

import java.util.Arrays;
import java.util.Set;

/**
 * * Metrics Extension to support JDBC Reporter
 */
@Component(
        name = "org.wso2.carbon.metrics.jdbc.core.JdbcMetricsExtension",
        service = MetricsExtension.class,
        immediate = true
)
public class JdbcMetricsExtension implements MetricsExtension {

    private static final Logger logger = LoggerFactory.getLogger(JdbcMetricsExtension.class);

    private String[] names;

    /**
     * Add JDBC Reporters
     */
    @Override
    public void activate(ConfigProvider configProvider, MetricService metricService,
                         MetricManagementService metricManagementService) {
        MetricsConfig metricsConfig;
        try {
            metricsConfig = configProvider.getConfigurationObject(MetricsConfig.class);
        } catch (CarbonConfigurationException e) {
            logger.error("Error loading Metrics Configuration", e);
            metricsConfig = new MetricsConfig();
        }
        Set<JdbcReporterConfig> jdbcReporterConfigs = metricsConfig.getReporting().getJdbc();
        if (jdbcReporterConfigs != null) {
            jdbcReporterConfigs.forEach(reporterBuilder -> {
                        try {
                            metricManagementService.addReporter(reporterBuilder);
                        } catch (ReporterBuildException e) {
                            logger.warn("JDBC Reporter build failed", e);
                        }
                    }
            );
            names = jdbcReporterConfigs.stream().map(jdbcReporterConfig -> jdbcReporterConfig.getName()).toArray(size ->
                    new String[size]);
        }
    }

    /**
     * Remove JDBC Reporters
     */
    @Override
    public void deactivate(MetricService metricService, MetricManagementService metricManagementService) {
        if (names != null) {
            Arrays.stream(names).forEach(metricManagementService::removeReporter);
        }
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void onDataSourceServiceReady(DataSourceService service) {
        if (logger.isDebugEnabled()) {
            logger.debug("The JNDI datasource lookup for JDBC Reporter should work now");
        }
    }


    protected void unregisterDataSourceService(DataSourceService dataSourceService) {
        if (logger.isDebugEnabled()) {
            logger.debug("The JNDI datasource is unregistered");
        }
    }

}
