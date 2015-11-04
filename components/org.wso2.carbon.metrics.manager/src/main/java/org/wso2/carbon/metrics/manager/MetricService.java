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

import java.util.List;
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
     * Get the {@link Level} for a given metric identifier
     * @param identifier The name of the Metric
     * @return {@link Level} for the given metric identifier
     */
    Level getMetricLevel(String identifier);

    /**
     * Get the {@link Level} for a given metric
     * @param name The name of the Metric
     * @param identifier The identifier of the Metric
     * @return {@link Level} for the given metric identifier
     */
    Level getMetricLevel(String name, String identifier);

    /**
     * Set a new level to the given metric identifier
     * 
     * @param identifier The name of the Metric
     * @param level New {@link Level} for the Metric
     */
    void setMetricLevel(String identifier, Level level);

    /**
     * Set a new level to the given metric (name & identifier)
     *
     * @param name The name of the Metric
     * @param identifier The name of the Metric
     * @param level New {@link Level} for the Metric
     */
    void setMetricLevel(String name, String identifier, Level level);

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
     * Get or create a {@link Meter} instance for the given identifier
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param path The annotated path of the metric
     * @param identifier The identifier of the metric
     * @return a {@link Meter} instance
     */
    Meter meter(Level level, String name, String path, String identifier);

    /**
     * Get or create a {@link Counter} instance for the given name
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param path The annotated path of the metric
     * @param identifier The identifier of the metric
     * @return a {@link Counter} instance
     */
    Counter counter(Level level, String name, String path, String identifier);

    /**
     * Get or create a {@link Timer} instance for the given name
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param path The annotated path of the metric
     * @param identifier The identifier of the metric
     * @return a {@link Timer} instance
     */
    Timer timer(Level level, String name, String path, String identifier);

    /**
     * Get or create a {@link Histogram} instance for the given name
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param path The annotated path of the metric
     * @param identifier The identifier of the metric
     * @return a {@link Histogram} instance
     */
    Histogram histogram(Level level, String name, String path, String identifier);

    /**
     * Get or create a {@link Gauge} for the given name
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param path The annotated path of the metric
     * @param identifier The identifier of the metric
     * @param gauge An implementation of {@link Gauge}
     */
    <T> void gauge(Level level, String name, String path, String identifier, Gauge<T> gauge);

    /**
     * Get or create a cached {@link Gauge} for the given name
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param path The annotated path of the metric
     * @param identifier The identifier of the metric
     * @param timeout the timeout
     * @param timeoutUnit the unit of {@code timeout}
     * @param gauge An implementation of {@link Gauge}
     */
    <T> void cachedGauge(Level level, String name, String path, String identifier, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge);

    /**
     * Invoke report method of all scheduled reporters.
     */
    void report();

    /**
     * @return List of affected {@link Metric}s
     */
    List<Metric> getAffectedMetrics(Level level, String name, String path, String identifier);

}
