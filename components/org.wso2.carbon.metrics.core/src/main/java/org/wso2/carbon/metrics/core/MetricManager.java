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

import org.wso2.carbon.metrics.core.impl.MetricService;

import java.util.concurrent.TimeUnit;

/**
 * MetricManager is a static utility class providing various metrics.
 */
public final class MetricManager {

    private MetricManager() {
    }

    /**
     * Concatenates elements to form a dotted name
     *
     * @param name  the first element of the name
     * @param names the remaining elements of the name
     * @return {@code name} and {@code names} concatenated by periods
     */
    public static String name(String name, String... names) {
        final StringBuilder builder = new StringBuilder();
        append(builder, name);
        if (names != null) {
            for (String s : names) {
                append(builder, s);
            }
        }
        return builder.toString();
    }

    /**
     * Concatenates a class name and elements to form a dotted name
     *
     * @param klass the first element of the name
     * @param names the remaining elements of the name
     * @return {@code klass} and {@code names} concatenated by periods
     */
    public static String name(Class<?> klass, String... names) {
        return name(klass.getName(), names);
    }

    private static void append(StringBuilder builder, String part) {
        if (part != null && !part.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(part);
        }
    }

    /**
     * <p>Get an existing {@link Meter} instance or {@link Meter}s bundle registered under a given name.</p>
     *
     * @param name The name of the metric (This name can be annotated. eg. org.wso2.parent[+].child.metric)
     * @return a single {@link Meter} instance or a {@link Meter} bundle.
     * @throws MetricNotFoundException when there is no {@link Meter} for the given name.
     * @see #meter(String, Level, Level...)
     */
    public static Meter getMeter(String name) throws MetricNotFoundException {
        return MetricService.getInstance().getMeter(name);
    }

    /**
     * <p>Get or create a {@link Meter} instance or a {@link Meter} bundle registered under given name.</p> <p>The name
     * must be annotated with "[+]" to get or create a {@link Meter} bundle. The number of {@link Level}s must match the
     * number of {@link Meter}s in the {@link Meter} bundle. In a {@link Meter} bundle, any action performed will be
     * delegated to the {@link Meter}s denoted by the annotated name. eg:</p>
     * <pre>
     *     Meter m = MetricManager.meter("org.wso2.parent[+].child.metric", Level.INFO, Level.INFO);
     *     m.mark();
     * </pre>
     * <p>Above example will get or create a  {@link Meter} bundle with two {@link Meter}s registered under
     * org.wso2.parent.metric and org.wso2.parent.child.metric with {@link Level#INFO}.</p>
     *
     * @param name   The name of the metric (This name can be annotated to get or create a {@link Meter} bundle. eg.
     *               org.wso2.parent[+].child.metric)
     * @param level  The {@link Level} used for the metric
     * @param levels The additional {@link Level}s used for each annotated metric (The total number of {@link Level}s
     *               and the metrics in a bundle should be equal)
     * @return a {@link Meter} or a {@link Meter} bundle if the name is annotated
     * @see #getMeter(String)
     */
    public static Meter meter(String name, Level level, Level... levels) {
        return MetricService.getInstance().meter(name, level, levels);
    }

    /**
     * <p>Get an existing {@link Counter} instance or {@link Counter}s bundle registered under a given name.</p>
     *
     * @param name The name of the metric (This name can be annotated. eg. org.wso2.parent[+].child.metric)
     * @return a single {@link Counter} instance or a {@link Counter} bundle.
     * @throws MetricNotFoundException when there is no {@link Counter} for the given name.
     * @see #counter(String, Level, Level...)
     */
    public static Counter getCounter(String name) throws MetricNotFoundException {
        return MetricService.getInstance().getCounter(name);
    }

    /**
     * <p>Get or create a {@link Counter} instance or a {@link Counter} bundle registered under given name.</p> <p>The
     * name must be annotated with "[+]" to get or create a {@link Counter} bundle. The number of {@link Level}s must
     * match the number of {@link Counter}s in the {@link Counter} bundle. In a {@link Counter} bundle, any action
     * performed will be delegated to the {@link Counter}s denoted by the annotated name. eg:</p>
     * <pre>
     *     Counter c = MetricManager.counter("org.wso2.parent[+].child.metric", Level.INFO, Level.INFO);
     *     c.inc();
     * </pre>
     * <p>Above example will get or create a  {@link Counter} bundle with two {@link Counter}s registered under
     * org.wso2.parent.metric and org.wso2.parent.child.metric with {@link Level#INFO}.</p>
     *
     * @param name   The name of the metric (This name can be annotated to get or create a {@link Counter} bundle. eg.
     *               org.wso2.parent[+].child.metric)
     * @param level  The {@link Level} used for the metric
     * @param levels The additional {@link Level}s used for each annotated metric (The total number of {@link Level}s
     *               and the metrics in a bundle should be equal)
     * @return a {@link Counter} or a {@link Counter} bundle if the name is annotated
     * @see #getCounter(String)
     */
    public static Counter counter(String name, Level level, Level... levels) {
        return MetricService.getInstance().counter(name, level, levels);
    }

