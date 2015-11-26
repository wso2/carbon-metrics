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
package org.wso2.carbon.metrics.manager;

import java.util.concurrent.TimeUnit;

/**
 * Main interface for the service creating various metrics
 */
public interface MetricService {

    /**
     * Enables the Metrics Feature
     */
    void enable();

    /**
     * Disables the Metrics Feature
     */
    void disable();

    /**
     * Get the current status of Metrics (Enabled/Disabled)
     *
     * @return {@code true} if the Metrics feature is enabled
     */
    boolean isEnabled();

    /**
     * Get the {@link Level} for a given metric
     *
     * @param name The name of the Metric
     * @return {@link Level} for the given metric
     */
    Level getMetricLevel(String name);

    /**
     * Set a new level to the given metric
     *
     * @param name  The name of the Metric
     * @param level New {@link Level} for the Metric
     */
    void setMetricLevel(String name, Level level);

    /**
     * @return The current root {@link Level}
     */
    Level getRootLevel();

    /**
     * Set a new root level to the Metrics Service
     *
     * @param level New Root {@link Level}
     */
    void setRootLevel(Level level);

    /**
     * Return the number of metrics used
     *
     * @return The metrics count
     */
    int getMetricsCount();

    /**
     * Get the {@link Meter} instance for the given metric name
     *
     * @param name The name of the metric (name can be annotated)
     * @return a {@link Meter} instance
     */
    Meter meter(String name);

    /**
     * Get or create a {@link Meter} instance for the given annotated name
     *
     * @param name   The annotated name of the metric
     * @param levels The {@link Level}s used for each annotated metric
     * @return a {@link Meter} instance (which wraps a collection of {@link Meter}s)
     */
    Meter meter(String name, Level... levels);

    /**
     * Get the {@link Counter} instance for the given metric name
     *
     * @param name The name of the metric (name can be annotated)
     * @return a {@link Counter} instance
     */
    Counter counter(String name);

    /**
     * Get or create a {@link Counter} instance for the given annotated name
     *
     * @param name   The annotated name of the metric
     * @param levels The {@link Level}s used for each annotated metric
     * @return a {@link Counter} instance (which wraps a collection of {@link Counter}s)
     */
    Counter counter(String name, Level... levels);

    /**
     * Get the {@link Timer} instance for the given metric name
     *
     * @param name The name of the metric (name can be annotated)
     * @return a {@link Timer} instance
     */
    Timer timer(String name);

    /**
     * Get or create a {@link Timer} instance for the given annotated name
     *
     * @param name   The annotated name of the metric
     * @param levels The {@link Level}s used for each annotated metric
     * @return a {@link Timer} instance (which wraps a collection of {@link Timer}s)
     */
    Timer timer(String name, Level... levels);

    /**
     * Get the {@link Histogram} instance for the given metric name
     *
     * @param name The name of the metric (name can be annotated)
     * @return a {@link Histogram} instance
     */
    Histogram histogram(String name);

    /**
     * Get or create a {@link Histogram} instance for the given annotated name
     *
     * @param name   The annotated name of the metric
     * @param levels The {@link Level}s used for each annotated metric
     * @return a {@link Histogram} instance (which wraps a collection of {@link Histogram}s)
     */
    Histogram histogram(String name, Level... levels);

    /**
     * Get or create a {@link Gauge} for the given name
     *
     * @param name  The name of the metric
     * @param level The {@link Level} used for metric
     * @param gauge An implementation of {@link Gauge}
     */
    <T> void gauge(String name, Level level, Gauge<T> gauge);

    /**
     * Get or create a cached {@link Gauge} for the given name
     *
     * @param name        The name of the metric
     * @param level       The {@link Level} used for metric
     * @param timeout     the timeout
     * @param timeoutUnit the unit of {@code timeout}
     * @param gauge       An implementation of {@link Gauge}
     */
    <T> void cachedGauge(String name, Level level, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge);

    /**
     * Invoke report method of all scheduled reporters.
     */
    void report();

    /**
     * @return The generated {@link MetricHierarchy}
     */
    MetricHierarchy getMetricHierarchy();

    /**
     * @return The generated {@link MetricHierarchy} for a given path
     */
    MetricHierarchy getMetricHierarchy(String path);

}
