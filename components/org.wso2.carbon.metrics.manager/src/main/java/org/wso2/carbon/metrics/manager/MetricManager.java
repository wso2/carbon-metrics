/*
 * Copyright 2014-2015 WSO2 Inc. (http://wso2.org)
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

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.manager.exception.MetricNotFoundException;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;
import org.wso2.carbon.metrics.manager.jmx.MetricManagerMXBean;
import org.wso2.carbon.metrics.manager.jmx.MetricManagerMXBeanImpl;

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
     * Get an existing {@link Meter} instance or {@link Meter}s bundle registered under a given name. If the name is not
     * annotated, it'll return a single {@link Meter} instance. Otherwise it'll return a {@link Meter} bundle. Moreover,
     * if the name is annotated, performing actions (i.e {@link Meter#mark()}) in the returned bundle will result in
     * updating all the {@link Meter}s denoted by the annotated name. i.e.
     * 
     * <pre>
     * {@code
     * Meter m = MetricManager.meter("org.wso2.parent[+].child.metric");
     * m.mark();
     * }
     * </pre>
     * 
     * Above example will internally call {@link Meter#mark()} on both {@link Meter}s registered under
     * org.wso2.parent.metric and org.wso2.parent.child.metric
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.parent[+].child.metric)
     * @return a single {@link Meter} instance or a {@link Meter} bundle.
     */
    public static Meter getMeter(String name) throws MetricNotFoundException {
        return ServiceReferenceHolder.getInstance().getMetricService().getMeter(name);
    }

    /**
     * Get or create a {@link Meter}s bundle registered under a given annotated name and {@link Level}s. Unlike
     * {@link #getMeter(String)}, this will create the metrics denoted by the annotated name if they do not exist.
     * Moreover, performing actions (i.e {@link Meter#mark()}) in the returned bundle will result in updating
     * all the {@link Meter}s denoted by the annotated name. i.e.
     * 
     * <pre>
     * {@code
     * Meter m = MetricManager.meter("org.wso2.parent[+].child.metric", Level.INFO, Level.INFO);
     * m.mark();
     * }
     * </pre>
     * 
     * Above example will get or create two {@link Meter}s registered under org.wso2.parent.metric and
     * org.wso2.parent.child.metric with {@link Level#INFO}. Furthermore, it will internally call
     * {@link Meter#mark()} on both retrieved (or created) {@link Meter}s.
     *
     * @param name The annotated name of the metric (i.e org.wso2.parent[+].child.metric)
     * @param levels The {@link Level}s used for each annotated metric (Number of {@code levels} and Metrics count
     *            should be equal)
     * @return a {@link Meter} bundle which wraps a collection of {@link Meter}s
     */
    public static Meter meter(String name, Level... levels) {
        return ServiceReferenceHolder.getInstance().getMetricService().meter(name, levels);
    }

    /**
     * Return a {@link Meter} instance registered under given name
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Meter} instance
     * @deprecated Use {@link #meter(String, Level...)} instead
     */
    @Deprecated
    public static Meter meter(Level level, String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().meter(name, level);
    }

    /**
     * Get an existing {@link Counter} instance or {@link Counter}s bundle registered under a given name. If the name
     * is not annotated, it'll return a single {@link Counter} instance. Otherwise it'll return a {@link Counter}
     * bundle. Moreover, if the name is annotated, performing actions (i.e {@link Counter#inc()}) in the returned bundle
     * will result in updating all the {@link Counter}s denoted by the annotated name. i.e.
     * 
     * <pre>
     * {@code
     * Counter c = MetricManager.getCounter("org.wso2.parent[+].child.metric");
     * c.inc();
     * }
     * </pre>
     * 
     * Above example will internally call {@link Counter#inc()} on both {@link Counter}s registered under
     * org.wso2.parent.metric and org.wso2.parent.child.metric
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.parent[+].child.metric)
     * @return a single {@link Counter} instance or a {@link Counter} bundle.
     */
    public static Counter getCounter(String name) throws MetricNotFoundException {
        return ServiceReferenceHolder.getInstance().getMetricService().getCounter(name);
    }

    /**
     * Get or create a {@link Counter}s bundle registered under a given annotated name and {@link Level}s. Unlike
     * {@link #getCounter(String)}, this will create the metrics denoted by the annotated name if they do not exist.
     * Moreover, performing actions (i.e {@link Counter#inc()}) in the returned bundle will result in updating
     * all the {@link Counter}s denoted by the annotated name. i.e.
     * 
     * <pre>
     *
     * Counter c = MetricManager.counter("org.wso2.parent[+].child.metric", Level.INFO, Level.INFO);
     * c.inc();
     * }
     * </pre>
     * 
     * Above example will get or create two {@link Counter}s registered under org.wso2.parent.metric and
     * org.wso2.parent.child.metric with {@link Level#INFO}. Furthermore, it will internally call
     * {@link Counter#inc()} on both retrieved (or created) {@link Counter}s.
     *
     * @param name The annotated name of the metric (i.e org.wso2.parent[+].child.metric)
     * @param levels The {@link Level}s used for each annotated metric (Number of {@code levels} and Metrics count
     *            should be equal)
     * @return a {@link Counter} bundle which wraps a collection of {@link Counter}s
     */
    public static Counter counter(String name, Level... levels) {
        return ServiceReferenceHolder.getInstance().getMetricService().counter(name, levels);
    }

    /**
     * Return a {@link Counter} instance registered under given name
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Counter} instance
     * @deprecated Use {@link #counter(String, Level...)} instead
     */
    @Deprecated
    public static Counter counter(Level level, String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().counter(name, level);
    }

    /**
     * Get the {@link Timer} instance registered under given name
     *
     * @param name The name of the metric (name can be annotated)
     * @return a {@link Timer} instance
     */
    public static Timer getTimer(String name) throws MetricNotFoundException {
        return ServiceReferenceHolder.getInstance().getMetricService().timer(name);
    }

    /**
     * Get or create a {@link Timer} instances registered under a annotated name and levels
     *
     * @param name The name of the metric
     * @param level The {@link Level} used for metric
     * @return a {@link Timer} instance
     */
    public static Timer timer(String name, Level level) {
        return ServiceReferenceHolder.getInstance().getMetricService().timer(name, level);
    }

    /**
     * Get or create a {@link Timer} instances registered under a annotated name and levels
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Timer} instance
     * @deprecated Use {@link #timer(String, Level)} instead
     */
    @Deprecated
    public static Timer timer(Level level, String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().timer(name, level);
    }

    /**
     * Get an existing {@link Histogram} instance or {@link Histogram}s bundle registered under a given name. If the
     * name is not
     * annotated, it'll return a single {@link Histogram} instance. Otherwise it'll return a {@link Histogram} bundle.
     * Moreover,
     * if the name is annotated, performing actions (i.e {@link Histogram#update(int)}) in the returned bundle will
     * result in
     * updating all the {@link Histogram}s denoted by the annotated name. i.e.
     * 
     * <pre>
     * {@code
     * Histogram c = MetricManager.Histogram("org.wso2.parent[+].child.metric");
     * c.update(5);
     * }
     * </pre>
     * 
     * Above example will internally call {@link Histogram#update(int)} on both {@link Histogram}s registered under
     * org.wso2.parent.metric and org.wso2.parent.child.metric
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.parent[+].child.metric)
     * @return a single {@link Histogram} instance or a {@link Histogram} bundle.
     */
    public static Histogram getHistogram(String name) throws MetricNotFoundException {
        return ServiceReferenceHolder.getInstance().getMetricService().getHistogram(name);
    }

    /**
     * Get or create a {@link Histogram}s bundle registered under a given annotated name and {@link Level}s. Unlike
     * {@link #getHistogram(String)}, this will create the metrics denoted by the annotated name if they do not exist.
     * Moreover, performing actions (i.e {@link Histogram#update(int)}) in the returned bundle will result in updating
     * all the {@link Histogram}s denoted by the annotated name. i.e.
     * 
     * <pre>
     * {@code
     * Histogram c = MetricManager.histogram("org.wso2.parent[+].child.metric", Level.INFO, Level.INFO);
     * c.update(5);
     * }
     * </pre>
     * 
     * Above example will get or create two {@link Histogram}s registered under org.wso2.parent.metric and
     * org.wso2.parent.child.metric with {@link Level#INFO}. Furthermore, it will internally call
     * {@link Histogram#update(int)} on both retrieved (or created) {@link Histogram}s.
     *
     * @param name The annotated name of the metric (i.e org.wso2.parent[+].child.metric)
     * @param levels The {@link Level}s used for each annotated metric (Number of {@code levels} and Metrics count
     *            should be equal)
     * @return a {@link Histogram} bundle which wraps a collection of {@link Histogram}s
     */
    public static Histogram histogram(String name, Level... levels) {
        return ServiceReferenceHolder.getInstance().getMetricService().histogram(name, levels);
    }

    /**
     * Return a {@link Histogram} instance registered under given name
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @return a {@link Histogram} instance
     * @deprecated Use {@link #histogram(String, Level...)} instead
     */
    @Deprecated
    public static Histogram histogram(Level level, String name) {
        return ServiceReferenceHolder.getInstance().getMetricService().histogram(name, level);
    }

    /**
     * Register a {@link Gauge} instance under given name
     *
     * @param name The name of the metric
     * @param level The {@link Level} used for metric
     * @param gauge An implementation of {@link Gauge}
     */
    public static <T> void gauge(String name, Level level, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().gauge(name, level, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metric
     * @param gauge An implementation of {@link Gauge}
     * @deprecated Use {@link #gauge(String, Level, Gauge)} instead
     */
    @Deprecated
    public static <T> void gauge(Level level, String name, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().gauge(name, level, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout
     *
     * @param name The name of the metric
     * @param level The {@link Level} used for metric
     * @param timeout The timeout value
     * @param timeoutUnit The {@link TimeUnit} for the timeout
     * @param gauge An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(String name, Level level, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(name, level, timeout, timeoutUnit, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metrics
     * @param timeout The timeout value
     * @param timeoutUnit The {@link TimeUnit} for the {@code timeout}
     * @param gauge An implementation of {@link Gauge}
     * @deprecated Use {@link #cachedGauge(String, Level, long, TimeUnit, Gauge)} instead
     */
    @Deprecated
    public static <T> void cachedGauge(Level level, String name, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(name, level, timeout, timeoutUnit, gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout in seconds
     *
     * @param name The name of the metric
     * @param level The {@link Level} used for metric
     * @param timeout The timeout value in seconds
     * @param gauge An implementation of {@link Gauge}
     */
    public static <T> void cachedGauge(String name, Level level, long timeout, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(name, level, timeout, TimeUnit.SECONDS,
                gauge);
    }

    /**
     * Register a {@link Gauge} instance under given name with a configurable cache timeout in seconds
     *
     * @param level The {@link Level} used for metric
     * @param name The name of the metrics
     * @param timeout The timeout value in seconds
     * @param gauge An implementation of {@link Gauge}
     * @deprecated Use {@link #cachedGauge(String, Level, long, Gauge)} instead
     */
    @Deprecated
    public static <T> void cachedGauge(Level level, String name, long timeout, Gauge<T> gauge) {
        ServiceReferenceHolder.getInstance().getMetricService().cachedGauge(name, level, timeout, TimeUnit.SECONDS,
                gauge);
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
                logger.error(String.format("MetricManagerMXBean with name '%s' was failed to unregister", MBEAN_NAME),
                        e);
            }
        }
    }
}
