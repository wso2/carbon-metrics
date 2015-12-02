/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.wso2.carbon.metrics.impl;

import junit.framework.TestCase;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.manager.*;
import org.wso2.carbon.metrics.manager.exception.MetricNotFoundException;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

/**
 * Test Cases for {@link Counter}
 */
public class MetricApiTest extends TestCase {

    private MetricService metricService;

    protected void setUp() throws Exception {
        super.setUp();
        MetricsConfiguration configuration = Utils.getConfiguration();
        MetricsLevelConfiguration levelConfiguration = Utils.getLevelConfiguration();
        metricService = new MetricServiceImpl.Builder().configure(configuration).build(levelConfiguration);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
    }

    public void testCreateSingleCounter() {
        // create new counter
        Counter counter1 = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
        // retrieve created counter
        Counter counter2 = null;
        try {
            counter2 = MetricManager.getCounter("org.wso2.main.throughput");
        } catch (MetricNotFoundException ignored) {

        }
        // get or create counter
        Counter counter3 = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
        counter1.inc();

        assertTrue("Counter should not be null", counter2 != null);
        assertEquals("Count should be one", 1, counter1.getCount());
        assertEquals("Count should be one", 1, counter2.getCount());
        assertEquals("Count should be one", 1, counter3.getCount());
    }

    public void testCreateMultipleCounters() {
        // create new counters
        Counter subCounterCollection1 = MetricManager.counter("org.wso2.main[+].sub.throughput", Level.INFO, Level.INFO);
        // retrieve created counters
        Counter subCounterCollection2 = null;
        try {
            subCounterCollection2 = MetricManager.getCounter("org.wso2.main[+].sub.throughput");
            Counter subCounter1 = MetricManager.getCounter("org.wso2.main.sub.throughput");
            Counter mainCounter1 = MetricManager.getCounter("org.wso2.main.throughput");

            // get or create counters
            Counter subCounter2 = MetricManager.counter("org.wso2.main.sub.throughput", Level.INFO);
            Counter mainCounter2 = MetricManager.counter("org.wso2.main.throughput", Level.INFO);

            subCounterCollection1.inc(1);

            assertEquals("Count should be three", 1, subCounterCollection1.getCount());
            assertEquals("Count should be three", 1, subCounterCollection2.getCount());
            assertEquals("Count should be three", 1, subCounter1.getCount());
            assertEquals("Count should be three", 1, mainCounter1.getCount());
            assertEquals("Count should be three", 1, subCounter2.getCount());
            assertEquals("Count should be three", 1, mainCounter2.getCount());
        } catch (MetricNotFoundException e) {
            fail("Metric should exist");
        }
    }

    public void testGetUndefinedCounter() {
        try {
            Counter counter = MetricManager.getCounter("org.wso2.main.throughput");
            fail("Should throw an exception, cannot retrieve undefined metric");
        } catch (Exception e) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(MetricNotFoundException.class));
        }
    }

    public void testGetWrongMetricType() {
        try {
            Counter counter = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
            Meter meter = MetricManager.getMeter("org.wso2.main.throughput");
            fail("Should throw an exception, cannot retrieve metric when the metric type is different");
        } catch (Exception e) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(IllegalArgumentException.class));
        }
    }

    public void testAnnotatedNameToCreateSingleMetric() {
        try {
            Counter counter = MetricManager.counter("org.wso2.main[+].sub.throughput", Level.INFO);
            fail("Should throw an exception, cannot retrieve metric when there's no sufficient Levels" +
                    " to suite annotated name");
        } catch (Exception e) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(IllegalArgumentException.class));
        }
    }

    public void testGetMetricWithDifferentLevel() {
        try {
            Counter counter1 = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
            Counter counter2 = MetricManager.counter("org.wso2.main.throughput", Level.DEBUG);
            fail("Should throw an exception, cannot retrieve metric when the metric level is different");
        } catch (Exception e) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(IllegalArgumentException.class));
        }
    }
}
