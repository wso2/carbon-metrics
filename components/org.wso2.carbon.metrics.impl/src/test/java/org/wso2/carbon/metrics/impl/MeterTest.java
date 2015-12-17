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
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

import junit.framework.TestCase;

/**
 * Test Cases for {@link Meter}
 */
public class MeterTest extends TestCase {

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
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-initial-count"), Level.INFO);
        assertEquals("Initial count should be zero", 0, meter.getCount());
        Meter meter2 = MetricManager.meter(MetricManager.name(this.getClass(), "test-initial-count"), Level.INFO);
        assertEquals("Initial count should be zero", 0, meter2.getCount());
    }

    public void testParentCount() {
        Meter main = MetricManager.meter("org.wso2.main.test-meter", Level.INFO);
        Meter sub = MetricManager.meter("org.wso2.main[+].sub.test-meter", Level.INFO, Level.INFO);
        sub.mark(5);
        main.mark(5);
        assertEquals("Count should be five", 5, sub.getCount());
        assertEquals("Count should be ten", 10, main.getCount());
    }

    public void testSameMetric() {
        String name = MetricManager.name(this.getClass(), "test-same-meter");
        Meter meter = MetricManager.meter(name, Level.INFO);
        meter.mark();
        assertEquals("Count should be one", 1, meter.getCount());

        Meter meter2 = MetricManager.meter(name, Level.INFO);
        assertEquals("Count should be one", 1, meter2.getCount());
    }

    public void testSameMetricWithParent() {
        Meter main = MetricManager.meter("org.wso2.main.test-meter", Level.INFO);
        Meter sub = MetricManager.meter("org.wso2.main[+].sub.test-meter", Level.INFO, Level.INFO);

        Meter main2 = MetricManager.meter("org.wso2.main.test-meter", Level.INFO);
        Meter sub2 = MetricManager.meter("org.wso2.main[+].sub.test-meter", Level.INFO, Level.INFO);

        sub.mark();
        assertEquals("Count should be one", 1, sub.getCount());
        assertEquals("Count should be one", 1, sub2.getCount());
        assertEquals("Count should be one", 1, main.getCount());
        assertEquals("Count should be one", 1, main2.getCount());

        main.mark(5);
        assertEquals("Count should be one", 1, sub.getCount());
        assertEquals("Count should be one", 1, sub2.getCount());
        assertEquals("Count should be six", 6, main.getCount());
        assertEquals("Count should be six", 6, main2.getCount());
    }

    public void testMetricWithNonExistingParents() {
        Meter sub2 =
                MetricManager.meter("org.wso2.main[+].sub1[+].sub2.test-meter", Level.INFO, Level.INFO, Level.INFO);
        Meter sub1 = MetricManager.meter("org.wso2.main[+].sub1.test-meter", Level.INFO, Level.INFO);
        Meter main = MetricManager.meter("org.wso2.main.test-meter", Level.INFO);

        sub2.mark();
        assertEquals("Count should be one", 1, sub2.getCount());
        assertEquals("Count should be one", 1, sub1.getCount());
        assertEquals("Count should be one", 1, main.getCount());

        sub1.mark(2);
        assertEquals("Count should be one", 1, sub2.getCount());
        assertEquals("Count should be three", 3, sub1.getCount());
        assertEquals("Count should be three", 3, main.getCount());

        main.mark(2);
        assertEquals("Count should be one", 1, sub2.getCount());
        assertEquals("Count should be three", 3, sub1.getCount());
        assertEquals("Count should be five", 5, main.getCount());
    }

    public void testMarkEvent() {
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-meter-mark"), Level.INFO);
        meter.mark();
        assertEquals("Count should be one", 1, meter.getCount());

        metricService.setRootLevel(Level.OFF);
        meter.mark();
        assertEquals("Count should be one", 1, meter.getCount());
    }

    public void testMarkEventByRandomNumber() {
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-meter-mark-rand"), Level.INFO);
        int n = randomGenerator.nextInt();
        meter.mark(n);
        assertEquals("Count should be " + n, n, meter.getCount());

        metricService.setRootLevel(Level.OFF);
        meter.mark(n);
        assertEquals("Count should be " + n, n, meter.getCount());
    }
}
