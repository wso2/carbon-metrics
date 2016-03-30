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
package org.wso2.carbon.metrics.data.service.internal;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * @scr.component name="org.wso2.carbon.metrics.data.service.internal.MetricsDataComponent" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class MetricsDataComponent {

    private static final Logger logger = LoggerFactory.getLogger(MetricsDataComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("Metrics Data Service component activated");
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deactivating Metrics Data Service Component");
        }
    }

    // This service is required to lookup data source in MetricsDataService.
    // Otherwise the data source reading component will not be activated before this component.
    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && logger.isDebugEnabled()) {
            logger.debug("Registry service initialized");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }

}
