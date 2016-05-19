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
import org.wso2.carbon.metrics.core.Timer.Context;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

/**
 * Test Cases for {@link Timer}
 */
public class TimerTest extends BaseMetricTest {

    @Test
    public void testInitialCount() {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-initial-count"), Level.INFO);
        Assert.assertEquals(timer.getCount(), 0);
    }

    @Test
    public void testSameMetric() {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-same-timer"), Level.INFO);
        timer.update(1, TimeUnit.SECONDS);
        Assert.assertEquals(timer.getCount(), 1);

        Timer timer2 = MetricManager.timer(MetricManager.name(this.getClass(), "test-same-timer"), Level.INFO);
        Assert.assertEquals(timer2.getCount(), 1);
        timer.update(1, TimeUnit.SECONDS);

        try {
            Timer timer3 = MetricManager.getTimer(MetricManager.name(this.getClass(), "test-same-timer"));
            Assert.assertEquals(timer3.getCount(), 2);
        } catch (MetricNotFoundException e) {
            Assert.fail("Timer should be available", e);
        }
    }

    @Test
    public void testTime() {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-timer-start"), Level.INFO);
        Context context = timer.start();
        Assert.assertTrue(context.stop() > 0, "Timer value should be greater than zero");
        Assert.assertEquals(timer.getCount(), 1);
        context.close();

        MetricManager.getMetricService().setRootLevel(Level.OFF);
        context = timer.start();
        Assert.assertEquals(context.stop(), 0);
        context.close();
    }

    @Test
    public void testTimerUpdateCount() {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-timer-update"), Level.INFO);
        timer.update(1, TimeUnit.SECONDS);
        Assert.assertEquals(timer.getCount(), 1);

        MetricManager.getMetricService().setRootLevel(Level.OFF);
        timer.update(1, TimeUnit.SECONDS);
        Assert.assertEquals(timer.getCount(), 1);
    }

    @Test
    public void testTimerCallableInstances() throws Exception {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-timer-callable"), Level.INFO);
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "test";
            }

        };
        String value = timer.time(callable);
        Assert.assertEquals(value, "test");

        MetricManager.getMetricService().setRootLevel(Level.OFF);
        value = timer.time(callable);
        Assert.assertNull(value, "Value should be null");
    }

    @Test
    public void testSnapshot() {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-timer-callable"), Level.INFO);

        LongStream.rangeClosed(1, 100).forEach(i -> timer.update(i, TimeUnit.NANOSECONDS));

        Snapshot snapshot = timer.getSnapshot();
        testSnapshot(snapshot);
    }

    @Test
    public void testEventRate() {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-timer-rate"), Level.INFO);
        timer.update(1L, TimeUnit.NANOSECONDS);
        Assert.assertEquals(timer.getCount(), 1);
        Assert.assertTrue(timer.getOneMinuteRate() >= 0);
        Assert.assertTrue(timer.getFiveMinuteRate() >= 0);
        Assert.assertTrue(timer.getFifteenMinuteRate() >= 0);
        Assert.assertTrue(timer.getMeanRate() >= 0);
    }

}