    /**
     * <p>Get the {@link Timer} instance registered under given name.</p>
     *
     * @param name The name of the metric
     * @return a {@link Timer} instance
     * @throws MetricNotFoundException when there is no  {@link Timer} for the given name.
     * @see #timer(String, Level)
     */
    public static Timer getTimer(String name) throws MetricNotFoundException {
        return MetricService.getInstance().getTimer(name);
    }

    /**
     * <p>Get or create a {@link Timer} instance registered under given name.</p>
     *
     * @param name  The name of the metric
     * @param level The {@link Level} used for metric
     * @return a {@link Timer} instance
     * @see #getTimer(String)
     */
    public static Timer timer(String name, Level level) {
        return MetricService.getInstance().timer(name, level);
    }

    /**
     * <p>Get an existing {@link Histogram} instance or {@link Histogram}s bundle registered under a given name.</p>
     *
     * @param name The name of the metric (This name can be annotated. eg. org.wso2.parent[+].child.metric)
     * @return a single {@link Histogram} instance or a {@link Histogram} bundle.
     * @throws MetricNotFoundException when there is no {@link Histogram} for the given name.
     * @see #histogram(String, Level, Level...)
     */
    public static Histogram getHistogram(String name) throws MetricNotFoundException {
        return MetricService.getInstance().getHistogram(name);
    }

    /**
     * <p>Get or create a {@link Histogram} instance or a {@link Histogram} bundle registered under given name.</p>
     * <p>The name must be annotated with "[+]" to get or create a {@link Histogram} bundle. The number of {@link
     * Level}s must match the number of {@link Histogram}s in the {@link Histogram} bundle. In a {@link Histogram}
     * bundle, any action performed will be delegated to the {@link Histogram}s denoted by the annotated name. eg:</p>
     * <pre>
     *     Histogram c = MetricManager.histogram("org.wso2.parent[+].child.metric", Level.INFO, Level.INFO);
     *     c.update(5);
     * </pre>
     * <p>Above example will get or create a  {@link Histogram} bundle with two {@link Histogram}s registered under
     * org.wso2.parent.metric and org.wso2.parent.child.metric with {@link Level#INFO}.</p>
     *
     * @param name   The name of the metric (This name can be annotated to get or create a {@link Histogram} bundle. eg.
     *               org.wso2.parent[+].child.metric)
     * @param level  The {@link Level} used for the metric
     * @param levels The additional {@link Level}s used for each annotated metric (The total number of {@link Level}s
     *               and the metrics in a bundle should be equal)
     * @return a {@link Histogram} or a {@link Histogram} bundle if the name is annotated
     * @see #getHistogram(String)
     */
    public static Histogram histogram(String name, Level level, Level... levels) {
        return MetricService.getInstance().histogram(name, level, levels);
    }

    /**
     * Register a {@link Gauge} instance under given name
     *
     * @param <T>   The type of the value used in the {@link Gauge}
     * @param name  The name of the metric
     * @param level The {@link Level} used for metric
     * @param gauge An implementation of {@link Gauge}
     * @see #cachedGauge(String, Level, long, Gauge)
     * @see #cachedGauge(String, Level, long, TimeUnit, Gauge)
     */
    public static <T> void gauge(String name, Level level, Gauge<T> gauge) {
        MetricService.getInstance().gauge(name, level, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout
     *
     * @param <T>         The type of the value used in the {@link Gauge}
     * @param name        The name of the metric
     * @param level       The {@link Level} used for metric
     * @param timeout     The timeout value
     * @param timeoutUnit The {@link TimeUnit} for the timeout
     * @param gauge       An implementation of {@link Gauge}
     * @see #gauge(String, Level, Gauge)
     * @see #cachedGauge(String, Level, long, Gauge)
     */
    public static <T> void cachedGauge(String name, Level level, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        MetricService.getInstance().cachedGauge(name, level, timeout, timeoutUnit, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout in seconds
     *
     * @param <T>     The type of the value used in the {@link Gauge}
     * @param name    The name of the metric
     * @param level   The {@link Level} used for metric
     * @param timeout The timeout value in seconds
     * @param gauge   An implementation of {@link Gauge}
     * @see #gauge(String, Level, Gauge)
     * @see #cachedGauge(String, Level, long, TimeUnit, Gauge)
     */
    public static <T> void cachedGauge(String name, Level level, long timeout, Gauge<T> gauge) {
        MetricService.getInstance().cachedGauge(name, level, timeout, TimeUnit.SECONDS,
                gauge);
    }

}
