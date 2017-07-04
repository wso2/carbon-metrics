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
package org.wso2.carbon.metrics.core;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.metrics.core.utils.Utils;

/**
 * Test Disabled Metrics in Carbon Environment.
 */
public class MetricsTest {

    private Metrics metrics;

    private MetricManagementService metricManagementService;

    @BeforeMethod
    private void activate() throws ConfigurationException {
        Utils.setCarbonEnvironment(true);
        metrics = new Metrics(TestUtils.getConfigProvider("metrics-disabled.yaml"));
        metrics.activate();
        metricManagementService = metrics.getMetricManagementService();
    }

    @AfterMethod
    private void deactivate() {
        metricManagementService.stopReporters();
        metrics.deactivate();
        Utils.setCarbonEnvironment(false);
    }

    @Test
    public void testDisabled() {
        Assert.assertFalse(metricManagementService.isEnabled());
        Assert.assertFalse(metricManagementService.isReporterRunning("JMX"));
    }

    @Test
    public void testEnable() {
        metricManagementService.enable();
        Assert.assertTrue(metricManagementService.isEnabled());
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX"));
        metricManagementService.disable();
        Assert.assertFalse(metricManagementService.isEnabled());
        Assert.assertFalse(metricManagementService.isReporterRunning("JMX"));
    }
}
