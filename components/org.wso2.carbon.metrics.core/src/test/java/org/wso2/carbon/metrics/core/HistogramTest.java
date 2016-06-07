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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

/**
 * Test Cases for {@link Histogram}
 */
public class HistogramTest extends BaseMetricTest {

    @Test
    public void testInitialCount() {
        Histogram histogram =
                metricService.histogram(MetricService.name(this.getClass(), "test-initial-count"), Level.INFO);
        Assert.assertEquals(histogram.getCount(), 0);
    }

    @Test
    public void testParentCount() {
        Histogram main = metricService.histogram("org.wso2.carbon.metrics.histogram.test.value", Level.INFO);
        Histogram sub = metricService.histogram("org.wso2.carbon.metrics.histogram.test[+].sub.value",
                Level.INFO, Level.INFO);
        sub.update(random.nextInt());
        main.update(random.nextInt());
        Assert.assertEquals(sub.getCount(), 1);
        Assert.assertEquals(main.getCount(), 2);
        sub.update(random.nextLong());
        main.update(random.nextLong());
        Assert.assertEquals(sub.getCount(), 2);
        Assert.assertEquals(main.getCount(), 4);
    }

    @Test
    public void testSameMetric() {
        Histogram histogram =
                metricService.histogram(MetricService.name(this.getClass(), "test-same-histogram"), Level.INFO);
        histogram.update(random.nextInt());
        Assert.assertEquals(histogram.getCount(), 1);

        Histogram histogram2 =
                metricService.histogram(MetricService.name(this.getClass(), "test-same-histogram"), Level.INFO);
        Assert.assertEquals(histogram2.getCount(), 1);
    }

    @Test
    public void testSameMetricWithParent() {
        Histogram main = metricService.histogram("org.wso2.carbon.metrics.histogram.test1.value", Level.INFO);
        Histogram sub = metricService.histogram("org.wso2.carbon.metrics.histogram.test1[+].sub.value",
                Level.INFO, Level.INFO);

        Histogram main2 = metricService.histogram("org.wso2.carbon.metrics.histogram.test1.value", Level.INFO);
        Histogram sub2 = metricService.histogram("org.wso2.carbon.metrics.histogram.test1[+].sub.value",
                Level.INFO, Level.INFO);

        sub.update(random.nextInt());
        Assert.assertEquals(sub.getCount(), 1);
        Assert.assertEquals(sub2.getCount(), 1);
        Assert.assertEquals(main.getCount(), 1);
        Assert.assertEquals(main2.getCount(), 1);

        main.update(random.nextInt());
        Assert.assertEquals(sub.getCount(), 1);
        Assert.assertEquals(sub2.getCount(), 1);
        Assert.assertEquals(main.getCount(), 2);
        Assert.assertEquals(main2.getCount(), 2);
    }

    @Test
    public void testMetricWithNonExistingParents() {
        Histogram sub2 = metricService.histogram("org.wso2.carbon.metrics.histogram.test2[+].sub1[+].sub2.value",
                Level.INFO,
                Level.INFO, Level.INFO);
        Histogram sub1 = metricService.histogram("org.wso2.carbon.metrics.histogram.test2[+].sub1.value", Level.INFO,
                Level.INFO);
        Histogram main = metricService.histogram("org.wso2.carbon.metrics.histogram.test2.value", Level.INFO);

        sub2.update(random.nextInt());
        Assert.assertEquals(sub1.getCount(), 1);
        Assert.assertEquals(sub2.getCount(), 1);
        Assert.assertEquals(main.getCount(), 1);

        sub1.update(random.nextInt());
        Assert.assertEquals(sub1.getCount(), 2);
        Assert.assertEquals(sub2.getCount(), 1);
        Assert.assertEquals(main.getCount(), 2);

        main.update(random.nextInt());
        Assert.assertEquals(sub1.getCount(), 2);
        Assert.assertEquals(sub2.getCount(), 1);
        Assert.assertEquals(main.getCount(), 3);
    }

    @Test
    public void testUpdateInt() {
        Histogram histogram =
                metricService.histogram(MetricService.name(this.getClass(), "test-histogram-update-int"), Level.INFO);
        histogram.update(random.nextInt());
        Assert.assertEquals(histogram.getCount(), 1);

        metricManagementService.setRootLevel(Level.OFF);
        histogram.update(random.nextInt());
        Assert.assertEquals(histogram.getCount(), 1);
    }

    @Test
    public void testUpdateLong() {
        Histogram histogram =
                metricService.histogram(MetricService.name(this.getClass(), "test-histogram-update-long"), Level.INFO);

        histogram.update(random.nextLong());
        Assert.assertEquals(histogram.getCount(), 1);

        metricManagementService.setRootLevel(Level.OFF);
        histogram.update(random.nextLong());
        Assert.assertEquals(histogram.getCount(), 1);
    }

    @Test
    public void testSnapshot() {
        Histogram histogram = metricService.histogram(MetricService.name(this.getClass(), "test-snapshot"), Level.INFO);
        testSnapshot(histogram);
    }

    @Test
    public void testHistogramCollectionSnapshot() {
        Histogram histogram = metricService.histogram("org.wso2.carbon.metrics.histogram.test3[+].sub1.value",
                Level.INFO, Level.INFO);
        testSnapshot(histogram);
    }

    private void testSnapshot(Histogram histogram) {
        IntStream.rangeClosed(1, 100).forEach(histogram::update);
        Snapshot snapshot = histogram.getSnapshot();
        testSnapshot(snapshot);
    }

}
