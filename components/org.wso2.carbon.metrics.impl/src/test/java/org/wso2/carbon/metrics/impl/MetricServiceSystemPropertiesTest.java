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
package org.wso2.carbon.metrics.impl;

import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.internal.MetricServiceValueHolder;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

import junit.framework.TestCase;

/**
 * Test Cases for {@link MetricService}
 */
public class MetricServiceSystemPropertiesTest extends TestCase {

    private static MetricService metricService;

    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("metrics.enabled", "true");
        System.setProperty("metrics.rootLevel", "INFO");
        MetricsConfiguration configuration = Utils.getConfiguration();
        MetricsLevelConfiguration levelConfiguration = Utils.getLevelConfiguration();
        metricService = new MetricServiceImpl.Builder().configure(configuration).build(levelConfiguration);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
        MetricServiceValueHolder.registerMetricServiceInstance(metricService);
    }

    public void testMetricServiceLevels() {
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-levels"), Level.INFO);
        meter.mark();
        assertEquals("Count should be one", 1, meter.getCount());
    }

}
