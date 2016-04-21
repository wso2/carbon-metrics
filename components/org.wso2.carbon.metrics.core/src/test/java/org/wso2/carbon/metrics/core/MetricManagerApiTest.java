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
        Counter counter1 = MetricManager.counter("org.wso2.carbon.metrics.api.test.counter", Level.INFO);
        // retrieve created counter
        Counter counter2 = null;
        try {
            counter2 = MetricManager.getCounter("org.wso2.carbon.metrics.api.test.counter");
        } catch (MetricNotFoundException ignored) {

        }
        // get or create counter
        Counter counter3 = MetricManager.counter("org.wso2.carbon.metrics.api.test.counter", Level.INFO);
        counter1.inc();

        Assert.assertTrue(counter2 != null, "Counter should not be null");
        Assert.assertEquals(counter1.getCount(), 1);
        Assert.assertEquals(counter2.getCount(), 1);
        Assert.assertEquals(counter3.getCount(), 1);
    }

    @Test
    public void testSimpleName() {
        Counter counter = MetricManager.counter("counter1", Level.INFO);
        counter.inc();
        Assert.assertEquals(counter.getCount(), 1);
    }

    @Test
    public void testInvalidMultipleLevels() {
        try {
            MetricManager.counter("counter2", Level.INFO, Level.INFO);
            Assert.fail("Counter shouldn't be created");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testCreateMultipleCounters() {
        // create new counters
        Counter subCounterCollection1 =
                MetricManager.counter("org.wso2.carbon.metrics.api.test1[+].sub.counter", Level.INFO, Level.INFO);
        // retrieve created counters
        Counter subCounterCollection2 = null;
        try {
            subCounterCollection2 = MetricManager.getCounter("org.wso2.carbon.metrics.api.test1[+].sub.counter");
            Counter subCounter1 = MetricManager.getCounter("org.wso2.carbon.metrics.api.test1.sub.counter");
            Counter mainCounter1 = MetricManager.getCounter("org.wso2.carbon.metrics.api.test1.counter");

            // get or create counters
            Counter subCounter2 = MetricManager.counter("org.wso2.carbon.metrics.api.test1.sub.counter", Level.INFO);
            Counter mainCounter2 = MetricManager.counter("org.wso2.carbon.metrics.api.test1.counter", Level.INFO);

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
            MetricManager.getCounter("org.wso2.carbon.metrics.api.test2.counter");
            Assert.fail("Should throw an exception, cannot retrieve undefined metric");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof MetricNotFoundException);
        }
    }

    @Test
    public void testGetWrongMetricType() {
        try {
            MetricManager.counter("org.wso2.carbon.metrics.api.test3.counter", Level.INFO);
            MetricManager.getMeter("org.wso2.carbon.metrics.api.test3.counter");
            Assert.fail("Should throw an exception, cannot retrieve metric when the metric type is different");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testWrongMetricCollectionType() {
        try {
            MetricManager.meter("org.wso2.carbon.metrics.api.test4[+].sub.meter", Level.INFO, Level.INFO);
            MetricManager.counter("org.wso2.carbon.metrics.api.test4[+].sub.meter", Level.INFO, Level.INFO);
            Assert.fail("Should throw an exception, cannot retrieve metric when the metric type is different");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testInvalidAnnotations() {
        String failMessage = "Invalid name should not be accepted";
        try {
            MetricManager.counter("api[+].sub", Level.INFO, Level.INFO);
            Assert.fail(failMessage);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            MetricManager.counter("api.sub[+].counter", Level.INFO, Level.INFO);
            Assert.fail(failMessage);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            MetricManager.counter("api.sub.counter[+]", Level.INFO, Level.INFO);
            Assert.fail(failMessage);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            MetricManager.counter("api[+].sub.counter", Level.INFO, Level.INFO);
        } catch (Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testAnnotatedNameToCreateSingleMetric() {
        try {
            MetricManager.counter("org.wso2.carbon.metrics.api.test4[+].sub.counter", Level.INFO);
            Assert.fail("Should throw an exception as there is only one level specified for the annotated name");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            MetricManager.counter("org.wso2.carbon.metrics.api.test4[+].sub.counter", Level.INFO, Level.INFO
                    , Level.INFO);
            Assert.fail("Should throw an exception, cannot retrieve metric when the levels do not match with "
                    + "the annotated name");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testGetMetricWithDifferentLevel() {
        try {
            MetricManager.counter("org.wso2.carbon.metrics.api.test5.counter", Level.INFO);
            MetricManager.counter("org.wso2.carbon.metrics.api.test5.counter", Level.DEBUG);
            Assert.fail("Should throw an exception, cannot retrieve metric when the metric level is different");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testCreateSingleMeter() {
        // Create a new meter
        Meter meter1 = MetricManager.meter("org.wso2.carbon.metrics.api.test.meter", Level.INFO);
        // retrieve created meter
        Meter meter2 = null;
        try {
            meter2 = MetricManager.getMeter("org.wso2.carbon.metrics.api.test.meter");
        } catch (MetricNotFoundException ignored) {

        }
        // get or create meter
        Meter meter3 = MetricManager.meter("org.wso2.carbon.metrics.api.test.meter", Level.INFO);
        meter1.mark();

        Assert.assertNotNull(meter2);
        Assert.assertEquals(meter1.getCount(), 1);
        Assert.assertEquals(meter2.getCount(), 1);
        Assert.assertEquals(meter3.getCount(), 1);
    }

    @Test
    public void testCreateMultipleMeters() {
        // create new meters
        Meter subMeterCollection1 =
                MetricManager.meter("org.wso2.carbon.metrics.api.test1[+].sub.meter", Level.INFO, Level.INFO);
        // retrieve created meters
        try {
            Meter subMeterCollection2 = MetricManager.getMeter("org.wso2.carbon.metrics.api.test1[+].sub.meter");
            Meter subMeter1 = MetricManager.getMeter("org.wso2.carbon.metrics.api.test1.sub.meter");
            Meter mainMeter1 = MetricManager.getMeter("org.wso2.carbon.metrics.api.test1.meter");

            // get or create meters
            Meter subCounter2 = MetricManager.meter("org.wso2.carbon.metrics.api.test1.sub.meter", Level.INFO);
            Meter mainCounter2 = MetricManager.meter("org.wso2.carbon.metrics.api.test1.meter", Level.INFO);

            subMeterCollection1.mark();

            Assert.assertEquals(subMeterCollection1.getCount(), 1);
            Assert.assertEquals(subMeterCollection2.getCount(), 1);
            Assert.assertEquals(subMeter1.getCount(), 1);
            Assert.assertEquals(mainMeter1.getCount(), 1);
            Assert.assertEquals(subCounter2.getCount(), 1);
            Assert.assertEquals(mainCounter2.getCount(), 1);
        } catch (MetricNotFoundException e) {
            Assert.fail("Metric should exist");
        }
    }

    @Test
    public void testGetUndefinedMeter() {
        try {
            MetricManager.getMeter("org.wso2.carbon.metrics.api.test2.meter");
            Assert.fail("Should throw an exception, cannot retrieve undefined metric");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof MetricNotFoundException);
        }
    }

    @Test
    public void testCreateSingleHistogram() {
        // Create a new histogram
        Histogram histogram1 = MetricManager.histogram("org.wso2.carbon.metrics.api.test.histogram", Level.INFO);
        // retrieve created histogram
        Histogram histogram2 = null;
        try {
            histogram2 = MetricManager.getHistogram("org.wso2.carbon.metrics.api.test.histogram");
        } catch (MetricNotFoundException ignored) {

        }
        // get or create histogram
        Histogram histogram3 = MetricManager.histogram("org.wso2.carbon.metrics.api.test.histogram", Level.INFO);
        histogram1.update(1);

        Assert.assertNotNull(histogram2);
        Assert.assertEquals(histogram1.getCount(), 1);
        Assert.assertEquals(histogram2.getCount(), 1);
        Assert.assertEquals(histogram3.getCount(), 1);
    }

    @Test
    public void testCreateMultipleHistograms() {
        // create new histograms
        Histogram subHistogramCollection1 =
                MetricManager.histogram("org.wso2.carbon.metrics.api.test1[+].sub.histogram", Level.INFO, Level.INFO);
        // retrieve created histograms
        try {
            Histogram subHistogramCollection2 = MetricManager
                    .getHistogram("org.wso2.carbon.metrics.api.test1[+].sub.histogram");
            Histogram subHistogram1 = MetricManager.getHistogram("org.wso2.carbon.metrics.api.test1.sub.histogram");
            Histogram mainHistogram1 = MetricManager.getHistogram("org.wso2.carbon.metrics.api.test1.histogram");

            // get or create histograms
            Histogram subCounter2 = MetricManager.histogram("org.wso2.carbon.metrics.api.test1.sub.histogram",
                    Level.INFO);
            Histogram mainCounter2 = MetricManager.histogram("org.wso2.carbon.metrics.api.test1.histogram", Level.INFO);

            subHistogramCollection1.update(1);

            Assert.assertEquals(subHistogramCollection1.getCount(), 1);
            Assert.assertEquals(subHistogramCollection2.getCount(), 1);
            Assert.assertEquals(subHistogram1.getCount(), 1);
            Assert.assertEquals(mainHistogram1.getCount(), 1);
            Assert.assertEquals(subCounter2.getCount(), 1);
            Assert.assertEquals(mainCounter2.getCount(), 1);
        } catch (MetricNotFoundException e) {
            Assert.fail("Metric should exist");
        }
    }

    @Test
    public void testGetUndefinedHistogram() {
        try {
            MetricManager.getHistogram("org.wso2.carbon.metrics.api.test2.histogram");
            Assert.fail("Should throw an exception, cannot retrieve undefined metric");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof MetricNotFoundException);
        }
    }
}
