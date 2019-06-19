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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "org.wso2.carbon.metrics.data.service.internal.MetricsDataComponent",
        immediate = true)
public class MetricsDataComponent {

    private static final Logger logger = LoggerFactory.getLogger(MetricsDataComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {

        if (logger.isDebugEnabled()) {
            logger.debug("Metrics Data Service component activated");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        if (logger.isDebugEnabled()) {
            logger.debug("Deactivating Metrics Data Service Component");
        }
    }

    // This service is required to lookup data source in MetricsDataService.
    // Otherwise the data source reading component will not be activated before this component.
    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        if (registryService != null && logger.isDebugEnabled()) {
            logger.debug("Registry service initialized");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {

    }
}
