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
package org.wso2.carbon.metrics.impl;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.metric.ClassLoadingGaugeSet;
import org.wso2.carbon.metrics.impl.metric.OperatingSystemMetricSet;
import org.wso2.carbon.metrics.impl.reporter.ListeningReporter;
import org.wso2.carbon.metrics.impl.reporter.Reporter;
import org.wso2.carbon.metrics.impl.reporter.ScheduledReporter;
import org.wso2.carbon.metrics.impl.util.MetricTreeNode;
import org.wso2.carbon.metrics.impl.util.ReporterDisabledException;
import org.wso2.carbon.metrics.impl.util.ReporterBuilder;
import org.wso2.carbon.metrics.manager.*;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import org.wso2.carbon.metrics.manager.Timer;

/**
 * Implementation class for {@link MetricService}, which will use the Metrics (https://dropwizard.github.io/metrics)
 * library. This is registered as an OSGi service
 */
public class MetricServiceImpl implements MetricService {

    private static final Logger logger = LoggerFactory.getLogger(MetricServiceImpl.class);

    /**
     * Keep all metrics created via this service
     */
    private final ConcurrentMap<String, MetricWrapper> metricsMap = new ConcurrentHashMap<String, MetricWrapper>();

    private final MetricTreeNode rootNode = new MetricTreeNode("root");

    /**
     * Metrics feature enabling flag
     */
    private boolean enabled;

    /**
     * The {@link MetricRegistry} instance from the Metrics Implementation
     */
    private final MetricRegistry metricRegistry;

    private static final String SYSTEM_PROPERTY_METRICS_ENABLED = "metrics.enabled";
    private static final String SYSTEM_PROPERTY_METRICS_ROOT_LEVEL = "metrics.rootLevel";

    /**
     * Name of the root metric. This is set to empty string.
     */
    private static final String ROOT_METRIC_NAME = "";

    /**
     * Hierarchy delimiter in Metric name
     */
    private static final String HIERARCHY_DELIMITER = ".";

    private final MetricsLevelConfiguration levelConfiguration;

    private final Set<Reporter> reporters = new HashSet<Reporter>();

    private final MetricFilter enabledMetricFilter = new EnabledMetricFilter();

    /**
     * MetricWrapper class is used for the metrics map. This class keeps the associated {@link Level} and enabled status
     * for a metric. The main reason to keep the enabled status separately is that EnabledMetricFilter gets called as
     * soon as a Metric is added to MetricRegistry. The JMXReporter registers MBeans via a listener added to
     * MetricRegistry. The wrapper is not available when the listener gets called and by keeping enabled status
     * separately, we can check whether the metric should be filtered without having the metric wrapper
     */
    protected static class MetricWrapper {

        private final Level level;

        private Boolean enabled;

        private AbstractMetric metric;

        private final String name;

        private final String path;

        private final String identifier;

        private MetricWrapper(String name, String path, String identifier, Level level, Boolean enabled) {
            this.name = name;
            this.path = path;
            this.identifier = identifier;
            this.level = level;
            this.enabled = enabled;
        }
    }

    public static class Builder {

        private static final String ENABLED = "Enabled";

        private boolean enabled;

        private Level rootLevel;

        private Set<ReporterBuilder<? extends Reporter>> reporterBuilders = new HashSet<>();

        public Builder setEnabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setRootLevel(final Level rootLevel) {
            this.rootLevel = rootLevel;
            return this;
        }

        public Builder addReporterBuilder(final ReporterBuilder<? extends Reporter> reporterBuilder) {
            this.reporterBuilders.add(reporterBuilder);
            return this;
        }

        public Builder configure(final MetricsConfiguration configuration) {
            enabled = Boolean.valueOf(configuration.getProperty(ENABLED));
            return this;
        }

        public MetricService build(final MetricsLevelConfiguration levelConfiguration) {
            return new MetricServiceImpl(enabled, rootLevel, levelConfiguration, reporterBuilders);
        }
    }

