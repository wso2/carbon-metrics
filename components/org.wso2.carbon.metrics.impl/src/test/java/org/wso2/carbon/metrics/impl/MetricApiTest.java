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
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.internal.MetricServiceValueHolder;
import org.wso2.carbon.metrics.manager.*;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

import java.util.NoSuchElementException;

/**
 * Test Cases for {@link Counter}
 */
public class MetricApiTest extends TestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MetricService metricService;

    protected void setUp() throws Exception {
        super.setUp();
        MetricsConfiguration configuration = Utils.getConfiguration();
        MetricsLevelConfiguration levelConfiguration = Utils.getLevelConfiguration();
        metricService = new MetricServiceImpl.Builder().configure(configuration).build(levelConfiguration);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
        MetricServiceValueHolder.registerMetricServiceInstance(metricService);
    }

    public void testCreateSingleCounter() {
        // create new counter
        Counter counter1 = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
        // retrieve created counter
        Counter counter2 = MetricManager.counter("org.wso2.main.throughput");
        // get or create counter
        Counter counter3 = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
        counter1.inc();
        assertEquals("Count should be one", 1, counter1.getCount());
        assertEquals("Count should be one", 1, counter2.getCount());
        assertEquals("Count should be one", 1, counter3.getCount());
    }

    public void testCreateMultipleCounters() {
        // create new counters
        Counter subCounterCollection1 = MetricManager.counter("org.wso2.main[+].sub.throughput", Level.INFO, Level.INFO);
        // retrieve created counters
        Counter subCounterCollection2 = MetricManager.counter("org.wso2.main[+].sub.throughput");
        Counter subCounter1 = MetricManager.counter("org.wso2.main.sub.throughput");
        Counter mainCounter1 = MetricManager.counter("org.wso2.main.throughput");
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
    }
//
//    public void testGetUndefinedCounter() {
//        thrown.expect(NoSuchElementException.class);
//        Counter counter = MetricManager.counter("org.wso2.main.throughput");
//    }
//
//    public void testGetWrongMetricType() {
//        Counter counter = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
//        thrown.expect(IllegalArgumentException.class);
//        Meter meter = MetricManager.meter("org.wso2.main.throughput");
//    }
//
//    public void testAnnotatedNameToCreateSingleMetric() {
//        thrown.expect(IllegalArgumentException.class);
//        Counter counter = MetricManager.counter("org.wso2.main[+].sub.throughput", Level.INFO);
//    }
//
//    public void testGetMetricWithDifferentLevel() {
//        Counter counter1 = MetricManager.counter("org.wso2.main.throughput", Level.INFO);
//        thrown.expect(IllegalArgumentException.class);
//        Counter counter2 = MetricManager.counter("org.wso2.main.throughput", Level.DEBUG);
//    }

}
