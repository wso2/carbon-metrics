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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.metrics.impl.MetricServiceImpl;
import org.wso2.carbon.metrics.impl.MetricsConfigException;
import org.wso2.carbon.metrics.impl.MetricsConfiguration;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * @scr.component name="org.wso2.carbon.metrics.impl.internal.MetricsImplComponent" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class MetricsImplComponent {

    private static final Log log = LogFactory.getLog(MetricsImplComponent.class);

    @SuppressWarnings("rawtypes")
    private ServiceRegistration metricsServiceRegistration;

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Metrics manager component activated");
        }
        MetricsConfiguration configuration = new MetricsConfiguration();
        String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "metrics.xml";
        try {
            configuration.load(filePath);
        } catch (MetricsConfigException e) {
            if (log.isErrorEnabled()) {
                log.error("Error reading configuration from " + filePath, e);
            }
        }

        MetricService metricService = new MetricServiceImpl(configuration);

        metricsServiceRegistration = componentContext.getBundleContext().registerService(MetricService.class.getName(),
                metricService, null);

    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating Metrics manager component");
        }
        metricsServiceRegistration.unregister();
    }

    // This service is required to lookup data source in MetricServiceImpl.
    // Otherwise the data source reading component will not be activated before this component.
    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }

}
