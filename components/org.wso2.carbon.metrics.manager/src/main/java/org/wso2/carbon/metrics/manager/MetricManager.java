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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;
import org.wso2.carbon.metrics.manager.jmx.MetricManagerMXBean;
import org.wso2.carbon.metrics.manager.jmx.MetricManagerMXBeanImpl;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

/**
 * MetricManager is a static utility class providing various metrics.
 */
public final class MetricManager {

    private static final Logger logger = LoggerFactory.getLogger(MetricManager.class);

    private static final String MBEAN_NAME = "org.wso2.carbon:type=MetricManager";

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
     * Return a {@link Meter} instance registered under given name
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param statName The statName of the metric
     * @return a {@link Meter} instance
     */
    public static Meter meter(Level level, String name, String statName) {
        return ServiceReferenceHolder.getInstance().getMetricService().meter(level, name, name, statName);
    }

    /**
     * Return a {@link Meter} instance registered under given name
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param path     The annotated path of the metric
     * @param statName The statName of the metric
     * @return a {@link Meter} instance
     */
    public static Meter meter(Level level, String name, String path, String statName) {
        return ServiceReferenceHolder.getInstance().getMetricService().meter(level, name, path, statName);
    }

    /**
     * Return a {@link Counter} instance registered under given name
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param statName The statName of the metric
     * @return a {@link Counter} instance
     */
    public static Counter counter(Level level, String name, String statName) {
        return ServiceReferenceHolder.getInstance().getMetricService().counter(level, name, name, statName);
    }

    /**
     * Return a {@link Counter} instance registered under given name
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param path     The annotated path of the metric
     * @param statName The statName of the metric
     * @return a {@link Counter} instance
     */
    public static Counter counter(Level level, String name, String path, String statName) {
        return ServiceReferenceHolder.getInstance().getMetricService().counter(level, name, path, statName);
    }

    /**
     * Return a {@link Timer} instance registered under given name
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param statName The statName of the metric
     * @return a {@link Timer} instance
     */
    public static Timer timer(Level level, String name, String statName) {
        return ServiceReferenceHolder.getInstance().getMetricService().timer(level, name, name, statName);
    }

    /**
     * Return a {@link Timer} instance registered under given name
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param path     The annotated path of the metric
     * @param statName The statName of the metric
     * @return a {@link Timer} instance
     */
    public static Timer timer(Level level, String name, String path, String statName) {
        return ServiceReferenceHolder.getInstance().getMetricService().timer(level, name, path, statName);
    }

    /**
     * Return a {@link Histogram} instance registered under given name
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param statName The statName of the metric
     * @return a {@link Histogram} instance
     */
    public static Histogram histogram(Level level, String name, String statName) {
        return ServiceReferenceHolder.getInstance().getMetricService().histogram(level, name, name, statName);
    }

    /**
     * Return a {@link Histogram} instance registered under given name
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param path     The annotated path of the metric
     * @param statName The statName of the metric
     * @return a {@link Histogram} instance
     */
    public static Histogram histogram(Level level, String name, String path, String statName) {
        return ServiceReferenceHolder.getInstance().getMetricService().histogram(level, name, path, statName);
    }

    /**
     * Register a {@link Gauge} instance under given name
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param statName The statName of the metric
     * @param gauge    An implementation of {@link Gauge}
     */
    public static <T> void gauge(Level level, String name, String statName, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().gauge(level, name, name, statName, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param path     The annotated path of the metric
     * @param statName The statName of the metric
     * @param gauge    An implementation of {@link Gauge}
     */
    public static <T> void gauge(Level level, String name, String path, String statName, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().gauge(level, name, path, statName, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout
     *
     * @param level       The {@link Level} used for metric
     * @param name        The name of the metric
     * @param statName    The statName of the metric
     * @param timeout     The timeout value
     * @param timeoutUnit The {@link TimeUnit} for the timeout
     * @param gauge       An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(Level level, String name, String statName, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(level, name, name, statName, timeout, timeoutUnit, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout
     *
     * @param level       The {@link Level} used for metric
     * @param name        The name of the metric
     * @param path        The annotated path of the metric
     * @param statName    The statName of the metric
     * @param timeout     The timeout value
     * @param timeoutUnit The {@link TimeUnit} for the timeout
     * @param gauge       An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(Level level, String name, String path, String statName, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(level, name, path, statName, timeout, timeoutUnit, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout in seconds
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param statName The statName of the metric
     * @param timeout  The timeout value in seconds
     * @param gauge    An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(Level level, String name, String statName, long timeout, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(level, name, name, statName, timeout, TimeUnit.SECONDS, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout in seconds
     *
     * @param level    The {@link Level} used for metric
     * @param name     The name of the metric
     * @param path     The annotated path of the metric
     * @param statName The statName of the metric
     * @param timeout  The timeout value in seconds
     * @param gauge    An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(Level level, String name, String path, String statName, long timeout, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(level, name, path, statName, timeout, TimeUnit.SECONDS, gauge);
    }

    /**
     * @return the Metric Hierarchy
     */
    public static MetricHierarchy metricHierarchy() {
        return ServiceReferenceHolder.getInstance().getMetricService().getMetricHierarchy();
    }

    public static void registerMXBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = new ObjectName(MBEAN_NAME);
            if (mBeanServer.isRegistered(name)) {
                mBeanServer.unregisterMBean(name);
            }
            MetricManagerMXBean mxBean =
                    new MetricManagerMXBeanImpl(ServiceReferenceHolder.getInstance().getMetricService());
            mBeanServer.registerMBean(mxBean, name);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("MetricManagerMXBean registered under name: %s", name));
            }
        } catch (JMException e) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("MetricManagerMXBean registration failed. Name: %s", MBEAN_NAME), e);
            }
        }
    }

    public static void unregisterMXBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = new ObjectName(MBEAN_NAME);
            if (mBeanServer.isRegistered(name)) {
                mBeanServer.unregisterMBean(name);
            }
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("MetricManagerMXBean with name '%s' was unregistered.", name));
            }
        } catch (JMException e) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("MetricManagerMXBean with name '%s' was failed to unregister", MBEAN_NAME), e);
            }
        }
    }
}
