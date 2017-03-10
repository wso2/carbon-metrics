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

import com.codahale.metrics.MetricRegistry;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.config.model.MetricsLevelConfig;
import org.wso2.carbon.metrics.core.config.model.ReservoirConfig;
import org.wso2.carbon.metrics.core.impl.MetricManager;
import org.wso2.carbon.metrics.core.impl.listener.EnabledStatusChangeListener;
import org.wso2.carbon.metrics.core.impl.listener.MetricLevelChangeListener;
import org.wso2.carbon.metrics.core.impl.listener.RootLevelChangeListener;

/**
 * Test Cases for {@link MetricManager} listeners
 */
public class MetricManagerTest {

    private static MetricManager metricManager;

    @BeforeClass
    private void load() {
        metricManager = new MetricManager(new MetricRegistry(), new MetricsLevelConfig(), new ReservoirConfig());
    }

    @Test
    public void testEnabledStatusChangeListener() {
        Assert.assertFalse(metricManager.isEnabled());
        EnabledStatusChangeListener listener1 = enabled -> Assert.assertTrue(enabled);
        metricManager.addEnabledStatusChangeListener(listener1);
        metricManager.enable();
        Assert.assertTrue(metricManager.isEnabled());

        EnabledStatusChangeListener listener2 = enabled -> Assert.assertFalse(enabled);
        metricManager.addEnabledStatusChangeListener(listener2);
        metricManager.removeEnabledStatusChangeListener(listener1);
        metricManager.disable();
        Assert.assertFalse(metricManager.isEnabled());
    }

    @Test
    public void testRootLevelChangeListener() {
        Assert.assertEquals(metricManager.getRootLevel(), Level.INFO);
        RootLevelChangeListener listener1 = (oldLevel, newLevel) -> {
            Assert.assertEquals(oldLevel, Level.OFF);
            Assert.assertEquals(newLevel, Level.INFO);
        };
        metricManager.addRootLevelChangeListener(listener1);
        metricManager.setRootLevel(Level.INFO);
        Assert.assertEquals(metricManager.getRootLevel(), Level.INFO);

        RootLevelChangeListener listener2 = (oldLevel, newLevel) -> {
            Assert.assertEquals(oldLevel, Level.INFO);
            Assert.assertEquals(newLevel, Level.DEBUG);
        };
        metricManager.addRootLevelChangeListener(listener2);
        metricManager.removeRootLevelChangeListener(listener1);
        metricManager.setRootLevel(Level.DEBUG);
        Assert.assertEquals(metricManager.getRootLevel(), Level.DEBUG);
    }


    @Test
    public void testMetricLevelChangeListener() {
        String name = MetricService.name(MetricManagerTest.class, "counter");
        metricManager.counter(name, Level.TRACE);
        MetricLevelChangeListener listener1 = (metric, oldLevel, newLevel) -> {
            Assert.assertEquals(metric.getName(), name);
            Assert.assertNull(oldLevel);
            Assert.assertEquals(newLevel, Level.DEBUG);
        };
        metricManager.addMetricLevelChangeListener(listener1);
        metricManager.setMetricLevel(name, Level.DEBUG);
        Assert.assertEquals(metricManager.getMetricLevel(name), Level.DEBUG);

        MetricLevelChangeListener listener2 = (metric, oldLevel, newLevel) -> {
            Assert.assertEquals(metric.getName(), name);
            Assert.assertEquals(oldLevel, Level.DEBUG);
            Assert.assertEquals(newLevel, Level.INFO);
        };
        metricManager.addMetricLevelChangeListener(listener2);
        metricManager.removeMetricLevelChangeListener(listener1);
        metricManager.setMetricLevel(name, Level.INFO);
        Assert.assertEquals(metricManager.getMetricLevel(name), Level.INFO);
    }

}