    /**
     * Initialize the MetricRegistry, Level and Reporters
     */
    private MetricServiceImpl(boolean enabled, Level rootLevel, final MetricsLevelConfiguration levelConfiguration,
            Set<ReporterBuilder<? extends Reporter>> reporterBuilders) {
        this.levelConfiguration = levelConfiguration;
        // Highest priority is given for the System Properties
        String metricsEnabledProperty = System.getProperty(SYSTEM_PROPERTY_METRICS_ENABLED);
        if (metricsEnabledProperty != null && !metricsEnabledProperty.trim().isEmpty()) {
            enabled = Boolean.valueOf(metricsEnabledProperty);
        }
        String rootLevelProperty = System.getProperty(SYSTEM_PROPERTY_METRICS_ROOT_LEVEL);
        if (rootLevelProperty != null && !rootLevelProperty.trim().isEmpty()) {
            Level level = Level.getLevel(rootLevelProperty);
            if (level != null) {
                rootLevel = level;
            }
        }

        if (rootLevel != null) {
            levelConfiguration.setRootLevel(rootLevel);
        }

        // Initialize Metric Registry
        metricRegistry = new MetricRegistry();

        // Build all reporters
        for (ReporterBuilder<? extends Reporter> reporterBuilder : reporterBuilders) {
            try {
                reporters.add(reporterBuilder.build(metricRegistry, enabledMetricFilter));
            } catch (ReporterDisabledException e) {
                // This can be ignored
            } catch (Throwable e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to build the reporter", e);
                }
            }
        }

        // Set enabled
        setEnabled(enabled);

