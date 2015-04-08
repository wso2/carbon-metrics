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

import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

/**
 * MetricManager is a static utility class providing various metrics.
 */
public final class MetricManager {

    private MetricManager() {
    }

    /**
     * Concatenates elements to form a dotted name
     *
     * @param name the first element of the name
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
     * Return a {@link Meter} instance registered under given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Meter} instance
     */
    public static Meter meter(Level level, String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().meter(level, name);
    }

    /**
     * Return a {@link Counter} instance registered under given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Counter} instance
     */
    public static Counter counter(Level level, String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().counter(level, name);
    }

    /**
     * Return a {@link Timer} instance registered under given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Timer} instance
     */
    public static Timer timer(Level level, String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().timer(level, name);
    }

    /**
     * Return a {@link Histogram} instance registered under given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Histogram} instance
     */
    public static Histogram histogram(Level level, String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().histogram(level, name);
    }

    /**
     * Register a {@link Gauge} instance under given name
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param gauge An implementation of {@link Gauge}
     */
    public static <T> void gauge(Level level, String name, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().gauge(level, name, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metrics
     * @param timeout The timeout value
     * @param timeoutUnit The {@link TimeUnit} for the {@link timeout}
     * @param gauge An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(Level level, String name, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(level, name, timeout, timeoutUnit, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout in seconds
     * 
     * @param level The {@link Level} used for metric
     * @param name The name of the metrics
     * @param timeout The timeout value in seconds
     * @param gauge An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(Level level, String name, long timeout, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService()
                .cachedGauge(level, name, timeout, TimeUnit.SECONDS, gauge);
    }
}
