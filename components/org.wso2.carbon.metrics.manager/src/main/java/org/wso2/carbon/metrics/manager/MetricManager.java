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
     * Get the {@link Meter} instance registered under given name
     *
     * @param name The name of the metric (name can be annotated)
     * @return a {@link Meter} instance
     */
    public static Meter meter(String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().meter(name);
    }

    /**
     * Get or create a {@link Meter} instances registered under a annotated name and levels
     *
     * @param name   The annotated name of the metric
     * @param levels The {@link Level}s used for each annotated metric
     * @return a {@link Meter} instance (which wraps a collection of {@link Meter}s)
     */
    public static Meter meter(String name, Level... levels) {
        return ServiceReferenceHolder.getInstance().getMetricService().meter(name, levels);
    }

    /**
     * Get the {@link Counter} instance registered under given name
     *
     * @param name The name of the metric (name can be annotated)
     * @return a {@link Counter} instance
     */
    public static Counter counter(String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().counter(name);
    }

    /**
     * Get or create a {@link Counter} instances registered under a annotated name and levels
     *
     * @param name   The annotated name of the metric
     * @param levels The {@link Level}s used for each annotated metric
     * @return a {@link Counter} instance (which wraps a collection of {@link Counter}s)
     */
    public static Counter counter(String name, Level... levels) {
        return ServiceReferenceHolder.getInstance().getMetricService().counter(name, levels);
    }

    /**
     * Get the {@link Timer} instance registered under given name
     *
     * @param name The name of the metric (name can be annotated)
     * @return a {@link Timer} instance
     */
    public static Timer timer(String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().timer(name);
    }

    /**
     * Get or create a {@link Timer} instances registered under a annotated name and levels
     *
     * @param name   The annotated name of the metric
     * @param levels The {@link Level}s used for each annotated metric
     * @return a {@link Timer} instance (which wraps a collection of {@link Timer}s)
     */
    public static Timer timer(String name, Level... levels) {
        return ServiceReferenceHolder.getInstance().getMetricService().timer(name, levels);
    }

    /**
     * Get the {@link Histogram} instance registered under given name
     *
     * @param name The name of the metric (name can be annotated)
     * @return a {@link Histogram} instance
     */
    public static Histogram histogram(String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().histogram(name);
    }

    /**
     * Get or create a {@link Histogram} instances registered under a annotated name and levels
     *
     * @param name   The annotated name of the metric
     * @param levels The {@link Level}s used for each annotated metric
     * @return a {@link Histogram} instance (which wraps a collection of {@link Histogram}s)
     */
    public static Histogram histogram(String name, Level... levels) {
        return ServiceReferenceHolder.getInstance().getMetricService().histogram(name, levels);
    }

    /**
     * Register a {@link Gauge} instance under given name
     *
     * @param name  The name of the metric
     * @param level The {@link Level} used for metric
     * @param gauge An implementation of {@link Gauge}
     */
    public static <T> void gauge(String name, Level level, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().gauge(name, level, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout
     *
     * @param name        The name of the metric
     * @param level       The {@link Level} used for metric
     * @param timeout     The timeout value
     * @param timeoutUnit The {@link TimeUnit} for the timeout
     * @param gauge       An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(String name, Level level, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(name, level, timeout, timeoutUnit, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout in seconds
     *
     * @param name    The name of the metric
     * @param level   The {@link Level} used for metric
     * @param timeout The timeout value in seconds
     * @param gauge   An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(String name, Level level, long timeout, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(name, level, timeout, TimeUnit.SECONDS, gauge);
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
