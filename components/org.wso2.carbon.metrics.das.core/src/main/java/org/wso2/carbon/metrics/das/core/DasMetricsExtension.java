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
package org.wso2.carbon.metrics.das.core;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.spi.MetricsExtension;
import org.wso2.carbon.metrics.das.core.config.model.DasReporterConfig;
import org.wso2.carbon.metrics.das.core.config.model.MetricsConfig;

import java.util.Arrays;
import java.util.Set;

/**
 * Metrics Extension to support DAS Reporter.
 */
@Component(
        name = "org.wso2.carbon.metrics.das.core.DasMetricsExtension",
        service = MetricsExtension.class
)
public class DasMetricsExtension implements MetricsExtension {

    private static final Logger logger = LoggerFactory.getLogger(DasMetricsExtension.class);

    private String[] names;

    /**
     * Add DAS Reporters.
     */
    @Override
    public void activate(ConfigProvider configProvider, MetricService metricService,
                         MetricManagementService metricManagementService) {
        MetricsConfig metricsConfig;
        try {
            metricsConfig = configProvider.getConfigurationObject(MetricsConfig.class);
        } catch (ConfigurationException e) {
            logger.error("Error loading Metrics Configuration", e);
            metricsConfig = new MetricsConfig();
        }
        Set<DasReporterConfig> dasReporterConfigs = metricsConfig.getReporting().getDas();
        if (dasReporterConfigs != null) {
            dasReporterConfigs.forEach(reporterBuilder -> {
                        try {
                            metricManagementService.addReporter(reporterBuilder);
                        } catch (ReporterBuildException e) {
                            logger.warn("DAS Reporter build failed", e);
                        }
                    }
            );
            names = dasReporterConfigs.stream().map(dasReporterConfig -> dasReporterConfig.getName()).toArray(size ->
                    new String[size]);
        }
    }

    /**
     * Remove DAS Reporters.
     */
    @Override
    public void deactivate(MetricService metricService, MetricManagementService metricManagementService) {
        if (names != null) {
            Arrays.stream(names).forEach(metricManagementService::removeReporter);
        }
    }

    /**
     * This bind method will be called when {@link MetricService} is registered.
     *
     * @param metricService The {@link MetricService} instance registered as an OSGi service
     */
    @Reference(
            name = "carbon.metrics.service",
            service = MetricService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetricService"
    )
    protected void setMetricService(MetricService metricService) {
        // This extension should be activated only after getting MetricService.
        // Metrics Component will activate this extension.
        if (logger.isDebugEnabled()) {
            logger.debug("Metric Service is available as an OSGi service.");
        }
    }

    /**
     * This is the unbind method which gets called at the un-registration of {@link MetricService}
     *
     * @param metricService The {@link MetricService} instance registered as an OSGi service
     */
    protected void unsetMetricService(MetricService metricService) {
        // Ignore
    }

}
