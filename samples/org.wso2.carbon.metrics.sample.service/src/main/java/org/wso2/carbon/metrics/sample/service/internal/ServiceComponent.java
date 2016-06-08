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
package org.wso2.carbon.metrics.sample.service.internal;

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
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.sample.service.RandomNumberService;

/**
 * Service component to refer the Metrics OSGi services
 */
@Component(
        name = "org.wso2.carbon.metrics.sample.service.internal.ServiceComponent",
        immediate = true
)
public class ServiceComponent {

    private static final Logger logger = LoggerFactory.getLogger(ServiceComponent.class);

    private ServiceRegistration serviceRegistration;

    /**
     * This is the activation method of ServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     */
    @Activate
    protected void activate(BundleContext bundleContext) {
        logger.info("Service Component is activated");

        // Register RandomNumberServiceImpl instance as an OSGi service.
        serviceRegistration = bundleContext.registerService(RandomNumberService.class, new RandomNumberServiceImpl(),
                null);
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are not satisfied during runtime.
     */
    @Deactivate
    protected void deactivate() {
        logger.info("Service Component is deactivated");

        // Unregister the OSGi service
        serviceRegistration.unregister();
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
        DataHolder.getInstance().setMetricService(metricService);
    }

    /**
     * This is the unbind method which gets called at the un-registration of {@link MetricService}
     *
     * @param metricService The {@link MetricService} instance registered as an OSGi service
     */
    protected void unsetMetricService(MetricService metricService) {
        DataHolder.getInstance().setMetricService(null);
    }

    /**
     * This bind method will be called when {@link MetricManagementService} is registered.
     *
     * @param metricManagementService The {@link MetricManagementService} instance registered as an OSGi service
     */
    @Reference(
            name = "carbon.metrics.management.service",
            service = MetricManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetricManagementService"
    )
    protected void setMetricManagementService(MetricManagementService metricManagementService) {
        DataHolder.getInstance().setMetricManagementService(metricManagementService);
    }

    /**
     * This is the unbind method which gets called at the un-registration of {@link MetricManagementService}
     *
     * @param metricManagementService The {@link MetricManagementService} instance registered as an OSGi service
     */
    protected void unsetMetricManagementService(MetricManagementService metricManagementService) {
        DataHolder.getInstance().setMetricManagementService(null);
    }
}
