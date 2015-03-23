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
package org.wso2.carbon.metrics.impl.internal;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.metrics.common.MetricsConfigException;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.MetricServiceImpl;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * @scr.component name="org.wso2.carbon.metrics.impl.internal.MetricsImplComponent" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class MetricsImplComponent {

    private static final Logger logger = LoggerFactory.getLogger(MetricsImplComponent.class);

    @SuppressWarnings("rawtypes")
    private ServiceRegistration metricsServiceRegistration;
    
    private MetricService metricService;

    protected void activate(ComponentContext componentContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("Metrics Service component activated");
        }
        MetricsConfiguration configuration = new MetricsConfiguration();
        String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "metrics.xml";
        try {
            configuration.load(filePath);
        } catch (MetricsConfigException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error reading configuration from " + filePath, e);
            }
        }

        metricService = new MetricServiceImpl(configuration);

        metricsServiceRegistration = componentContext.getBundleContext().registerService(MetricService.class.getName(),
                metricService, null);

    }

    protected void deactivate(ComponentContext componentContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deactivating Metrics Service component");
        }
        // Set Level to OFF to stop reporters etc.
        metricService.setLevel(Level.OFF);
        metricsServiceRegistration.unregister();
    }

    // This service is required to lookup data source in MetricServiceImpl.
    // Otherwise the data source reading component will not be activated before this component.
    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && logger.isDebugEnabled()) {
            logger.debug("Registry service initialized");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }

}