        // Register JVM Metrics
        // This should be the last method when initializing MetricService
        registerJVMMetrics();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#enable()
     */
    @Override
    public void enable() {
        setEnabled(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#disable()
     */
    @Override
    public void disable() {
        setEnabled(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private void setEnabled(boolean enabled) {
        boolean changed = (this.enabled != enabled);
        this.enabled = enabled;
        if (changed) {
            notifyEnabledStatus();
            if (enabled) {
                startReporters();
            } else {
                stopReporters();
            }
        }
    }

    private void notifyEnabledStatus() {
        for (MetricWrapper metricWrapper : metricsMap.values()) {
            AbstractMetric metric = metricWrapper.metric;
            metric.setEnabled(isMetricEnabled(metricWrapper.name, metricWrapper.path, metricWrapper.identifier, metric.getLevel(), levelConfiguration.getLevel(metric.getName()), false));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#getMetricLevel(java.lang.String)
     */
    @Override
    public Level getMetricLevel(String identifier) {
        // TODO : default name should be a constant
        return getMetricLevel("org.wso2", identifier);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#getMetricLevel(java.lang.String)
     */
    @Override
    public Level getMetricLevel(String name, String identifier) {
        String absoluteName = getAbsoluteName(identifier, name);
        if (!metricsMap.containsKey(absoluteName)) {
            throw new IllegalArgumentException("Invalid Metric Name");
        }
        return levelConfiguration.getLevel(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#setMetricLevel(java.lang.String,
     * org.wso2.carbon.metrics.manager.Level)
     */
    @Override
    public void setMetricLevel(String identifier, Level level) {
        // TODO : This might have to be removed (default name might not work)
        // TODO : default name should be a constant
        setMetricLevel("org.wso2", identifier, level);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#setMetricLevel(java.lang.String,
     * org.wso2.carbon.metrics.manager.Level)
     */
    @Override
    public void setMetricLevel(String name, String identifier, Level level) {
        String absoluteName = getAbsoluteName(identifier, name);
        MetricWrapper metricWrapper = metricsMap.get(absoluteName);
        if (metricWrapper == null) {
            throw new IllegalArgumentException("Invalid Metric Name");
        }
        Level currentLevel = levelConfiguration.getLevel(name);
        if (currentLevel == null || !currentLevel.equals(level)) {
            // Set new level only if there is no existing level or the new level is different from existing level
            levelConfiguration.setLevel(name, level);
            AbstractMetric metric = metricWrapper.metric;
            metric.setEnabled(isMetricEnabled(metricWrapper.name, metricWrapper.path, metricWrapper.identifier, metric.getLevel(), level, false));
            restartListeningReporters();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#getRootLevel()
     */
    @Override
    public Level getRootLevel() {
        return levelConfiguration.getRootLevel();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#setRootLevel(org.wso2.carbon.metrics.manager.Level)
     */
    @Override
    public void setRootLevel(Level level) {
        boolean changed = !this.getRootLevel().equals(level);
        levelConfiguration.setRootLevel(level);
        if (changed) {
            notifyEnabledStatus();
            restartListeningReporters();
        }
    }

    @Override
    public List<org.wso2.carbon.metrics.manager.Metric> getAffectedMetrics(Level level, String name, String path, String identifier) {
        List<org.wso2.carbon.metrics.manager.Metric> affected = new ArrayList<>();
        String[] chunks = path.split("\\.");
        StringBuilder builder = new StringBuilder();
        String affectedName;
        for (String chunk : chunks) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(chunk);
            if (chunk.contains("[+]")) {
                affectedName = builder.toString().replaceAll("\\[\\+\\]", "");
                String absoluteName = getAbsoluteName(identifier, affectedName);
                if (metricsMap.get(absoluteName) != null) {
                    affected.add(metricsMap.get(absoluteName).metric);
                } else {
                    MetricWrapper wrapper = metricsMap.get(name);
                    if (wrapper.metric instanceof Counter) {
                        counter(level, affectedName, affectedName, identifier);
                    } else if (wrapper.metric instanceof Meter) {
                        meter(level, affectedName, affectedName, identifier);
                    } else if (wrapper.metric instanceof Histogram) {
                        histogram(level, affectedName, affectedName, identifier);
                    } else if (wrapper.metric instanceof Timer) {
                        timer(level, affectedName, affectedName, identifier);
                    }
                    return getAffectedMetrics(level, name, path, identifier);
                }
            }
        }
        return affected;
    }

    private String getAbsoluteName(String identifier, String name) {
        return new StringBuilder().append(identifier).append("@").append(name.replaceAll("\\[\\+\\]", "")).toString();
    }

    private boolean isMetricEnabled(String name, String path, String identifier, Level metricLevel, Level configLevel, boolean getFromCache) {
        String absoluteName = getAbsoluteName(identifier, name);
        MetricWrapper metricWrapper = metricsMap.get(absoluteName);
        if (!getFromCache || metricWrapper.enabled == null) {
            metricWrapper.enabled = isMetricEnabledBasedOnHierarchyLevel(name, metricLevel, configLevel);
        }
        return metricWrapper.enabled;
    }

    /**
     * Recursive method to check enabled status based on level hierarchy
     *
     * @param name Metric Name
     * @param metricLevel The {@code Level} associated with metric
     * @param configLevel The configured {@code Level} for the given metric
     * @return {@code true} if enabled
     */
    private boolean isMetricEnabledBasedOnHierarchyLevel(String name, Level metricLevel, Level configLevel) {
        // Enabled flag should be modified only if metrics feature is enabled.
        if (configLevel != null) {
            // Then this is enabled only if the new threshold level is greater than or equal to current level.
            // This should be done only if the new level is not equal to OFF.
            // Otherwise the condition would fail when comparing two "OFF" levels
            return this.enabled && configLevel.intLevel() >= metricLevel.intLevel()
                    && configLevel.intLevel() > Level.OFF.intLevel();
        } else {
            String parentName;
            int index = name.lastIndexOf(HIERARCHY_DELIMITER);
            if (index != -1) {
                parentName = name.substring(0, index);
                configLevel = levelConfiguration.getLevel(parentName);
            } else {
                parentName = ROOT_METRIC_NAME;
                configLevel = levelConfiguration.getRootLevel();
            }
            return isMetricEnabledBasedOnHierarchyLevel(parentName, metricLevel, configLevel);
        }
    }

    /**
     * Get or create a metric
     *
     * @param level The {@code Level} of Metric
     * @param name The name of the metric
     * @param metricBuilder A {@code MetricBuilder} instance used to create the relevant metric
     * @return The created {@code AbstractMetric}
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractMetric> T getOrCreateMetric(Level level, String name, String path, String identifier, MetricBuilder<T> metricBuilder) {
        String absoluteName = getAbsoluteName(identifier, name);
        MetricWrapper metricWrapper = metricsMap.get(absoluteName);
        if (metricWrapper != null && metricWrapper.metric != null) {
            AbstractMetric metric = metricWrapper.metric;
            if (metricBuilder.isInstance(metric)) {
                if (level.equals(metricWrapper.level)) {
                    if (metric instanceof MetricUpdater) {
                        ((MetricUpdater) metric).updateAffectedMetrics(path);
                    }
                    return (T) metric;
                } else {
                    throw new IllegalArgumentException(absoluteName + " is already used with a different level");
                }
            } else {
                throw new IllegalArgumentException(absoluteName + " is already used for a different type of metric");
            }
        } else {
            boolean enabled = isMetricEnabledBasedOnHierarchyLevel(name, level, levelConfiguration.getLevel(name));
            metricWrapper = new MetricWrapper(name, path, identifier, level, enabled);
            metricsMap.put(absoluteName, metricWrapper);
            T newMetric = metricBuilder.createMetric(level, absoluteName, path, identifier);
            metricWrapper.metric = newMetric;
            newMetric.setEnabled(enabled);
            addToMetricHierarchy(identifier, path, newMetric);
            if (newMetric instanceof MetricUpdater) {
                ((MetricUpdater) newMetric).updateAffectedMetrics(path);
            }
            return newMetric;
        }
    }

    private MetricTreeNode getOrCreateMetricTreeNode(String name) {
        MetricTreeNode treeNode = rootNode.getNodeByName(name);
        if (treeNode == null) {
            if (name.contains(".")) {
                int i = name.lastIndexOf(".");
                String parentName = name.substring(0, i);
                String childName = name.substring(i + 1);
                MetricTreeNode parent = getOrCreateMetricTreeNode(parentName);
                treeNode = parent.addChild(childName);
            } else {
                treeNode = rootNode.addChild(name);
            }
        }
        return treeNode;
    }

    private void addToMetricHierarchy(String identifier, String name, AbstractMetric metric) {
        // TODO : though it's called "name" actual value passing in is path
        name = name.replaceAll("\\[\\+\\]", "");
        MetricTreeNode treeNode = getOrCreateMetricTreeNode(name);
        treeNode.addMetric(identifier, metric);
    }

    /**
     * An interface for creating a new metric
     */
    private interface MetricBuilder<T extends AbstractMetric> {
        T createMetric(Level level, String absoluteName, String path, String identifier);

        boolean isInstance(AbstractMetric metric);
    }

    /**
     * Default Metric Builder for {@code MeterImpl}
     */
    private final MetricBuilder<MeterImpl> METER_BUILDER = new MetricBuilder<MeterImpl>() {
        @Override
        public MeterImpl createMetric(Level level, String absoluteName, String path, String identifier) {
            return new MeterImpl(level, absoluteName, path, identifier, metricRegistry.meter(absoluteName));
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return MeterImpl.class.isInstance(metric);
        }
    };

    /**
     * Default Metric Builder for {@code CounterImpl}
     */
    private final MetricBuilder<CounterImpl> COUNTER_BUILDER = new MetricBuilder<CounterImpl>() {
        @Override
        public CounterImpl createMetric(Level level, String absoluteName, String path, String identifier) {
            return new CounterImpl(level, absoluteName, path, identifier, metricRegistry.counter(absoluteName));
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return CounterImpl.class.isInstance(metric);
        }
    };

    /**
     * Default Metric Builder for {@code TimerImpl}
     */
    private final MetricBuilder<TimerImpl> TIMER_BUILDER = new MetricBuilder<TimerImpl>() {
        @Override
        public TimerImpl createMetric(Level level, String absoluteName, String path, String identifier) {
            return new TimerImpl(level, absoluteName, path, identifier, metricRegistry.timer(absoluteName));
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return TimerImpl.class.isInstance(metric);
        }
    };

    /**
     * Default Metric Builder for {@code HistogramImpl}
     */
    private final MetricBuilder<HistogramImpl> HISTOGRAM_BUILDER = new MetricBuilder<HistogramImpl>() {
        @Override
        public HistogramImpl createMetric(Level level, String absoluteName, String path, String identifier) {
            return new HistogramImpl(level, absoluteName, path, identifier, metricRegistry.histogram(absoluteName));
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return HistogramImpl.class.isInstance(metric);
        }
    };

    /**
     * A Metric Builder for {@code GaugeImpl}
     */
    private class GaugeBuilder<T> implements MetricBuilder<GaugeImpl<T>> {

        private final Gauge<T> gauge;

        public GaugeBuilder(Gauge<T> gauge) {
            super();
            this.gauge = gauge;
        }

        @Override
        public GaugeImpl<T> createMetric(Level level, String absoluteName, String path, String identifier) {
            GaugeImpl<T> gaugeImpl = new GaugeImpl<T>(level, absoluteName, path, identifier, gauge);
            metricRegistry.register(absoluteName, gaugeImpl);
            return gaugeImpl;
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return GaugeImpl.class.isInstance(metric);
        }
    };

    /**
     * A Metric Builder for {@code CachedGaugeImpl}
     */
    private class CachedGaugeBuilder<T> implements MetricBuilder<CachedGaugeImpl<T>> {

        private final Gauge<T> gauge;
        private final long timeout;
        private final TimeUnit timeoutUnit;

        public CachedGaugeBuilder(Gauge<T> gauge, long timeout, TimeUnit timeoutUnit) {
            super();
            this.gauge = gauge;
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
        }

        @Override
        public CachedGaugeImpl<T> createMetric(Level level, String absoluteName, String path, String identifier) {
            CachedGaugeImpl<T> gaugeImpl = new CachedGaugeImpl<T>(level, absoluteName, path, identifier, timeout, timeoutUnit, gauge);
            metricRegistry.register(absoluteName, gaugeImpl);
            return gaugeImpl;
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return CachedGaugeImpl.class.isInstance(metric);
        }

    };

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#meter(org.wso2.carbon.metrics.manager.Level, java.lang.String)
     */
    @Override
    public Meter meter(Level level, String name, String path, String identifier) {
        return getOrCreateMetric(level, name, path, identifier, METER_BUILDER);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#counter(org.wso2.carbon.metrics.manager.Level,
     * java.lang.String)
     */
    @Override
    public Counter counter(Level level, String name, String path, String identifier) {
        return getOrCreateMetric(level, name, path, identifier, COUNTER_BUILDER);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#timer(org.wso2.carbon.metrics.manager.Level, java.lang.String)
     */
    @Override
    public Timer timer(Level level, String name, String path, String identifier) {
        return getOrCreateMetric(level, name, path, identifier, TIMER_BUILDER);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#histogram(org.wso2.carbon.metrics.manager.Level,
     * java.lang.String)
     */
    @Override
    public Histogram histogram(Level level, String name, String path, String identifier) {
        return getOrCreateMetric(level, name, path, identifier, HISTOGRAM_BUILDER);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#gauge(org.wso2.carbon.metrics.manager.Level, java.lang.String,
     * org.wso2.carbon.metrics.manager.Gauge)
     */
    @Override
    public <T> void gauge(Level level, String name, String path, String identifier, Gauge<T> gauge) {
        getOrCreateMetric(level, name, path, identifier, new GaugeBuilder<T>(gauge));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricService#cachedGauge(org.wso2.carbon.metrics.manager.Level,
     * java.lang.String, long, java.util.concurrent.TimeUnit, org.wso2.carbon.metrics.manager.Gauge)
     */
    @Override
    public <T> void cachedGauge(Level level, String name, String path, String identifier, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        getOrCreateMetric(level, name, path, identifier, new CachedGaugeBuilder<T>(gauge, timeout, timeoutUnit));
    }

    public int getMetricsCount() {
        return metricsMap.size();
    }

    private void registerJVMMetrics() {
        // TODO : Hardcoded path???
        registerAll(Level.INFO, "jvm.memory", "org.wso2", new MemoryUsageGaugeSet());
        registerAll(Level.INFO, "jvm.os", "org.wso2", new OperatingSystemMetricSet());
        registerAll(Level.INFO, "jvm.class-loading", "org.wso2", new ClassLoadingGaugeSet());
        registerAll(Level.DEBUG, "jvm.gc", "org.wso2", new GarbageCollectorMetricSet());
        registerAll(Level.DEBUG, "jvm.threads", "org.wso2", new ThreadStatesGaugeSet());
        registerAll(Level.TRACE, "jvm.buffers", "org.wso2", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
    }

    private void registerAll(Level level, String prefix, String identifier, MetricSet metrics) throws IllegalArgumentException {
        for (Map.Entry<String, com.codahale.metrics.Metric> entry : metrics.getMetrics().entrySet()) {
            if (entry.getValue() instanceof MetricSet) {
                registerAll(level, prefix, entry.getKey(), (MetricSet) entry.getValue());
            } else if (filterJVMMetric(entry.getKey())) {
                String name = MetricRegistry.name(prefix, entry.getKey());
                com.codahale.metrics.Metric metric = entry.getValue();
                if (metric instanceof com.codahale.metrics.Gauge) {
                    com.codahale.metrics.Gauge<?> gauge = (com.codahale.metrics.Gauge<?>) metric;
                    gauge(level, prefix, prefix, entry.getKey(), new JVMGaugeWrapper(gauge));
                } else {
                    logger.warn(String.format("Unexpected Metric found. Name: %s@%s, Class: %s", entry.getKey(), name, metric.getClass()));
                }
            }
        }
    }

    private boolean filterJVMMetric(String name) {
        // Remove "deadlocks" as it is a String Set.
        if ("deadlocks".equals(name)) {
            return false;
        }
        return true;
    }

    private static class JVMGaugeWrapper implements Gauge<Object> {

        private final com.codahale.metrics.Gauge<?> gauge;

        private JVMGaugeWrapper(com.codahale.metrics.Gauge<?> gauge) {
            this.gauge = gauge;
        }

        @Override
        public Object getValue() {
            return gauge.getValue();
        }

    }

    /**
     * For testing purposes
     */
    @Override
    public void report() {
        for (Reporter reporter : reporters) {
            if (reporter instanceof ScheduledReporter) {
                ((ScheduledReporter) reporter).report();
            }
        }
    }

    private void startReporters() {
        for (Reporter reporter : reporters) {
            try {
                reporter.start();
            } catch (Throwable e) {
                logger.error("Error when starting the reporter", e);
            }
        }
    }

    private void stopReporters() {
        for (Reporter reporter : reporters) {
            try {
                reporter.stop();
            } catch (Throwable e) {
                logger.error("Error when stopping the reporter", e);
            }
        }
    }

    private void restartListeningReporters() {
        for (Reporter reporter : reporters) {
            if (reporter instanceof ListeningReporter) {
                ListeningReporter listeningReporter = (ListeningReporter) reporter;
                listeningReporter.stop();
                listeningReporter.start();
            }
        }
    }

    /**
     * A {@code MetricFilter} to filter metrics based on enabled status
     */
    private class EnabledMetricFilter implements MetricFilter {

        @Override
        public boolean matches(String name, Metric metric) {
            MetricWrapper metricWrapper = metricsMap.get(name);
            if (metricWrapper != null) {
                return isMetricEnabled(metricWrapper.name, metricWrapper.path, metricWrapper.identifier, metricWrapper.level, levelConfiguration.getLevel(name), true);
            }
            return false;
        }
    }

}
