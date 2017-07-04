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
package org.wso2.carbon.metrics.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test Cases for MetricManagementService.
 */
public class MetricManagementServiceTest extends BaseMetricTest {

    private static final Logger logger = LoggerFactory.getLogger(MetricManagementServiceTest.class);

    @BeforeMethod
    private void setRootLevelToOff() {
        if (logger.isInfoEnabled()) {
            logger.info("Resetting Root Level to {}", Level.OFF);
        }
        metricManagementService.setRootLevel(Level.OFF);
    }

    @Test
    public void testMeterInitialCount() {
        Meter meter = metricService.meter(MetricService.name(this.getClass(), "test-initial-count"), Level.INFO);
        Assert.assertEquals(meter.getCount(), 0);
    }

    @Test
    public void testMetricsCount() {
        Assert.assertTrue(metricManagementService.getMetricsCount() > 0);
        Assert.assertTrue(metricManagementService.getEnabledMetricsCount() > 0);
        Assert.assertTrue(metricManagementService.getEnabledMetricsCount()
                <= metricManagementService.getMetricsCount());
        Assert.assertTrue(metricManagementService.getMetricCollectionsCount() >= 0);
    }

    @Test
    public void testDuplicateMetric() {
        String name = "test-name";
        metricService.meter(MetricService.name(this.getClass(), name), Level.INFO);

        try {
            // Different Level
            metricService.meter(MetricService.name(this.getClass(), name), Level.DEBUG);
            Assert.fail("Meter should not be created");
        } catch (IllegalArgumentException e) {
            // Ignore
        }

        try {
            // Different Metric Type
            metricService.counter(MetricService.name(this.getClass(), name), Level.INFO);
            Assert.fail("Counter should not be created");
        } catch (IllegalArgumentException e) {
            // Ignore
        }
    }

    @Test
    public void testEnableDisable() {
        Assert.assertTrue(metricManagementService.isEnabled(), "Metric Service should be enabled");
        Meter meter = metricService.meter(MetricService.name(this.getClass(), "test-enabled"), Level.INFO);

        metricManagementService.setRootLevel(Level.TRACE);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricManagementService.disable();
        Assert.assertFalse(metricManagementService.isEnabled(), "Metric Service should be disabled");

        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricManagementService.enable();
        // Call again to cover the if condition
        metricManagementService.enable();
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);
    }

    @Test
    public void testMetricSetLevel() {
        String name = MetricService.name(this.getClass(), "test-metric-level");
        Meter meter = metricService.meter(name, Level.INFO);
        Assert.assertNull(metricManagementService.getMetricLevel(name), "There should be no configured level");

        metricManagementService.setRootLevel(Level.TRACE);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricManagementService.setMetricLevel(name, Level.INFO);
        Assert.assertEquals(metricManagementService.getMetricLevel(name), Level.INFO,
                "Configured level should be INFO");
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);

        metricManagementService.setMetricLevel(name, Level.DEBUG);
        Assert.assertEquals(metricManagementService.getMetricLevel(name), Level.DEBUG,
                "Configured level should be DEBUG");

        meter.mark();
        Assert.assertEquals(meter.getCount(), 3);

        metricManagementService.setMetricLevel(name, Level.TRACE);
        Assert.assertEquals(metricManagementService.getMetricLevel(name), Level.TRACE,
                "Configured level should be TRACE");
        meter.mark();
        Assert.assertEquals(meter.getCount(), 4);

        metricManagementService.setMetricLevel(name, Level.ALL);
        Assert.assertEquals(metricManagementService.getMetricLevel(name), Level.ALL,
                "Configured level should be ALL");

        meter.mark();
        Assert.assertEquals(meter.getCount(), 5);

        // Set level again
        metricManagementService.setMetricLevel(name, Level.ALL);
        Assert.assertEquals(metricManagementService.getMetricLevel(name), Level.ALL,
                "Configured level should be ALL");

        meter.mark();
        Assert.assertEquals(meter.getCount(), 6);

        metricManagementService.setMetricLevel(name, Level.OFF);
        Assert.assertEquals(metricManagementService.getMetricLevel(name), Level.OFF,
                "Configured level should be OFF");
        meter.mark();
        Assert.assertEquals(meter.getCount(), 6);

        metricManagementService.setMetricLevel(name, Level.INFO);
        Assert.assertEquals(metricManagementService.getMetricLevel(name), Level.INFO,
                "Configured level should be INFO");
        meter.mark();
        Assert.assertEquals(meter.getCount(), 7);


    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnknownMetricSetLevel() {
        metricManagementService.setMetricLevel("unknown", Level.INFO);
        Assert.fail("Set metric level should not be successful for unknown metrics");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnknownMetricGetLevel() {
        metricManagementService.getMetricLevel("unknown");
        Assert.fail("Get metric level should not be successful for unknown metrics");
    }

    @Test
    public void testMetricServiceLevels() {
        Meter meter = metricService.meter(MetricService.name(this.getClass(), "test-levels"), Level.INFO);
        meter.mark();
        // This is required as we need to check whether level changes are applied to existing metrics
        Assert.assertEquals(meter.getCount(), 0);

        metricManagementService.setRootLevel(Level.TRACE);
        Assert.assertEquals(metricManagementService.getRootLevel(), Level.TRACE);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricManagementService.setRootLevel(Level.DEBUG);
        Assert.assertEquals(metricManagementService.getRootLevel(), Level.DEBUG);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);

        metricManagementService.setRootLevel(Level.INFO);
        Assert.assertEquals(metricManagementService.getRootLevel(), Level.INFO);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 3);

        metricManagementService.setRootLevel(Level.ALL);
        Assert.assertEquals(metricManagementService.getRootLevel(), Level.ALL);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 4);

        metricManagementService.setRootLevel(Level.OFF);
        Assert.assertEquals(metricManagementService.getRootLevel(), Level.OFF);
        meter.mark();
        // There should be no change
        Assert.assertEquals(meter.getCount(), 4);
    }

    @Test
    public void testMetricLevels() {
        Meter meter = metricService.meter(MetricService.name(this.getClass(), "test1"), Level.OFF);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 0);
        metricManagementService.setRootLevel(Level.OFF);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 0);

        metricManagementService.setRootLevel(Level.TRACE);
        meter = metricService.meter(MetricService.name(this.getClass(), "test2"), Level.TRACE);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        meter = metricService.meter(MetricService.name(this.getClass(), "test3"), Level.DEBUG);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricManagementService.setRootLevel(Level.DEBUG);
        meter = metricService.meter(MetricService.name(this.getClass(), "test4"), Level.TRACE);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 0);

        meter = metricService.meter(MetricService.name(this.getClass(), "test5"), Level.DEBUG);
        meter.mark(100);
        Assert.assertEquals(meter.getCount(), 100);
    }
}
