/*
 * Copyright 2014 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.core.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Metrics;
import org.wso2.carbon.metrics.core.spi.MetricsExtension;
import org.wso2.carbon.metrics.core.utils.Utils;

/**
 * Metrics OSGi Component.
 */
@Component(name = "org.wso2.carbon.metrics.core.internal.MetricsComponent")
public class MetricsComponent {

    private static final Logger logger = LoggerFactory.getLogger(MetricsComponent.class);

    private Metrics metrics;

    private ServiceRegistration metricServiceRegistration;

    private ServiceRegistration metricManagementServiceRegistration;

    private ConfigProvider configProvider;

    @Activate
    protected void activate(BundleContext bundleContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("Metrics Component activated");
        }
        Utils.setCarbonEnvironment(true);
        metrics = new Metrics(configProvider);
        metrics.activate();
        metricServiceRegistration = bundleContext.registerService(MetricService.class, metrics.getMetricService(),
                null);
        metricManagementServiceRegistration = bundleContext.registerService(MetricManagementService.class,
                metrics.getMetricManagementService(), null);
    }

    @Deactivate
    protected void deactivate() {
        if (logger.isDebugEnabled()) {
            logger.debug("Metrics Component deactivated");
        }
        metrics.deactivate();
        metricServiceRegistration.unregister();
        metricManagementServiceRegistration.unregister();
    }

    /**
     * Get the ConfigProvider service.
     *
     * @param configProvider the ConfigProvider service that is registered as a service.
     */
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * This is the unbind method, which gets called for ConfigProvider instance un-registrations.
     *
     * @param configProvider the ConfigProvider service that get unregistered.
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        this.configProvider = null;
    }

    /**
     * This bind method will be called when CarbonRuntime OSGi service is registered.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime"
    )
    protected void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting default source to {}", carbonRuntime.getConfiguration().getId());
        }
        Utils.setDefaultSource(carbonRuntime.getConfiguration().getId());
    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        if (logger.isDebugEnabled()) {
            logger.debug("The Carbon Runtime is unregistered");
        }
    }

    /**
     * This bind method will be called when MetricsExtension OSGi service is registered.
     *
     * @param metricsExtension The MetricsExtension instance registered by the components extending Metrics
     */
    @Reference(
            name = "carbon.metrics.extension",
            service = MetricsExtension.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetricsExtension"
    )
    protected void setMetricsExtension(MetricsExtension metricsExtension) {
        if (logger.isDebugEnabled()) {
            logger.debug("Activating Metrics Extension {}", metricsExtension.getClass().getName());
        }
        metricsExtension.activate(configProvider, metrics.getMetricService(), metrics.getMetricManagementService());
    }

    /**
     * This is the unbind method which gets called at the un-registration of MetricsExtension OSGi service.
     *
     * @param metricsExtension The MetricsExtension instance registered by the components extending Metrics
     */
    protected void unsetMetricsExtension(MetricsExtension metricsExtension) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deactivating Metrics Extension {}", metricsExtension.getClass().getName());
        }
        metricsExtension.deactivate(metrics.getMetricService(), metrics.getMetricManagementService());
    }
}


