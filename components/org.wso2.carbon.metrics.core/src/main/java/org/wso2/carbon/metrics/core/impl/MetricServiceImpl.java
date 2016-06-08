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
package org.wso2.carbon.metrics.core.impl;

import org.wso2.carbon.metrics.core.Counter;
import org.wso2.carbon.metrics.core.Gauge;
import org.wso2.carbon.metrics.core.Histogram;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MetricNotFoundException;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Timer;

import java.util.concurrent.TimeUnit;

/**
 * The main implementation for {@link MetricService}.
 */
public class MetricServiceImpl implements MetricService {

    private final MetricManager metricManager;

    public MetricServiceImpl(MetricManager metricManager) {
        this.metricManager = metricManager;
    }

    @Override
    public Counter counter(String name) throws MetricNotFoundException {
        return metricManager.counter(name);
    }

    @Override
    public Counter counter(String name, Level level, Level... levels) {
        return metricManager.counter(name, level, levels);
    }

    @Override
    public Meter meter(String name) throws MetricNotFoundException {
        return metricManager.meter(name);
    }

    @Override
    public Meter meter(String name, Level level, Level... levels) {
        return metricManager.meter(name, level, levels);
    }

    @Override
    public Histogram histogram(String name) throws MetricNotFoundException {
        return metricManager.histogram(name);
    }

    @Override
    public Histogram histogram(String name, Level level, Level... levels) {
        return metricManager.histogram(name, level, levels);
    }

    @Override
    public Timer timer(String name) throws MetricNotFoundException {
        return metricManager.timer(name);
    }

    @Override
    public Timer timer(String name, Level level) {
        return metricManager.timer(name, level);
    }

    @Override
    public <T> void gauge(String name, Level level, Gauge<T> gauge) {
        metricManager.gauge(name, level, gauge);
    }

    @Override
    public <T> void cachedGauge(String name, Level level, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        metricManager.cachedGauge(name, level, timeout, timeoutUnit, gauge);
    }

    @Override
    public <T> void cachedGauge(String name, Level level, long timeout, Gauge<T> gauge) {
        metricManager.cachedGauge(name, level, timeout, TimeUnit.SECONDS, gauge);
    }
}
