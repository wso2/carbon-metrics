/*
 * Copyright 2016 WSO2 Inc. (http://wso2.org)
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
import org.wso2.carbon.metrics.core.annotation.Counted;
import org.wso2.carbon.metrics.core.annotation.Level;
import org.wso2.carbon.metrics.core.annotation.Metered;
import org.wso2.carbon.metrics.core.annotation.Timed;

import java.lang.reflect.Method;

/**
 * Test Cases for {@link MetricAnnotation}.
 */
public class MetricAnnotationTest extends BaseMetricTest {

    @Counted
    public void counter1() {
    }

    @Counted(name = "test-counter")
    public void counter2() {
    }

    @Counted(name = "test-counter", absolute = true)
    public void counter3() {
    }

    @Counted(name = "test-off-counter", absolute = true, level = Level.OFF)
    public void counter4() {
    }

    @Counted(name = "test-trace-counter", absolute = true, level = Level.TRACE)
    public void counter5() {
    }

    @Counted(name = "test-debug-counter", absolute = true, level = Level.DEBUG)
    public void counter6() {
    }

    @Counted(name = "test-info-counter", absolute = true, level = Level.INFO)
    public void counter7() {
    }

    @Counted(name = "test-all-counter", absolute = true, level = Level.ALL)
    public void counter8() {
    }

    @Metered(name = "test-meter")
    public void meter() {
    }

    @Timed(name = "test-timer")
    public void timer() {
    }

    private void checkName(Method method, String name) throws MetricNotFoundException {
        Assert.assertTrue(method.isAnnotationPresent(Counted.class));
        Counted annotation = method.getAnnotation(Counted.class);
        MetricAnnotation.counter(metricService, annotation, method);
        Assert.assertNotNull(metricService.counter(name));
    }

    private void checkLevel(Method method, String name, org.wso2.carbon.metrics.core.Level level) {
        Assert.assertTrue(method.isAnnotationPresent(Counted.class));
        Counted annotation = method.getAnnotation(Counted.class);
        MetricAnnotation.counter(metricService, annotation, method);
        Assert.assertNotNull(metricService.counter(name, level));
    }

    @Test
    public void testCounterDefaultLevel() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("counter1");
        Counted annotation = method.getAnnotation(Counted.class);
        // Test default Level and Level.valueOf() method
        Assert.assertEquals(annotation.level(), Level.valueOf("INFO"));
    }

    @Test
    public void testCounter1() throws NoSuchMethodException, MetricNotFoundException {
        Method method = this.getClass().getMethod("counter1");
        checkName(method, MetricService.name(method.getDeclaringClass().getName(), method.getName()));
    }

    @Test
    public void testCounter2() throws NoSuchMethodException, MetricNotFoundException {
        Method method = this.getClass().getMethod("counter2");
        checkName(method, MetricService.name(method.getDeclaringClass().getName(), method.getName(), "test-counter"));
    }

    @Test
    public void testCounter3() throws NoSuchMethodException, MetricNotFoundException {
        Method method = this.getClass().getMethod("counter3");
        checkName(method, "test-counter");
    }

    @Test
    public void testCounter4() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("counter4");
        checkLevel(method, "test-off-counter", org.wso2.carbon.metrics.core.Level.OFF);
    }

    @Test
    public void testCounter5() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("counter5");
        checkLevel(method, "test-trace-counter", org.wso2.carbon.metrics.core.Level.TRACE);
    }

    @Test
    public void testCounter6() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("counter6");
        checkLevel(method, "test-debug-counter", org.wso2.carbon.metrics.core.Level.DEBUG);
    }

    @Test
    public void testCounter7() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("counter7");
        checkLevel(method, "test-info-counter", org.wso2.carbon.metrics.core.Level.INFO);
    }

    @Test
    public void testCounter8() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("counter8");
        checkLevel(method, "test-all-counter", org.wso2.carbon.metrics.core.Level.ALL);
    }

    @Test
    public void testMeter() throws NoSuchMethodException, MetricNotFoundException {
        Method method = this.getClass().getMethod("meter");
        Assert.assertTrue(method.isAnnotationPresent(Metered.class));
        Metered annotation = method.getAnnotation(Metered.class);
        MetricAnnotation.meter(metricService, annotation, method);
        Assert.assertNotNull(metricService.meter(
                MetricService.name(method.getDeclaringClass().getName(), method.getName(), "test-meter")));
    }

    @Test
    public void testTimer() throws NoSuchMethodException, MetricNotFoundException {
        Method method = this.getClass().getMethod("timer");
        Assert.assertTrue(method.isAnnotationPresent(Timed.class));
        Timed annotation = method.getAnnotation(Timed.class);
        MetricAnnotation.timer(metricService, annotation, method);
        Assert.assertNotNull(metricService.timer(
                MetricService.name(method.getDeclaringClass().getName(), method.getName(), "test-timer")));
    }
}
