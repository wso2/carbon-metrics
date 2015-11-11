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

import junit.framework.TestCase;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.internal.MetricServiceValueHolder;
import org.wso2.carbon.metrics.manager.*;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

import java.util.Random;

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
        MetricServiceValueHolder.registerMetricServiceInstance(metricService);
    }

    public void testMetricHierarchy() {
        Counter sub = MetricManager.counter(Level.INFO, "org.wso2.main.sub", "org.wso2.main[+].sub", "throughput");
        Counter sub1 = MetricManager.counter(Level.INFO, "org.wso2.main.sub.sub1", "org.wso2.main.sub[+].sub1", "throughput");
        Counter main = MetricManager.counter(Level.INFO, "org.wso2.main", "throughput");
        sub.inc(3);

        MetricHierarchy hierarchy = MetricManager.metricHierarchy();
        for (MetricHierarchy node : hierarchy) {
            String indent = createIndent(node.getLevel());
            System.out.println(indent + node);
        }
        System.out.println("");
    }

    private static String createIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append('\t');
        }
        return sb.toString();
    }


    public void testInitialCount() {
        Counter counter = MetricManager.counter(Level.INFO, MetricManager.name(this.getClass()), "test-counter");
        assertEquals("Initial count should be zero", 0, counter.getCount());
    }

    public void testParentCount() {
        Counter main = MetricManager.counter(Level.INFO, "org.wso2.main", "throughput");
        Counter sub = MetricManager.counter(Level.INFO, "org.wso2.main.sub", "org.wso2.main[+].sub", "throughput");
        sub.inc(5);
        main.dec(3);
        assertEquals("Count should be five", 5, sub.getCount());
        assertEquals("Count should be two", 2, main.getCount());
    }

    public void testParentCount2() {
        Counter sub = MetricManager.counter(Level.INFO, "org.wso2.main.sub", "org.wso2.main[+].sub", "throughput");
        Counter sub1 = MetricManager.counter(Level.INFO, "org.wso2.main.sub.sub1", "org.wso2.main.sub[+].sub1", "throughput");
        Counter main = MetricManager.counter(Level.INFO, "org.wso2.main", "throughput");
        sub.inc(3);
        assertEquals("Count should be three", 3, sub.getCount());
        assertEquals("Count should be three", 3, main.getCount());
        sub1.inc(2);
        assertEquals("Count should be three", 3, main.getCount());
        assertEquals("Count should be three", 5, sub.getCount());
        assertEquals("Count should be five", 2, sub1.getCount());
    }

    public void testSameMetric() {
        String name = MetricManager.name(this.getClass());
        Counter counter = MetricManager.counter(Level.INFO, name, "test-same-counter");
        counter.inc();
        assertEquals("Count should be one", 1, counter.getCount());
        Counter counter2 = MetricManager.counter(Level.INFO, name, "test-same-counter");
        assertEquals("Count should be one", 1, counter2.getCount());
    }

    public void testSameMetricWithParent() {
        Counter main = MetricManager.counter(Level.INFO, "org.wso2.main", "throughput");
        Counter sub = MetricManager.counter(Level.INFO, "org.wso2.main.sub", "org.wso2.main[+].sub", "throughput");

        Counter main2 = MetricManager.counter(Level.INFO, "org.wso2.main", "throughput");
        Counter sub2 = MetricManager.counter(Level.INFO, "org.wso2.main.sub", "org.wso2.main[+].sub", "throughput");

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
        Counter sub2 = MetricManager.counter(Level.INFO, "org.wso2.main.sub1.sub2", "org.wso2.main[+].sub1[+].sub2", "throughput");
        Counter sub1 = MetricManager.counter(Level.INFO, "org.wso2.main.sub1", "org.wso2.main[+].sub1", "throughput");
        Counter main = MetricManager.counter(Level.INFO, "org.wso2.main", "throughput");
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
        Counter counter = MetricManager.counter(Level.INFO, MetricManager.name(this.getClass()), "test-counter-inc1");
        counter.inc();
        assertEquals("Count should be one", 1, counter.getCount());

        metricService.setRootLevel(Level.OFF);
        counter.inc();
        assertEquals("Count should be one", 1, counter.getCount());
    }

    public void testIncrementByRandomNumber() {
        Counter counter = MetricManager.counter(Level.INFO, MetricManager.name(this.getClass()), "test-counter-inc-rand");
        int n = randomGenerator.nextInt();
        counter.inc(n);
        assertEquals("Count should be " + n, n, counter.getCount());

        metricService.setRootLevel(Level.OFF);
        counter.inc(n);
        assertEquals("Count should be " + n, n, counter.getCount());
    }

    public void testDecrementByOne() {
        Counter counter = MetricManager.counter(Level.INFO, MetricManager.name(this.getClass()), "test-counter-dec1");
        counter.dec();
        assertEquals("Count should be -1", -1, counter.getCount());

        metricService.setRootLevel(Level.OFF);
        counter.dec();
        assertEquals("Count should be -1", -1, counter.getCount());
    }

    public void testDecrementByRandomNumber() {
        Counter counter = MetricManager.counter(Level.INFO, MetricManager.name(this.getClass()), "test-counter-dec-rand");
        int n = randomGenerator.nextInt();
        counter.dec(n);
        assertEquals("Count should be " + n, 0 - n, counter.getCount());

        metricService.setRootLevel(Level.OFF);
        counter.dec(n);
        assertEquals("Count should be " + n, 0 - n, counter.getCount());
    }

}
