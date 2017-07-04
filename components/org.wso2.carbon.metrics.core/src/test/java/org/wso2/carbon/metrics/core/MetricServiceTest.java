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
 * Test Cases for MetricService.
 */
public class MetricServiceTest extends BaseMetricTest {

    @Test
    public void testCreateSingleCounter() {
        // create new counter
        Counter counter1 = metricService.counter("org.wso2.carbon.metrics.api.test.counter", Level.INFO);
        // retrieve created counter
        Counter counter2 = null;
        try {
            counter2 = metricService.counter("org.wso2.carbon.metrics.api.test.counter");
        } catch (MetricNotFoundException ignored) {

        }

        // get or create counter
        Counter counter3 = metricService.counter("org.wso2.carbon.metrics.api.test.counter", Level.INFO);
        counter1.inc();

        Assert.assertNotNull(counter2, "Counter should not be null");
        Assert.assertEquals(counter1.getCount(), 1);
        Assert.assertEquals(counter2.getCount(), 1);
        Assert.assertEquals(counter3.getCount(), 1);
    }

    @Test
    public void testSimpleName() {
        Counter counter = metricService.counter("counter1", Level.INFO);
        counter.inc();
        Assert.assertEquals(counter.getCount(), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidMultipleLevels() {
        // Counter shouldn't be created
        metricService.counter("counter2", Level.INFO, Level.INFO);
    }

    @Test
    public void testCreateMultipleCounters() {
        // create new counters
        Counter subCounterCollection1 =
                metricService.counter("org.wso2.carbon.metrics.api.test1[+].sub.counter", Level.INFO, Level.INFO);
        // retrieve created counters
        Counter subCounterCollection2 = null;
        try {
            subCounterCollection2 = metricService.counter("org.wso2.carbon.metrics.api.test1[+].sub.counter");
            Counter subCounter1 = metricService.counter("org.wso2.carbon.metrics.api.test1.sub.counter");
            Counter mainCounter1 = metricService.counter("org.wso2.carbon.metrics.api.test1.counter");

            // get or create counters
            Counter subCounter2 = metricService.counter("org.wso2.carbon.metrics.api.test1.sub.counter", Level.INFO);
            Counter mainCounter2 = metricService.counter("org.wso2.carbon.metrics.api.test1.counter", Level.INFO);

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

    @Test(expectedExceptions = MetricNotFoundException.class)
    public void testGetUndefinedCounter() throws MetricNotFoundException {
        // cannot retrieve undefined metric
        metricService.counter("org.wso2.carbon.metrics.api.test2.counter");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetWrongMetricType() throws MetricNotFoundException {
        // cannot retrieve metric when the metric type is different
        metricService.counter("org.wso2.carbon.metrics.api.test3.counter", Level.INFO);
        metricService.meter("org.wso2.carbon.metrics.api.test3.counter");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testWrongMetricCollectionType() {
        // cannot retrieve metric when the metric type is different
        metricService.meter("org.wso2.carbon.metrics.api.test4[+].sub.meter", Level.INFO, Level.INFO);
        metricService.counter("org.wso2.carbon.metrics.api.test4[+].sub.meter", Level.INFO, Level.INFO);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidAnnotation1() {
        metricService.counter("api[+].sub", Level.INFO, Level.INFO);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidAnnotation2() {
        metricService.counter("api.sub[+].counter", Level.INFO, Level.INFO);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidAnnotation3() {
        metricService.counter("api.sub.counter[+]", Level.INFO, Level.INFO);
    }

    @Test
    public void testValidAnnotation() {
        metricService.counter("api[+].sub.counter", Level.INFO, Level.INFO);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAnnotatedNameWithOneLevel() {
        // There is only one level specified for the annotated name
        metricService.counter("org.wso2.carbon.metrics.api.test4[+].sub.counter", Level.INFO);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAnnotatedNameWithExtraLevels() {
        // Cannot retrieve metric when the levels do not match with the annotated name
        metricService.counter("org.wso2.carbon.metrics.api.test4[+].sub.counter", Level.INFO, Level.INFO, Level.INFO);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetMetricWithDifferentLevel() {
        // Cannot retrieve metric when the metric level is different
        metricService.counter("org.wso2.carbon.metrics.api.test5.counter", Level.INFO);
        metricService.counter("org.wso2.carbon.metrics.api.test5.counter", Level.DEBUG);
    }

    @Test
    public void testCreateSingleMeter() {
        // Create a new meter
        Meter meter1 = metricService.meter("org.wso2.carbon.metrics.api.test.meter", Level.INFO);
        // retrieve created meter
        Meter meter2 = null;
        try {
            meter2 = metricService.meter("org.wso2.carbon.metrics.api.test.meter");
        } catch (MetricNotFoundException ignored) {

        }
        // get or create meter
        Meter meter3 = metricService.meter("org.wso2.carbon.metrics.api.test.meter", Level.INFO);
        meter1.mark();

        Assert.assertNotNull(meter2);
        Assert.assertEquals(meter1.getCount(), 1);
        Assert.assertEquals(meter2.getCount(), 1);
        Assert.assertEquals(meter3.getCount(), 1);
    }

    @Test
    public void testCreateMultipleMeters() throws MetricNotFoundException {
        // create new meters
        Meter subMeterCollection1 =
                metricService.meter("org.wso2.carbon.metrics.api.test1[+].sub.meter", Level.INFO, Level.INFO);
        // retrieve created meters
        Meter subMeterCollection2 = metricService.meter("org.wso2.carbon.metrics.api.test1[+].sub.meter");
        Meter subMeter1 = metricService.meter("org.wso2.carbon.metrics.api.test1.sub.meter");
        Meter mainMeter1 = metricService.meter("org.wso2.carbon.metrics.api.test1.meter");

        // get or create meters
        Meter subCounter2 = metricService.meter("org.wso2.carbon.metrics.api.test1.sub.meter", Level.INFO);
        Meter mainCounter2 = metricService.meter("org.wso2.carbon.metrics.api.test1.meter", Level.INFO);

        subMeterCollection1.mark();

        Assert.assertEquals(subMeterCollection1.getCount(), 1);
        Assert.assertEquals(subMeterCollection2.getCount(), 1);
        Assert.assertEquals(subMeter1.getCount(), 1);
        Assert.assertEquals(mainMeter1.getCount(), 1);
        Assert.assertEquals(subCounter2.getCount(), 1);
        Assert.assertEquals(mainCounter2.getCount(), 1);
    }

    @Test(expectedExceptions = MetricNotFoundException.class)
    public void testGetUndefinedMeter() throws MetricNotFoundException {
        // cannot retrieve undefined metric
        metricService.meter("org.wso2.carbon.metrics.api.test2.meter");
    }

    @Test
    public void testCreateSingleHistogram() {
        // Create a new histogram
        Histogram histogram1 = metricService.histogram("org.wso2.carbon.metrics.api.test.histogram", Level.INFO);
        // retrieve created histogram
        Histogram histogram2 = null;
        try {
            histogram2 = metricService.histogram("org.wso2.carbon.metrics.api.test.histogram");
        } catch (MetricNotFoundException ignored) {

        }
        // get or create histogram
        Histogram histogram3 = metricService.histogram("org.wso2.carbon.metrics.api.test.histogram", Level.INFO);
        histogram1.update(1);

        Assert.assertNotNull(histogram2);
        Assert.assertEquals(histogram1.getCount(), 1);
        Assert.assertEquals(histogram2.getCount(), 1);
        Assert.assertEquals(histogram3.getCount(), 1);
    }

    @Test
    public void testCreateMultipleHistograms() throws MetricNotFoundException {
        // create new histograms
        Histogram subHistogramCollection1 =
                metricService.histogram("org.wso2.carbon.metrics.api.test1[+].sub.histogram", Level.INFO, Level.INFO);
        // retrieve created histograms
        Histogram subHistogramCollection2 = metricService
                .histogram("org.wso2.carbon.metrics.api.test1[+].sub.histogram");
        Histogram subHistogram1 = metricService.histogram("org.wso2.carbon.metrics.api.test1.sub.histogram");
        Histogram mainHistogram1 = metricService.histogram("org.wso2.carbon.metrics.api.test1.histogram");

        // get or create histograms
        Histogram subCounter2 = metricService.histogram("org.wso2.carbon.metrics.api.test1.sub.histogram",
                Level.INFO);
        Histogram mainCounter2 = metricService.histogram("org.wso2.carbon.metrics.api.test1.histogram", Level.INFO);

        subHistogramCollection1.update(1);

        Assert.assertEquals(subHistogramCollection1.getCount(), 1);
        Assert.assertEquals(subHistogramCollection2.getCount(), 1);
        Assert.assertEquals(subHistogram1.getCount(), 1);
        Assert.assertEquals(mainHistogram1.getCount(), 1);
        Assert.assertEquals(subCounter2.getCount(), 1);
        Assert.assertEquals(mainCounter2.getCount(), 1);
    }

    @Test(expectedExceptions = MetricNotFoundException.class)
    public void testGetUndefinedHistogram() throws MetricNotFoundException {
        // cannot retrieve undefined metric
        metricService.histogram("org.wso2.carbon.metrics.api.test2.histogram");
    }

    @Test(expectedExceptions = MetricNotFoundException.class)
    public void testMetricRemove() throws MetricNotFoundException {
        String name = "org.wso2.carbon.metrics.api.remove.meter";
        metricService.meter(name, Level.INFO);
        Assert.assertNotNull(metricService.meter(name));
        Assert.assertTrue(metricService.remove(name));
        Assert.assertFalse(metricService.remove(name));
        // Following should throw an exception
        Assert.assertNull(metricService.meter(name));
    }

    @Test(expectedExceptions = MetricNotFoundException.class)
    public void testMetricCollectionRemove() throws MetricNotFoundException {
        String name = "org.wso2.carbon.metrics.api.remove[+].sub.histogram";
        String sub = "org.wso2.carbon.metrics.api.remove.sub.histogram";
        String main = "org.wso2.carbon.metrics.api.remove.histogram";
        metricService.histogram(name, Level.INFO, Level.INFO);
        Assert.assertNotNull(metricService.histogram(name));
        Assert.assertNotNull(metricService.histogram(sub));
        Assert.assertNotNull(metricService.histogram(main));
        Assert.assertTrue(metricService.remove(name));
        Assert.assertFalse(metricService.remove(name));
        // Following should throw an exception
        Assert.assertNull(metricService.histogram(name));
    }

    @Test(expectedExceptions = MetricNotFoundException.class)
    public void testMetricCollectionRemoveChildren() throws MetricNotFoundException {
        String name = "org.wso2.carbon.metrics.api.remove[+].sub.counter";
        String sub = "org.wso2.carbon.metrics.api.remove.sub.counter";
        String main = "org.wso2.carbon.metrics.api.remove.counter";
        metricService.counter(name, Level.INFO, Level.INFO);
        Assert.assertNotNull(metricService.counter(name));
        Assert.assertNotNull(metricService.counter(sub));
        Assert.assertNotNull(metricService.counter(main));
        Assert.assertTrue(metricService.remove(sub));
        Assert.assertFalse(metricService.remove(sub));
        Assert.assertTrue(metricService.remove(main));
        Assert.assertFalse(metricService.remove(main));
        Assert.assertTrue(metricService.remove(name));
        Assert.assertFalse(metricService.remove(name));
        // Following should throw an exception
        Assert.assertNull(metricService.histogram(sub));
    }
}
