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

import java.util.concurrent.TimeUnit;

/**
 * Test Cases for {@link Gauge}
 */
public class GaugeTest extends BaseMetricTest {

    @Test
    public void testSameMetric() {
        String name = MetricManager.name(this.getClass(), "test-same-gauge");

        Gauge<Integer> gauge = () -> 1;

        MetricManager.gauge(name, Level.INFO, gauge);

        // This call also should be successful as we are getting the same gauge
        MetricManager.gauge(name, Level.INFO, gauge);
    }

    @Test
    public void testSameCachedMetric() {
        String name = MetricManager.name(this.getClass(), "test-same-cached-gauge");

        Gauge<Integer> gauge = () -> 1;

        MetricManager.cachedGauge(name, Level.INFO, 5, gauge);

        MetricManager.cachedGauge(name, Level.INFO, 5, TimeUnit.SECONDS, gauge);
    }

}
