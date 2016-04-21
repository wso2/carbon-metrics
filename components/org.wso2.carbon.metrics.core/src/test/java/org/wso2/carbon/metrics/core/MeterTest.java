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
 * Test Cases for {@link Meter}
 */
public class MeterTest extends BaseTest {

    @Test
    public void testInitialCount() {
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-initial-count"), Level.INFO);
        Assert.assertEquals(meter.getCount(), 0);
        Meter meter2 = MetricManager.meter(MetricManager.name(this.getClass(), "test-initial-count"), Level.INFO);
        Assert.assertEquals(meter2.getCount(), 0);
    }

    @Test
    public void testParentCount() {
        Meter main = MetricManager.meter("org.wso2.carbon.metrics.meter.test.events", Level.INFO);
        Meter sub = MetricManager.meter("org.wso2.carbon.metrics.meter.test[+].sub.events", Level.INFO, Level.INFO);
        sub.mark(5);
        main.mark(5);
        Assert.assertEquals(sub.getCount(), 5);
        Assert.assertEquals(main.getCount(), 10);
    }

    @Test
    public void testSameMetric() {
        String name = MetricManager.name(this.getClass(), "test-same-meter");
        Meter meter = MetricManager.meter(name, Level.INFO);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        Meter meter2 = MetricManager.meter(name, Level.INFO);
        Assert.assertEquals(meter2.getCount(), 1);
    }

    @Test
    public void testSameMetricWithParent() {
        Meter main = MetricManager.meter("org.wso2.carbon.metrics.meter.test1.events", Level.INFO);
        Meter sub = MetricManager.meter("org.wso2.carbon.metrics.meter.test1[+].sub.events", Level.INFO, Level.INFO);

        Meter main2 = MetricManager.meter("org.wso2.carbon.metrics.meter.test1.events", Level.INFO);
        Meter sub2 = MetricManager.meter("org.wso2.carbon.metrics.meter.test1[+].sub.events", Level.INFO, Level.INFO);

        sub.mark();
        Assert.assertEquals(sub.getCount(), 1);
        Assert.assertEquals(sub2.getCount(), 1);
        Assert.assertEquals(main.getCount(), 1);
        Assert.assertEquals(main2.getCount(), 1);


        main.mark(5);
        Assert.assertEquals(sub.getCount(), 1);
        Assert.assertEquals(sub2.getCount(), 1);
        Assert.assertEquals(main.getCount(), 6);
        Assert.assertEquals(main2.getCount(), 6);
    }

    @Test
    public void testMetricWithNonExistingParents() {
        Meter sub2 =
                MetricManager.meter("org.wso2.carbon.metrics.meter.test2[+].sub1[+].sub2.events", Level.INFO,
                        Level.INFO, Level.INFO);
        Meter sub1 = MetricManager.meter("org.wso2.carbon.metrics.meter.test2[+].sub1.events", Level.INFO, Level.INFO);
        Meter main = MetricManager.meter("org.wso2.carbon.metrics.meter.test2.events", Level.INFO);

        sub2.mark();
        Assert.assertEquals(sub2.getCount(), 1);
        Assert.assertEquals(sub1.getCount(), 1);
        Assert.assertEquals(main.getCount(), 1);

        sub1.mark(2);
        Assert.assertEquals(sub2.getCount(), 1);
        Assert.assertEquals(sub1.getCount(), 3);
        Assert.assertEquals(main.getCount(), 3);

        main.mark(2);
        Assert.assertEquals(sub2.getCount(), 1);
        Assert.assertEquals(sub1.getCount(), 3);
        Assert.assertEquals(main.getCount(), 5);
    }

    @Test
    public void testMarkEvent() {
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-meter-mark"), Level.INFO);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricService.setRootLevel(Level.OFF);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);
    }

    @Test
    public void testMarkEventByRandomNumber() {
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-meter-mark-rand"), Level.INFO);
        int n = random.nextInt();
        meter.mark(n);
        Assert.assertEquals(meter.getCount(), n);

        metricService.setRootLevel(Level.OFF);
        meter.mark(n);
        Assert.assertEquals(meter.getCount(), n);
    }

    @Test
    public void testEventRate() {
        Meter meter = MetricManager.meter(MetricManager.name(this.getClass(), "test-meter-rate"), Level.INFO);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);
        Assert.assertTrue(meter.getOneMinuteRate() >= 0);
        Assert.assertTrue(meter.getFiveMinuteRate() >= 0);
        Assert.assertTrue(meter.getFifteenMinuteRate() >= 0);
        Assert.assertTrue(meter.getMeanRate() >= 0);
    }

    @Test
    public void testMetricCollectionEventRate() {
        Meter meter = MetricManager.meter("org.wso2.carbon.metrics.meter.test3[+].sub.events", Level.INFO,
                Level.INFO);

        meter.mark();
        Assert.assertTrue(meter.getOneMinuteRate() >= 0);
        Assert.assertTrue(meter.getFiveMinuteRate() >= 0);
        Assert.assertTrue(meter.getFifteenMinuteRate() >= 0);
        Assert.assertTrue(meter.getMeanRate() >= 0);
    }
}
