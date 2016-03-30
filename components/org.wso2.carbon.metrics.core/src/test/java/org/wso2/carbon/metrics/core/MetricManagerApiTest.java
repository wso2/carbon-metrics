/*
 * Copyright 2015 WSO2 Inc. (http://wso2.org)
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
import org.testng.annotations.Test;

/**
 * Test Cases for Metric Manager API
 */
public class MetricManagerApiTest extends BaseTest {

    @Test
    public void testCreateSingleCounter() {
        // create new counter
        Counter counter1 = MetricManager.counter("org.wso2.main.counter", Level.INFO);
        // retrieve created counter
        Counter counter2 = null;
        try {
            counter2 = MetricManager.getCounter("org.wso2.main.counter");
        } catch (MetricNotFoundException ignored) {

        }
        // get or create counter
        Counter counter3 = MetricManager.counter("org.wso2.main.counter", Level.INFO);
        counter1.inc();

        Assert.assertTrue(counter2 != null, "Counter should not be null");
        Assert.assertEquals(counter1.getCount(), 1);
        Assert.assertEquals(counter2.getCount(), 1);
        Assert.assertEquals(counter3.getCount(), 1);
    }

    @Test
    public void testCreateMultipleCounters() {
        // create new counters
        Counter subCounterCollection1 =
                MetricManager.counter("org.wso2.main1[+].sub.counter", Level.INFO, Level.INFO);
        // retrieve created counters
        Counter subCounterCollection2 = null;
        try {
            subCounterCollection2 = MetricManager.getCounter("org.wso2.main1[+].sub.counter");
            Counter subCounter1 = MetricManager.getCounter("org.wso2.main1.sub.counter");
            Counter mainCounter1 = MetricManager.getCounter("org.wso2.main1.counter");

            // get or create counters
            Counter subCounter2 = MetricManager.counter("org.wso2.main1.sub.counter", Level.INFO);
            Counter mainCounter2 = MetricManager.counter("org.wso2.main1.counter", Level.INFO);

            subCounterCollection1.inc(1);

            Assert.assertEquals(subCounterCollection1.getCount(), 1);
            Assert.assertEquals(subCounterCollection2.getCount(), 1);
            Assert.assertEquals(subCounter1.getCount(), 1);
            Assert.assertEquals(mainCounter1.getCount(), 1);
            Assert.assertEquals(subCounter2.getCount(), 1);
            Assert.assertEquals(mainCounter2.getCount(), 1);
        } catch (MetricNotFoundException e) {
            Assert.fail("Metric should exist");
        }
    }

    @Test
    public void testGetUndefinedCounter() {
        try {
            MetricManager.getCounter("org.wso2.main2.counter");
            Assert.fail("Should throw an exception, cannot retrieve undefined metric");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof MetricNotFoundException);
        }
    }

    @Test
    public void testGetWrongMetricType() {
        try {
            MetricManager.counter("org.wso2.main3.counter", Level.INFO);
            MetricManager.getMeter("org.wso2.main3.counter");
            Assert.fail("Should throw an exception, cannot retrieve metric when the metric type is different");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testAnnotatedNameToCreateSingleMetric() {
        try {
            MetricManager.counter("org.wso2.main4[+].sub.counter", Level.INFO);
            Assert.fail("Should throw an exception, cannot retrieve metric when there's no sufficient Levels"
                    + " to suite annotated name");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testGetMetricWithDifferentLevel() {
        try {
            MetricManager.counter("org.wso2.main5.counter", Level.INFO);
            MetricManager.counter("org.wso2.main5.counter", Level.DEBUG);
            Assert.fail("Should throw an exception, cannot retrieve metric when the metric level is different");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
