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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.metrics.core.MetricManager;

/**
 * Metrics OSGi Component
 */
@Component(
        name = "org.wso2.carbon.metrics.core.internal.MetricsComponent",
        immediate = true)
public class MetricsComponent {

    private static final Logger logger = LoggerFactory.getLogger(MetricsComponent.class);

    @Activate
    protected void activate() {
        if (logger.isDebugEnabled()) {
            logger.debug("Metrics Component activated");
        }
        // Initialize the metric service
        MetricManager.activate();
    }

    @Deactivate
    protected void deactivate() {
        if (logger.isDebugEnabled()) {
            logger.debug("Metrics Component deactivated");
        }
        MetricManager.deactivate();
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
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


