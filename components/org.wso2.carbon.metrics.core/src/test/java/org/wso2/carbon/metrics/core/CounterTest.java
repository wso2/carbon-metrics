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

/**
 * Test Cases for {@link Counter}
 */
public class CounterTest extends BaseTest {

    @Test
    public void testInitialCount() {
        Counter counter = MetricManager.counter(MetricManager.name(this.getClass(), "test-counter"), Level.INFO);
        Assert.assertEquals(counter.getCount(), 0);
    }

    @Test
    public void testParentCount() {
        Counter main = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
        Counter sub = MetricManager.counter("org.wso2.main[+].sub.throughput", Level.INFO, Level.INFO);
        sub.inc(5);
        main.dec(3);
        Assert.assertEquals(sub.getCount(), 5);
        Assert.assertEquals(main.getCount(), 2);
    }

    @Test
    public void testParentCount2() {
        Counter sub = MetricManager.counter("org.wso2.main2[+].sub.throughput", Level.INFO, Level.INFO);
        Counter sub1 = MetricManager.counter("org.wso2.main2.sub[+].sub1.throughput", Level.INFO, Level.INFO);
        Counter main = MetricManager.counter("org.wso2.main2.throughput", Level.INFO);
        sub.inc(3);
        Assert.assertEquals(sub.getCount(), 3);
        Assert.assertEquals(main.getCount(), 3);
        sub1.inc(2);
        Assert.assertEquals(main.getCount(), 3);
        Assert.assertEquals(sub.getCount(), 5);
        Assert.assertEquals(sub1.getCount(), 2);
    }

    @Test
    public void testSameMetric() {
        Counter counter = MetricManager.counter(MetricManager.name(this.getClass(), "test-same-counter"), Level.INFO);
        counter.inc();
        Assert.assertEquals(counter.getCount(), 1);
        Counter counter2 = MetricManager.counter(MetricManager.name(this.getClass(), "test-same-counter"), Level.INFO);
        Assert.assertEquals(counter2.getCount(), 1);
    }

    @Test
    public void testSameMetricWithParent() {
        Counter main = MetricManager.counter("org.wso2.main3.throughput", Level.INFO);
        Counter sub = MetricManager.counter("org.wso2.main3[+].sub.throughput", Level.INFO, Level.INFO);

        Counter main2 = MetricManager.counter("org.wso2.main3.throughput", Level.INFO);
        Counter sub2 = MetricManager.counter("org.wso2.main3[+].sub.throughput", Level.INFO, Level.INFO);

        sub.inc(5L);
        Assert.assertEquals(sub.getCount(), 5L);
        Assert.assertEquals(sub2.getCount(), 5L);
        Assert.assertEquals(main.getCount(), 5L);
        Assert.assertEquals(main2.getCount(), 5L);

        main.dec(3L);
        Assert.assertEquals(sub.getCount(), 5L);
        Assert.assertEquals(sub2.getCount(), 5L);
        Assert.assertEquals(main.getCount(), 2L);
        Assert.assertEquals(main2.getCount(), 2L);
    }

    @Test
    public void testMetricWithNonExistingParents() {
        Counter sub2 =
                MetricManager.counter("org.wso2.main4[+].sub1[+].sub2.throughput", Level.INFO, Level.INFO, Level.INFO);
        Counter sub1 = MetricManager.counter("org.wso2.main4[+].sub1.throughput", Level.INFO, Level.INFO);
        Counter main = MetricManager.counter("org.wso2.main4.throughput", Level.INFO);
        sub2.inc(5L);
        Assert.assertEquals(sub2.getCount(), 5L);
        Assert.assertEquals(sub1.getCount(), 5L);
        Assert.assertEquals(main.getCount(), 5L);

        sub1.dec(3L);
        Assert.assertEquals(sub2.getCount(), 5L);
        Assert.assertEquals(sub1.getCount(), 2L);
        Assert.assertEquals(main.getCount(), 2L);

        main.inc(10L);
        Assert.assertEquals(sub2.getCount(), 5L);
        Assert.assertEquals(sub1.getCount(), 2L);
        Assert.assertEquals(main.getCount(), 12L);
    }

    @Test
    public void testIncrementByOne() {
        Counter counter = MetricManager.counter(MetricManager.name(this.getClass(), "test-counter-inc1"), Level.INFO);
        counter.inc();
        Assert.assertEquals(counter.getCount(), 1L);

        metricService.setRootLevel(Level.OFF);
        counter.inc();
        Assert.assertEquals(counter.getCount(), 1L);
    }

    @Test
    public void testIncrementByRandomNumber() {
        Counter counter =
                MetricManager.counter(MetricManager.name(this.getClass(), "test-counter-inc-rand"), Level.INFO);
        int n = random.nextInt();
        counter.inc(n);
        Assert.assertEquals(counter.getCount(), n);

        metricService.setRootLevel(Level.OFF);
        counter.inc(n);
        Assert.assertEquals(counter.getCount(), n);
    }

    @Test
    public void testDecrementByOne() {
        Counter counter = MetricManager.counter(MetricManager.name(this.getClass(), "test-counter-dec1"), Level.INFO);
        counter.dec();
        Assert.assertEquals(counter.getCount(), -1L);

        metricService.setRootLevel(Level.OFF);
        counter.dec();
        Assert.assertEquals(counter.getCount(), -1L);
    }

    @Test
    public void testDecrementByRandomNumber() {
        Counter counter =
                MetricManager.counter(MetricManager.name(this.getClass(), "test-counter-dec-rand"), Level.INFO);
        int n = random.nextInt();
        counter.dec(n);
        Assert.assertEquals(counter.getCount(), 0 - n);

        metricService.setRootLevel(Level.OFF);
        counter.dec(n);
        Assert.assertEquals(counter.getCount(), 0 - n);
    }

}
