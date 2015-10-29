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
package org.wso2.carbon.metrics.manager.internal;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;

/**
 * @scr.component name="org.wso2.carbon.metrics.manager.internal.MetricManagerComponent" immediate="true"
 * @scr.reference name="metric.service" interface="org.wso2.carbon.metrics.manager.MetricService" cardinality="1..1"
 *                policy="dynamic" bind="setMetricService" unbind="unsetMetricService"
 */
public class MetricManagerComponent {

    private static final Logger log = LoggerFactory.getLogger(MetricManagerComponent.class);

    private ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Metrics manager component activated");
        }

        MetricManager.registerMXBean();
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating Metrics manager component");
        }

        MetricManager.unregisterMXBean();
    }

    protected void setMetricService(MetricService metricService) {
        serviceReferenceHolder.setMetricService(metricService);
    }

    protected void unsetMetricService(MetricService metricService) {
        serviceReferenceHolder.setMetricService(null);
    }

}
