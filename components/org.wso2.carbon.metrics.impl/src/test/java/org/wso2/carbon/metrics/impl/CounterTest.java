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
package org.wso2.carbon.metrics.impl;

import java.util.Random;

import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.manager.Counter;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.ServiceReferenceHolder;

import junit.framework.TestCase;

/**
 * Test Cases for {@link Counter}
 */
public class CounterTest extends TestCase {

    private MetricService metricService;

    private Random randomGenerator = new Random();

    protected void setUp() throws Exception {
        super.setUp();
        MetricsConfiguration configuration = Utils.getConfiguration();
        MetricsLevelConfiguration levelConfiguration = Utils.getLevelConfiguration();
        metricService = new MetricServiceImpl.Builder().configure(configuration).build(levelConfiguration);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
    }

    public void testInitialCount() {
        Counter counter = MetricManager.counter(MetricManager.name(this.getClass(), "test-counter"), Level.INFO);
        assertEquals("Initial count should be zero", 0, counter.getCount());
    }

    public void testParentCount() {
        Counter main = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
        Counter sub = MetricManager.counter("org.wso2.main[+].sub.throughput", Level.INFO, Level.INFO);
        sub.inc(5);
        main.dec(3);
        assertEquals("Count should be five", 5, sub.getCount());
        assertEquals("Count should be two", 2, main.getCount());
    }

    public void testParentCount2() {
        Counter sub = MetricManager.counter("org.wso2.main[+].sub.throughput", Level.INFO, Level.INFO);
        Counter sub1 = MetricManager.counter("org.wso2.main.sub[+].sub1.throughput", Level.INFO, Level.INFO);
        Counter main = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
        sub.inc(3);
        assertEquals("Count should be three", 3, sub.getCount());
        assertEquals("Count should be three", 3, main.getCount());
        sub1.inc(2);
        assertEquals("Count should be three", 3, main.getCount());
        assertEquals("Count should be three", 5, sub.getCount());
        assertEquals("Count should be five", 2, sub1.getCount());
    }

    public void testSameMetric() {
        Counter counter = MetricManager.counter(MetricManager.name(this.getClass(), "test-same-counter"), Level.INFO);
        counter.inc();
        assertEquals("Count should be one", 1, counter.getCount());
        Counter counter2 = MetricManager.counter(MetricManager.name(this.getClass(), "test-same-counter"), Level.INFO);
        assertEquals("Count should be one", 1, counter2.getCount());
    }

    public void testSameMetricWithParent() {
        Counter main = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
        Counter sub = MetricManager.counter("org.wso2.main[+].sub.throughput", Level.INFO, Level.INFO);

        Counter main2 = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
        Counter sub2 = MetricManager.counter("org.wso2.main[+].sub.throughput", Level.INFO, Level.INFO);

        sub.inc(5l);
        assertEquals("Count should be five", 5l, sub.getCount());
        assertEquals("Count should be five", 5l, sub2.getCount());
        assertEquals("Count should be five", 5l, main.getCount());
        assertEquals("Count should be five", 5l, main2.getCount());

        main.dec(3l);
        assertEquals("Count should be two", 2l, main.getCount());
        assertEquals("Count should be two", 2l, main2.getCount());
    }

    public void testMetricWithNonExistingParents() {
        Counter sub2 =
                MetricManager.counter("org.wso2.main[+].sub1[+].sub2.throughput", Level.INFO, Level.INFO, Level.INFO);
        Counter sub1 = MetricManager.counter("org.wso2.main[+].sub1.throughput", Level.INFO, Level.INFO);
        Counter main = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
        sub2.inc(5l);
        assertEquals("Count should be five", 5l, sub2.getCount());
        assertEquals("Count should be five", 5l, sub1.getCount());
        assertEquals("Count should be five", 5l, main.getCount());

        sub1.dec(3l);
        assertEquals("Count should be five", 5l, sub2.getCount());
        assertEquals("Count should be two", 2l, sub1.getCount());
        assertEquals("Count should be two", 2l, main.getCount());

        main.inc(10l);
        assertEquals("Count should be five", 5l, sub2.getCount());
        assertEquals("Count should be two", 2l, sub1.getCount());
        assertEquals("Count should be twelve", 12l, main.getCount());
    }

    public void testIncrementByOne() {
        Counter counter = MetricManager.counter(MetricManager.name(this.getClass(), "test-counter-inc1"), Level.INFO);
        counter.inc();
        assertEquals("Count should be one", 1, counter.getCount());

        metricService.setRootLevel(Level.OFF);
        counter.inc();
        assertEquals("Count should be one", 1, counter.getCount());
    }

    public void testIncrementByRandomNumber() {
        Counter counter =
                MetricManager.counter(MetricManager.name(this.getClass(), "test-counter-inc-rand"), Level.INFO);
        int n = randomGenerator.nextInt();
        counter.inc(n);
        assertEquals("Count should be " + n, n, counter.getCount());

        metricService.setRootLevel(Level.OFF);
        counter.inc(n);
        assertEquals("Count should be " + n, n, counter.getCount());
    }

    public void testDecrementByOne() {
        Counter counter = MetricManager.counter(MetricManager.name(this.getClass(), "test-counter-dec1"), Level.INFO);
        counter.dec();
        assertEquals("Count should be -1", -1, counter.getCount());

        metricService.setRootLevel(Level.OFF);
        counter.dec();
        assertEquals("Count should be -1", -1, counter.getCount());
    }

    public void testDecrementByRandomNumber() {
        Counter counter =
                MetricManager.counter(MetricManager.name(this.getClass(), "test-counter-dec-rand"), Level.INFO);
        int n = randomGenerator.nextInt();
        counter.dec(n);
        assertEquals("Count should be " + n, 0 - n, counter.getCount());

        metricService.setRootLevel(Level.OFF);
        counter.dec(n);
        assertEquals("Count should be " + n, 0 - n, counter.getCount());
    }

}
