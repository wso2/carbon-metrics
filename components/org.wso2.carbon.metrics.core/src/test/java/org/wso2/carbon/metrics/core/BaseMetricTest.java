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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.util.Random;

/**
 * Base Class for all Metric Test Cases
 */
public abstract class BaseMetricTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseMetricTest.class);

    protected static Random random = new Random();

    protected static Metrics metrics;

    protected static MetricService metricService;

    protected static MetricManagementService metricManagementService;

    @BeforeSuite
    protected void init() throws Exception {
        // Set Carbon Home to load configs
        System.setProperty("carbon.home", "target");
        System.setProperty("metrics.target", "target");
        // Initialize the Metrics
        metrics = new Metrics.Builder().build();
        metrics.activate();
        metricService = metrics.getMetricService();
        metricManagementService = metrics.getMetricManagementService();
        // Stop reporters
        metricManagementService.stopReporters();
    }

    @AfterSuite
    protected static void destroy() throws Exception {
        metrics.deactivate();
    }

    @BeforeMethod
    protected static void resetRootLevel() {
        if (logger.isTraceEnabled()) {
            logger.trace("Resetting Root Level to {}", Level.ALL);
        }
        metricManagementService.setRootLevel(Level.ALL);
    }


    protected void testSnapshot(Snapshot snapshot) {
        double delta = 0.2D;
        Assert.assertEquals(snapshot.getMin(), 1L);
        Assert.assertEquals(snapshot.getMax(), 100L);
        Assert.assertEquals(snapshot.getMean(), 50.5D, delta);
        Assert.assertEquals(snapshot.getStdDev(), 28.86607004772212D, delta);
        Assert.assertEquals(snapshot.getMedian(), 50D, delta);
        Assert.assertEquals(snapshot.get75thPercentile(), 75D, delta);
        Assert.assertEquals(snapshot.get95thPercentile(), 95D, delta);
        Assert.assertEquals(snapshot.get98thPercentile(), 98D, delta);
        Assert.assertEquals(snapshot.get99thPercentile(), 99D, delta);
        Assert.assertEquals(snapshot.get999thPercentile(), 100D, delta);
        Assert.assertEquals(snapshot.getValue(0.55D), 55D, delta);
        Assert.assertEquals(snapshot.getValues().length, 100L);
        Assert.assertEquals(snapshot.size(), 100);
    }
}
