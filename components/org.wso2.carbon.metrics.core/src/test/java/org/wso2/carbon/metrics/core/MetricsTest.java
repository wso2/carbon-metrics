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
import org.wso2.carbon.metrics.core.utils.Utils;

import java.io.File;

/**
 * Test Disabled Metrics in Carbon Environment
 */
public class MetricsTest {

    protected static final String RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";

    private Metrics metrics;

    private MetricManagementService metricManagementService;

    @BeforeMethod
    private void activate() {
        Utils.setCarbonEnvironment(true);
        System.setProperty("metrics.conf", RESOURCES_DIR + File.separator + "metrics-disabled.yml");
        metrics = new Metrics();
        metrics.activate();
        metricManagementService = metrics.getMetricManagementService();
    }

    @AfterMethod
    private void deactivate() {
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
