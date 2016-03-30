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

import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.Timer.Context;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test Cases for {@link Timer}
 */
public class TimerTest extends BaseTest {

    @Test
    public void testInitialCount() {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-initial-count"), Level.INFO);
        assertEquals("Initial count should be zero", 0, timer.getCount());
    }

    @Test
    public void testSameMetric() {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-same-timer"), Level.INFO);
        timer.update(1, TimeUnit.SECONDS);
        assertEquals("Timer count should be one", 1, timer.getCount());

        Timer timer2 = MetricManager.timer(MetricManager.name(this.getClass(), "test-same-timer"), Level.INFO);
        assertEquals("Timer count should be one", 1, timer2.getCount());
    }

    @Test
    public void testTime() {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-timer-start"), Level.INFO);
        Context context = timer.start();
        assertTrue("Timer value should be greater than zero", context.stop() > 0);
        assertEquals("Timer count should be one", 1, timer.getCount());
        context.close();

        metricService.setRootLevel(Level.OFF);
        context = timer.start();
        assertEquals("Timer should not work", 0, context.stop());
        context.close();
    }

    @Test
    public void testTimerUpdateCount() {
        Timer timer = MetricManager.timer(MetricManager.name(this.getClass(), "test-timer-update"), Level.INFO);
        timer.update(1, TimeUnit.SECONDS);
        assertEquals("Timer count should be one", 1, timer.getCount());

        metricService.setRootLevel(Level.OFF);
        timer.update(1, TimeUnit.SECONDS);
        assertEquals("Timer count should be one", 1, timer.getCount());
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
        assertEquals("Value should be 'test'", "test", value);

        metricService.setRootLevel(Level.OFF);
        value = timer.time(callable);
        assertNull("Value should be null", value);
    }

}
