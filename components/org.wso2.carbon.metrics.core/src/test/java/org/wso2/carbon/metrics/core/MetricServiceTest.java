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
 * Test Cases for MetricService
 */
public class MetricServiceTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(MetricServiceTest.class);

    @BeforeMethod
    private static void setRootLevelToOff() {
        if (logger.isInfoEnabled()) {
            logger.info("Resetting Root Level to {}", Level.OFF);
        }
        // Test setRootLevel(String)
        metricService.setRootLevel(Level.OFF.name());
    }

    @Test
    public void testMeterInitialCount() {
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-initial-count"), Level.INFO);
        Assert.assertEquals(meter.getCount(), 0);
        Assert.assertTrue(metricService.getMetricsCount() > 0, "Metrics count should be greater than zero");
    }

    @Test
    public void testDuplicateMetric() {
        String name = "test-name";
        MetricManager.meter(MetricManager.name(this.getClass(), name), Level.INFO);

        try {
            // Different Level
            MetricManager.meter(MetricManager.name(this.getClass(), name), Level.DEBUG);
            Assert.fail("Meter should not be created");
        } catch (IllegalArgumentException e) {
            // Ignore
        }

        try {
            // Different Metric Type
            MetricManager.counter(MetricManager.name(this.getClass(), name), Level.INFO);
            Assert.fail("Counter should not be created");
        } catch (IllegalArgumentException e) {
            // Ignore
        }
    }

    @Test
    public void testEnableDisable() {
        Assert.assertTrue(metricService.isEnabled(), "Metric Service should be enabled");
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-enabled"), Level.INFO);

        metricService.setRootLevel(Level.TRACE);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricService.disable();
        Assert.assertFalse(metricService.isEnabled(), "Metric Service should be disabled");

        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricService.enable();
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);
    }

    @Test
    public void testMetricSetLevel() {
        String name = MetricManager.name(this.getClass(), "test-metric-level");
        Meter meter = MetricManager.meter(name, Level.INFO);
        Assert.assertNull(metricService.getMetricLevel(name), "There should be no configured level");

        metricService.setRootLevel(Level.TRACE);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricService.setMetricLevel(name, Level.INFO);
        Assert.assertEquals(metricService.getMetricLevel(name), Level.INFO, "Configured level should be INFO");
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);

        metricService.setMetricLevel(name, Level.DEBUG);
        Assert.assertEquals(metricService.getMetricLevel(name), Level.DEBUG, "Configured level should be DEBUG");

        meter.mark();
        Assert.assertEquals(meter.getCount(), 3);

        metricService.setMetricLevel(name, Level.TRACE);
        Assert.assertEquals(metricService.getMetricLevel(name), Level.TRACE, "Configured level should be TRACE");
        meter.mark();
        Assert.assertEquals(meter.getCount(), 4);

        metricService.setMetricLevel(name, Level.ALL);
        Assert.assertEquals(metricService.getMetricLevel(name), Level.ALL, "Configured level should be ALL");

        meter.mark();
        Assert.assertEquals(meter.getCount(), 5);

        metricService.setMetricLevel(name, Level.OFF);
        Assert.assertEquals(metricService.getMetricLevel(name), Level.OFF, "Configured level should be OFF");
        meter.mark();
        Assert.assertEquals(meter.getCount(), 5);

        metricService.setMetricLevel(name, Level.INFO);
        Assert.assertEquals(metricService.getMetricLevel(name), Level.INFO, "Configured level should be INFO");
        meter.mark();
        Assert.assertEquals(meter.getCount(), 6);
    }

    @Test
    public void testMetricServiceLevels() {
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-levels"), Level.INFO);
        meter.mark();
        // This is required as we need to check whether level changes are applied to existing metrics
        Assert.assertEquals(meter.getCount(), 0);

        metricService.setRootLevel(Level.TRACE);
        Assert.assertEquals(metricService.getRootLevel(), Level.TRACE.name());
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricService.setRootLevel(Level.DEBUG);
        Assert.assertEquals(metricService.getRootLevel(), Level.DEBUG.name());
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);

        metricService.setRootLevel(Level.INFO);
        Assert.assertEquals(metricService.getRootLevel(), Level.INFO.name());
        meter.mark();
        Assert.assertEquals(meter.getCount(), 3);

        metricService.setRootLevel(Level.ALL);
        Assert.assertEquals(metricService.getRootLevel(), Level.ALL.name());
        meter.mark();
        Assert.assertEquals(meter.getCount(), 4);

        metricService.setRootLevel(Level.OFF);
        Assert.assertEquals(metricService.getRootLevel(), Level.OFF.name());
        meter.mark();
        // There should be no change
        Assert.assertEquals(meter.getCount(), 4);
    }

    @Test
    public void testMetricLevels() {
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test1"), Level.OFF);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 0);
        metricService.setRootLevel(Level.OFF);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 0);

        metricService.setRootLevel(Level.TRACE);
        meter = MetricManager.meter(MetricManager.name(this.getClass(), "test2"), Level.TRACE);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        meter = MetricManager.meter(MetricManager.name(this.getClass(), "test3"), Level.DEBUG);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricService.setRootLevel(Level.DEBUG);
        meter = MetricManager.meter(MetricManager.name(this.getClass(), "test4"), Level.TRACE);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 0);

        meter = MetricManager.meter(MetricManager.name(this.getClass(), "test5"), Level.DEBUG);
        meter.mark(100);
        Assert.assertEquals(meter.getCount(), 100);
    }
}
