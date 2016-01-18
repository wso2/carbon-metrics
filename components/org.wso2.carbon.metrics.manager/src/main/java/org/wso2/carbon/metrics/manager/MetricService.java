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

import org.wso2.carbon.metrics.manager.exception.MetricNotFoundException;

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
     * @param name The name of the Metric
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
     * Get an existing {@link Meter} instance or {@link Meter}s bundle registered under a given name. If the name is not
     * annotated, it'll return a single {@link Meter} instance. Otherwise it'll return a {@link Meter} bundle. Moreover,
     * if the name is annotated, performing actions (i.e {@link Meter#mark()}) in the returned bundle will result in
     * updating all the {@link Meter}s denoted by the annotated name.
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.cep[+].executionPlan.statName)
     * @return a single {@link Meter} instance or a {@link Meter} bundle.
     */
    Meter getMeter(String name) throws MetricNotFoundException;

    /**
     * Get or create a {@link Meter}s bundle registered under a given annotated name and {@link Level}s. Unlike
     * {@link #getMeter(String)}, this will create the metrics denoted by the annotated name if they do not exists.
     * Moreover, performing actions (i.e {@link Meter#mark()}) in the returned bundle will result in updating
     * all the {@link Meter}s denoted by the annotated name.
     *
     * @param name The annotated name of the metric (i.e org.wso2.cep[+].executionPlan.statName)
     * @param levels The {@link Level}s used for each annotated metric (Number of {@code levels} and Metrics count
     *            should be equal)
     * @return a {@link Meter} bundle which wraps a collection of {@link Meter}s
     */
    Meter meter(String name, Level... levels);

    /**
     * Get an existing {@link Counter} instance or {@link Counter}s bundle registered under a given name. If the name
     * is not annotated, it'll return a single {@link Counter} instance. Otherwise it'll return a {@link Counter}
     * bundle. Moreover, if the name is annotated, performing actions (i.e {@link Counter#inc()}) in the returned
     * bundle will result in updating all the {@link Counter}s denoted by the annotated name.
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.cep[+].executionPlan.statName)
     * @return a single {@link Counter} instance or a {@link Counter} bundle.
     */
    Counter getCounter(String name) throws MetricNotFoundException;

    /**
     * Get or create a {@link Counter}s bundle registered under a given annotated name and {@link Level}s. Unlike
     * {@link #getCounter(String)}, this will create the metrics denoted by the annotated name if they do not exists.
     * Moreover, performing actions (i.e {@link Counter#inc()}) in the returned bundle will result in updating
     * all the {@link Counter}s denoted by the annotated name.
     *
     * @param name The annotated name of the metric (i.e org.wso2.cep[+].executionPlan.statName)
     * @param levels The {@link Level}s used for each annotated metric (Number of {@code levels} and Metrics count
     *            should be equal)
     * @return a {@link Counter} bundle which wraps a collection of {@link Counter}s
     */
    Counter counter(String name, Level... levels);

    /**
     * Get an existing {@link Timer} instance or {@link Timer}s bundle registered under a given name. If the name is not
     * annotated, it'll return a single {@link Timer} instance. Otherwise it'll return a {@link Timer} bundle. Moreover,
     * if the name is annotated, performing actions (i.e {@link Timer#update(long, TimeUnit)}) in the returned bundle
     * will result in updating all the {@link Timer}s denoted by the annotated name.
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.cep[+].executionPlan.statName)
     * @return a single {@link Timer} instance or a {@link Timer} bundle.
     */
    Timer timer(String name) throws MetricNotFoundException;

    /**
     * Get or create a {@link Timer} instance for the given name
     *
     * @param name The name of the metric
     * @param level The {@link Level} used for metric
     * @return a {@link Timer} instance
     */
    Timer timer(String name, Level level);

    /**
     * Get an existing {@link Histogram} instance or {@link Histogram}s bundle registered under a given name. If the
     * name is not annotated, it'll return a single {@link Histogram} instance. Otherwise it'll return a
     * {@link Histogram} bundle. Moreover, if the name is annotated, performing actions
     * (i.e {@link Histogram#update(int)}) in the returned bundle will result in updating all the {@link Histogram}s
     * denoted by the annotated name.
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.cep[+].executionPlan.statName)
     * @return a single {@link Histogram} instance or a {@link Histogram} bundle.
     */
    Histogram getHistogram(String name) throws MetricNotFoundException;

    /**
     * Get or create a {@link Histogram}s bundle registered under a given annotated name and {@link Level}s. Unlike
     * {@link #getHistogram(String)}, this will create the metrics denoted by the annotated name if they do not exists.
     * Moreover, performing actions (i.e {@link Histogram#update(int)}) in the returned bundle will result in updating
     * all the {@link Histogram}s denoted by the annotated name.
     *
     * @param name The annotated name of the metric (i.e org.wso2.cep[+].executionPlan.statName)
     * @param levels The {@link Level}s used for each annotated metric (Number of {@code levels} and Metrics count
     *            should be equal)
     * @return a {@link Histogram} bundle which wraps a collection of {@link Histogram}s
     */
    Histogram histogram(String name, Level... levels);

    /**
     * Get or create a {@link Gauge} for the given name
     *
     * @param name The name of the metric
     * @param level The {@link Level} used for metric
     * @param gauge An implementation of {@link Gauge}
     */
    <T> void gauge(String name, Level level, Gauge<T> gauge);

    /**
     * Get or create a cached {@link Gauge} for the given name
     *
     * @param name The name of the metric
     * @param level The {@link Level} used for metric
     * @param timeout the timeout
     * @param timeoutUnit the unit of {@code timeout}
     * @param gauge An implementation of {@link Gauge}
     */
    <T> void cachedGauge(String name, Level level, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge);

    /**
     * Invoke report method of all scheduled reporters.
     */
    void report();
}
