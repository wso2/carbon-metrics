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
package org.wso2.carbon.metrics.core.impl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.Counter;
import org.wso2.carbon.metrics.core.CounterCollection;
import org.wso2.carbon.metrics.core.Gauge;
import org.wso2.carbon.metrics.core.Histogram;
import org.wso2.carbon.metrics.core.HistogramCollection;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MeterCollection;
import org.wso2.carbon.metrics.core.Metric;
import org.wso2.carbon.metrics.core.MetricNotFoundException;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.carbon.metrics.core.config.MetricsConfigBuilder;
import org.wso2.carbon.metrics.core.config.MetricsLevelConfigBuilder;
import org.wso2.carbon.metrics.core.config.model.MetricsConfig;
import org.wso2.carbon.metrics.core.config.model.MetricsLevelConfig;
import org.wso2.carbon.metrics.core.jmx.MetricManagerMXBean;
import org.wso2.carbon.metrics.core.metric.ClassLoadingGaugeSet;
import org.wso2.carbon.metrics.core.metric.OperatingSystemMetricSet;
import org.wso2.carbon.metrics.core.reporter.ListeningReporter;
import org.wso2.carbon.metrics.core.reporter.Reporter;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.metrics.core.reporter.ScheduledReporter;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation class for {@link MetricService}, which will use the Metrics (https://dropwizard.github.io/metrics)
 * library. This is registered as an OSGi service
 */

/**
 * Main interface for the service creating various metrics
 */
public final class MetricService implements MetricManagerMXBean {

    private static final Logger logger = LoggerFactory.getLogger(MetricService.class);

    /**
     * Keep all metrics created via this service
     */
    private final ConcurrentMap<String, MetricWrapper> metricsMap = new ConcurrentHashMap<>();

    /**
     * Keep all metric collections created via this service
     */
    private final ConcurrentMap<String, Metric> metricsCollections = new ConcurrentHashMap<>();

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

    private static final String METRIC_AGGREGATE_ANNOTATION = "[+]";

    private static final String METRIC_AGGREGATE_ANNOTATION_REGEX = "\\[\\+\\]";

    /**
     * Name of the root metric. This is set to empty string.
     */
    private static final String ROOT_METRIC_NAME = "";

    /**
     * Hierarchy delimiter in Metric name
     */
    private static final String METRIC_PATH_DELIMITER = ".";

    private final MetricsLevelConfig levelConfiguration;

    private final Set<Reporter> reporters = new HashSet<>();

    private static final Pattern METRIC_AGGREGATE_ANNOTATION_PATTERN =
            Pattern.compile(METRIC_AGGREGATE_ANNOTATION_REGEX);

    /**
     * MetricWrapper class is used for the metrics map. This class keeps the associated {@link Level} and enabled status
     * for a metric. The main reason to keep the enabled status separately is that EnabledMetricFilter gets called as
     * soon as a Metric is added to MetricRegistry. The JMXReporter registers MBeans via a listener added to
     * MetricRegistry. The wrapper is not available when the listener gets called and by keeping enabled status
     * separately, we can check whether the metric should be filtered without having the metric wrapper
     */
    private static class MetricWrapper {

        private final Level level;
        private final String name;
        private Boolean enabled;
        private AbstractMetric metric;

        private MetricWrapper(String name, Level level, Boolean enabled) {
            this.name = name;
            this.level = level;
            this.enabled = enabled;
        }
    }

//    /**
//     * Builder for creating the {@link MetricService}
//     */
//    public static class Builder {
//
//        private static final String ENABLED = "Enabled";
//
//        private boolean enabled;
//
//        private Level rootLevel;
//
//        private Set<ReporterBuilder<? extends Reporter>> reporterBuilders = new HashSet<>();
//
//        public Builder setEnabled(final boolean enabled) {
//            this.enabled = enabled;
//            return this;
//        }
//
//        public Builder setRootLevel(final Level rootLevel) {
//            this.rootLevel = rootLevel;
//            return this;
//        }
//
//        public Builder addReporterBuilder(final ReporterBuilder<? extends Reporter> reporterBuilder) {
//            this.reporterBuilders.add(reporterBuilder);
//            return this;
//        }
//
//        public Builder configure(final MetricsConfiguration configuration) {
//            enabled = Boolean.valueOf(configuration.getProperty(ENABLED));
//            return this;
//        }
//
//        public MetricService build(final MetricsLevelConfig levelConfiguration) {
//            return new MetricService(enabled, rootLevel, levelConfiguration, reporterBuilders);
//        }
//    }


    /**
     * Initializes singleton. {@link MetricServiceHolder} is loaded on the first execution of { or the first access to
     * {@link MetricServiceHolder#INSTANCE}, not before.
     */
    private static class MetricServiceHolder {
        private static final MetricService INSTANCE = new MetricService();
    }

    public static MetricService getInstance() {
        return MetricServiceHolder.INSTANCE;
    }

    private final MetricsConfig metricsConfig;


    /**
     * Initialize the MetricRegistry, Level and Reporters
     */
    // Private constructor. Prevents instantiation from other classes.
    private MetricService() {
        // Initialize Metric Registry
        metricRegistry = new MetricRegistry();

        // Read configurations
        metricsConfig = MetricsConfigBuilder.build();
        levelConfiguration = MetricsLevelConfigBuilder.build();

        // Set enabled from the config
        boolean enabled = metricsConfig.isEnabled();

        // Highest priority is given for the System Properties
        String metricsEnabledProperty = System.getProperty(SYSTEM_PROPERTY_METRICS_ENABLED);
        if (metricsEnabledProperty != null && !metricsEnabledProperty.trim().isEmpty()) {
            enabled = Boolean.valueOf(metricsEnabledProperty);
        }

        Optional<Level> rootLevel = Optional.empty();
        String rootLevelProperty = System.getProperty(SYSTEM_PROPERTY_METRICS_ROOT_LEVEL);
        if (rootLevelProperty != null && !rootLevelProperty.trim().isEmpty()) {
            rootLevel = Optional.ofNullable(Level.getLevel(rootLevelProperty));
        }

        if (rootLevel.isPresent()) {
            levelConfiguration.setRootLevel(rootLevel.get());
        }

        MetricFilter enabledMetricFilter = new EnabledMetricFilter();

        // Build all reporters
        for (ReporterBuilder<? extends Reporter> reporterBuilder : metricsConfig.getReporting().getReporterBuilders()) {
            try {
                Optional<? extends Reporter> reporter = reporterBuilder.build(metricRegistry, enabledMetricFilter);
                if (reporter.isPresent()) {
                    reporters.add(reporter.get());
                }
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

    /**
     * Enables the Metrics Feature
     */
    public void enable() {
        setEnabled(true);
    }

    /**
     * Disables the Metrics Feature
     */
    public void disable() {
        setEnabled(false);
    }

    /**
     * Get the current status of Metrics (Enabled/Disabled)
     *
     * @return {@code true} if the Metrics feature is enabled
     */
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
            metric.setEnabled(isMetricEnabled(metricWrapper.name, metric.getLevel(),
                    levelConfiguration.getLevel(metric.getName()), false));
        }
    }

    public MetricsConfig getMetricsConfig() {
        return metricsConfig;
    }

    @Override
    public String getLevel(String name) {
        Level level = getMetricLevel(name);
        return level != null ? level.name() : null;
    }

    @Override
    public void updateLevel(String name, String level) {
        setMetricLevel(name, Level.valueOf(level));
    }

    /**
     * Get the {@link Level} for a given metric
     *
     * @param name The name of the Metric
     * @return {@link Level} for the given metric
     */
    public Level getMetricLevel(String name) {
        if (!metricsMap.containsKey(name)) {
            throw new IllegalArgumentException("Invalid Metric Name");
        }
        return levelConfiguration.getLevel(name);
    }

    /**
     * Set a new level to the given metric
     *
     * @param name  The name of the Metric
     * @param level New {@link Level} for the Metric
     */
    public void setMetricLevel(String name, Level level) {
        MetricWrapper metricWrapper = metricsMap.get(name);
        if (metricWrapper == null) {
            throw new IllegalArgumentException("Invalid Metric Name");
        }
        Level currentLevel = levelConfiguration.getLevel(name);
        if (currentLevel == null || !currentLevel.equals(level)) {
            // Set new level only if there is no existing level or the new level is different from existing level
            levelConfiguration.setLevel(name, level);
            AbstractMetric metric = metricWrapper.metric;
            metric.setEnabled(isMetricEnabled(metricWrapper.name, metric.getLevel(), level, false));
            restartListeningReporters();
        }
    }

    /**
     * @return The current root {@link Level}
     */
    public String getRootLevel() {
        return levelConfiguration.getRootLevel().name();
    }


    public void setRootLevel(String level) {
        setRootLevel(Level.valueOf(level));
    }

    /**
     * Set a new root level to the Metrics Service
     *
     * @param level New Root {@link Level}
     */
    public void setRootLevel(Level level) {
        boolean changed = !levelConfiguration.getRootLevel().equals(level);
        levelConfiguration.setRootLevel(level);
        if (changed) {
            notifyEnabledStatus();
            restartListeningReporters();
        }
    }

    private boolean isMetricEnabled(String name, Level metricLevel, Level configLevel, boolean getFromCache) {
        MetricWrapper metricWrapper = metricsMap.get(name);
        if (!getFromCache || metricWrapper.enabled == null) {
            metricWrapper.enabled = isMetricEnabledBasedOnHierarchyLevel(name, metricLevel, configLevel);
        }
        return metricWrapper.enabled;
    }

    /**
     * Recursive method to check enabled status based on level hierarchy
     *
     * @param name        Metric Name
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
            int index = name.lastIndexOf(METRIC_PATH_DELIMITER);
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
     * Get metric for a given name
     *
     * @param name          The name of the metric
     * @param metricBuilder A {@code MetricBuilder} instance used to create the relevant metric
     * @return The existing {@code AbstractMetric}
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractMetric> T getMetric(String name, MetricBuilder<T> metricBuilder)
            throws MetricNotFoundException {
        MetricWrapper metricWrapper = metricsMap.get(name);
        if (metricWrapper != null && metricWrapper.metric != null) {
            AbstractMetric metric = metricWrapper.metric;
            if (metricBuilder.isInstance(metric)) {
                return (T) metric;
            } else {
                throw new IllegalArgumentException("The name \"" + name + "\" is used for a different type of metric");
            }
        } else {
            throw new MetricNotFoundException("Metric \"" + name + "\" is not found");
        }
    }

    /**
     * Get or create a metric
     *
     * @param level         The {@code Level} of Metric
     * @param name          The name of the metric
     * @param metricBuilder A {@code MetricBuilder} instance used to create the relevant metric
     * @return The created {@code AbstractMetric}
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractMetric> T getOrCreateMetric(String name, Level level, MetricBuilder<T> metricBuilder) {
        if (isAnnotated(name)) {
            throw new IllegalArgumentException(name + " invalid metric name (annotated)");
        }
        MetricWrapper metricWrapper = metricsMap.get(name);
        if (metricWrapper != null && metricWrapper.metric != null) {
            AbstractMetric metric = metricWrapper.metric;
            if (metricBuilder.isInstance(metric)) {
                if (level.equals(metricWrapper.level)) {
                    return (T) metric;
                } else {
                    throw new IllegalArgumentException(name + " is already used with a different level");
                }
            } else {
                throw new IllegalArgumentException(name + " is already used for a different type of metric");
            }
        } else {
            boolean enabled = isMetricEnabledBasedOnHierarchyLevel(name, level, levelConfiguration.getLevel(name));
            metricWrapper = new MetricWrapper(name, level, enabled);
            metricsMap.put(name, metricWrapper);
            T newMetric = metricBuilder.createMetric(name, level);
            metricWrapper.metric = newMetric;
            newMetric.setEnabled(enabled);
            return newMetric;
        }
    }

    /**
     * Get or create a metric collection for a given path
     *
     * @param annotatedName The annotatedName of the metric
     * @param levels        The {@code Level}s for affected metrics
     * @param metricBuilder A {@code MetricBuilder} instance used to create the relevant metric
     * @return The created {@link Metric} collection
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractMetric> Metric getOrCreateMetricCollection(String annotatedName, Level[] levels,
                                                                          MetricBuilder<T> metricBuilder)
            throws MetricNotFoundException {
        Level level = null;
        if (levels != null && !isLevelsMatch(annotatedName, levels)) {
            throw new IllegalArgumentException("number of metric levels doesn't match the annotated name");
        } else if (levels != null && levels.length > 0) {
            level = levels[levels.length - 1];
        }
        Metric metricCollection = metricsCollections.get(annotatedName);
        if (metricCollection == null) {
            String name = annotatedName.replaceAll(METRIC_AGGREGATE_ANNOTATION_REGEX, "");
            Metric metric;
            if (level != null) {
                metric = getOrCreateMetric(name, level, metricBuilder);
            } else {
                metric = getMetric(name, metricBuilder);
            }
            boolean annotated = isAnnotated(annotatedName);
            List<?> affected = getAffectedMetrics(annotatedName, levels, metricBuilder);
            if (annotated && metric instanceof Counter) {
                metricCollection = new CounterCollection((Counter) metric, (List<Counter>) affected);
                metricsCollections.put(annotatedName, metricCollection);
            } else if (annotated && metric instanceof Histogram) {
                metricCollection = new HistogramCollection((Histogram) metric, (List<Histogram>) affected);
                metricsCollections.put(annotatedName, metricCollection);
            } else if (annotated && metric instanceof Meter) {
                metricCollection = new MeterCollection((Meter) metric, (List<Meter>) affected);
                metricsCollections.put(annotatedName, metricCollection);
            } else {
                metricCollection = metric;
            }
        }
        return metricCollection;
    }

    private boolean isAnnotated(String annotatedName) {
        return annotatedName.contains(METRIC_AGGREGATE_ANNOTATION);
    }

    private boolean isLevelsMatch(String annotatedName, Level[] levels) {
        Matcher m = METRIC_AGGREGATE_ANNOTATION_PATTERN.matcher(annotatedName);
        int affectedMetrics = 0;
        while (m.find()) {
            affectedMetrics++;
        }
        // Levels count should be equals to affected metrics + current metric.
        return levels.length == affectedMetrics + 1;
    }

    /**
     * Get affected Metrics for a given hierarchy path
     *
     * @param annotatedName The annotated name of the metric
     * @param levels        The {@code Level}s for affected metrics
     * @param metricBuilder A {@code MetricBuilder} instance used to create the relevant metric
     * @return The created {@link List<Metric>} collection
     */
    private <T extends AbstractMetric> List<?> getAffectedMetrics(String annotatedName, Level[] levels,
                                                                  MetricBuilder<T> metricBuilder)
            throws MetricNotFoundException {
        boolean getOrCreate = (levels != null) && (levels.length > 0);
        int levelIndex = 0;
        int index = annotatedName.lastIndexOf(METRIC_PATH_DELIMITER);
        String annotatedPath = annotatedName.substring(0, index);
        String statName = annotatedName.substring(index + 1);

        List<T> affected = new ArrayList<>();
        String[] chunks = annotatedPath.split("\\.");
        StringBuilder builder = new StringBuilder();
        String affectedName;
        // i < chunksLength - 1, cause affected metrics for path org.wso2.product.stat-name
        // will be searched only in org.stat-name, org.wso2.stat-name.
        for (int i = 0, chunksLength = chunks.length; i < chunksLength - 1; i++) {
            String chunk = chunks[i];
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(chunk);
            if (chunk.contains(METRIC_AGGREGATE_ANNOTATION)) {
                affectedName = builder.toString().replaceAll(METRIC_AGGREGATE_ANNOTATION_REGEX, "");
                String metricName = String.format("%s.%s", affectedName, statName);
                if (getOrCreate) {
                    Level level = levels[levelIndex];
                    affected.add(getOrCreateMetric(metricName, level, metricBuilder));
                } else {
                    affected.add(getMetric(metricName, metricBuilder));
                }
                levelIndex++;
            }
        }
        return affected;
    }

    /**
     * An interface for creating a new metric
     */
    private interface MetricBuilder<T extends AbstractMetric> {
        T createMetric(String name, Level level);

        boolean isInstance(AbstractMetric metric);
    }

    /**
     * Default Metric Builder for {@code MeterImpl}
     */
    private final MetricBuilder<MeterImpl> meterBuilder = new MetricBuilder<MeterImpl>() {
        @Override
        public MeterImpl createMetric(String name, Level level) {
            return new MeterImpl(name, level, metricRegistry.meter(name));
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return MeterImpl.class.isInstance(metric);
        }
    };

    /**
     * Default Metric Builder for {@code CounterImpl}
     */
    private final MetricBuilder<CounterImpl> counterBuilder = new MetricBuilder<CounterImpl>() {
        @Override
        public CounterImpl createMetric(String name, Level level) {
            return new CounterImpl(name, level, metricRegistry.counter(name));
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return CounterImpl.class.isInstance(metric);
        }
    };

    /**
     * Default Metric Builder for {@code TimerImpl}
     */
    private final MetricBuilder<TimerImpl> timerBuilder = new MetricBuilder<TimerImpl>() {
        @Override
        public TimerImpl createMetric(String name, Level level) {
            return new TimerImpl(name, level, metricRegistry.timer(name));
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return TimerImpl.class.isInstance(metric);
        }
    };

    /**
     * Default Metric Builder for {@code HistogramImpl}
     */
    private final MetricBuilder<HistogramImpl> histogramBuilder = new MetricBuilder<HistogramImpl>() {
        @Override
        public HistogramImpl createMetric(String name, Level level) {
            return new HistogramImpl(name, level, metricRegistry.histogram(name));
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

        GaugeBuilder(Gauge<T> gauge) {
            super();
            this.gauge = gauge;
        }

        @Override
        public GaugeImpl<T> createMetric(String name, Level level) {
            GaugeImpl<T> gaugeImpl = new GaugeImpl<T>(name, level, gauge);
            metricRegistry.register(name, gaugeImpl);
            return gaugeImpl;
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return GaugeImpl.class.isInstance(metric);
        }
    }

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
        public CachedGaugeImpl<T> createMetric(String name, Level level) {
            CachedGaugeImpl<T> gaugeImpl = new CachedGaugeImpl<T>(name, level, timeout, timeoutUnit, gauge);
            metricRegistry.register(name, gaugeImpl);
            return gaugeImpl;
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return CachedGaugeImpl.class.isInstance(metric);
        }
    }

    /**
     * Get an existing {@link Meter} instance or {@link Meter}s bundle registered under a given name. If the name is not
     * annotated, it'll return a single {@link Meter} instance. Otherwise it'll return a {@link Meter} bundle. Moreover,
     * if the name is annotated, performing actions (i.e {@link Meter#mark()}) in the returned bundle will result in
     * updating all the {@link Meter}s denoted by the annotated name.
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.cep[+].executionPlan.statName)
     * @return a single {@link Meter} instance or a {@link Meter} bundle.
     * @throws MetricNotFoundException when there is no Meter for the given name.
     */
    public Meter getMeter(String name) throws MetricNotFoundException {
        if (isAnnotated(name)) {
            return (Meter) getOrCreateMetricCollection(name, null, meterBuilder);
        } else {
            return getMetric(name, meterBuilder);
        }
    }

    /**
     * Get or create a {@link Meter}s bundle registered under a given annotated name and {@link Level}s. Unlike {@link
     * #getMeter(String)}, this will create the metrics denoted by the annotated name if they do not exists. Moreover,
     * performing actions (i.e {@link Meter#mark()}) in the returned bundle will result in updating all the {@link
     * Meter}s denoted by the annotated name.
     *
     * @param name   The annotated name of the metric (i.e org.wso2.cep[+].executionPlan.statName)
     * @param levels The {@link Level}s used for each annotated metric (Number of {@code levels} and Metrics count
     *               should be equal)
     * @return a {@link Meter} bundle which wraps a collection of {@link Meter}s
     */
    public Meter meter(String name, Level... levels) {
        if (levels.length == 1) {
            return getOrCreateMetric(name, levels[0], meterBuilder);
        } else {
            try {
                return (Meter) getOrCreateMetricCollection(name, levels, meterBuilder);
            } catch (MetricNotFoundException ignored) {
                // since levels passed to getOrCreateMetricCollection
                // MetricNotFoundException could not occur, therefore ignored
                return null;
            }
        }
    }

    /**
     * Get an existing {@link Counter} instance or {@link Counter}s bundle registered under a given name. If the name is
     * not annotated, it'll return a single {@link Counter} instance. Otherwise it'll return a {@link Counter} bundle.
     * Moreover, if the name is annotated, performing actions (i.e {@link Counter#inc()}) in the returned bundle will
     * result in updating all the {@link Counter}s denoted by the annotated name.
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.cep[+].executionPlan.statName)
     * @return a single {@link Counter} instance or a {@link Counter} bundle.
     * @throws MetricNotFoundException when there is no Counter for the given name.
     */
    public Counter getCounter(String name) throws MetricNotFoundException {
        if (isAnnotated(name)) {
            return (Counter) getOrCreateMetricCollection(name, null, counterBuilder);
        } else {
            return getMetric(name, counterBuilder);
        }
    }


    /**
     * Get or create a {@link Counter}s bundle registered under a given annotated name and {@link Level}s. Unlike {@link
     * #getCounter(String)}, this will create the metrics denoted by the annotated name if they do not exists. Moreover,
     * performing actions (i.e {@link Counter#inc()}) in the returned bundle will result in updating all the {@link
     * Counter}s denoted by the annotated name.
     *
     * @param name   The annotated name of the metric (i.e org.wso2.cep[+].executionPlan.statName)
     * @param levels The {@link Level}s used for each annotated metric (Number of {@code levels} and Metrics count
     *               should be equal)
     * @return a {@link Counter} bundle which wraps a collection of {@link Counter}s
     */
    public Counter counter(String name, Level... levels) {
        if (levels.length == 1) {
            return getOrCreateMetric(name, levels[0], counterBuilder);
        } else {
            try {
                return (Counter) getOrCreateMetricCollection(name, levels, counterBuilder);
            } catch (MetricNotFoundException ignored) {
                // since levels passed to getOrCreateMetricCollection
                // MetricNotFoundException could not occur, therefore ignored
                return null;
            }
        }
    }


    /**
     * Get an existing {@link Timer} instance or {@link Timer}s bundle registered under a given name. If the name is not
     * annotated, it'll return a single {@link Timer} instance. Otherwise it'll return a {@link Timer} bundle. Moreover,
     * if the name is annotated, performing actions (i.e {@link Timer#update(long, TimeUnit)}) in the returned bundle
     * will result in updating all the {@link Timer}s denoted by the annotated name.
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.cep[+].executionPlan.statName)
     * @return a single {@link Timer} instance or a {@link Timer} bundle.
     * @throws MetricNotFoundException when there is no Timer for the given name.
     */
    public Timer getTimer(String name) throws MetricNotFoundException {
        return getMetric(name, timerBuilder);
    }

    /**
     * Get or create a {@link Timer} instance for the given name
     *
     * @param name  The name of the metric
     * @param level The {@link Level} used for metric
     * @return a {@link Timer} instance
     */
    public Timer timer(String name, Level level) {
        return getOrCreateMetric(name, level, timerBuilder);
    }

    /**
     * Get an existing {@link Histogram} instance or {@link Histogram}s bundle registered under a given name. If the
     * name is not annotated, it'll return a single {@link Histogram} instance. Otherwise it'll return a {@link
     * Histogram} bundle. Moreover, if the name is annotated, performing actions (i.e {@link Histogram#update(int)}) in
     * the returned bundle will result in updating all the {@link Histogram}s denoted by the annotated name.
     *
     * @param name The name of the metric (This name can be annotated i.e org.wso2.cep[+].executionPlan.statName)
     * @return a single {@link Histogram} instance or a {@link Histogram} bundle.
     * @throws MetricNotFoundException when there is no Histogram for the given name.
     */
    public Histogram getHistogram(String name) throws MetricNotFoundException {
        if (isAnnotated(name)) {
            return (Histogram) getOrCreateMetricCollection(name, null, histogramBuilder);
        } else {
            return getMetric(name, histogramBuilder);
        }
    }

    /**
     * Get or create a {@link Histogram}s bundle registered under a given annotated name and {@link Level}s. Unlike
     * {@link #getHistogram(String)}, this will create the metrics denoted by the annotated name if they do not exists.
     * Moreover, performing actions (i.e {@link Histogram#update(int)}) in the returned bundle will result in updating
     * all the {@link Histogram}s denoted by the annotated name.
     *
     * @param name   The annotated name of the metric (i.e org.wso2.cep[+].executionPlan.statName)
     * @param levels The {@link Level}s used for each annotated metric (Number of {@code levels} and Metrics count
     *               should be equal)
     * @return a {@link Histogram} bundle which wraps a collection of {@link Histogram}s
     */
    public Histogram histogram(String name, Level... levels) {
        if (levels.length == 1) {
            return getOrCreateMetric(name, levels[0], histogramBuilder);
        } else {
            try {
                return (Histogram) getOrCreateMetricCollection(name, levels, histogramBuilder);
            } catch (MetricNotFoundException ignored) {
                // since levels passed to getOrCreateMetricCollection
                // MetricNotFoundException could not occur, therefore ignored
                return null;
            }
        }
    }

    /**
     * Get or create a {@link Gauge} for the given name
     *
     * @param <T>   The type of the value used in the {@link Gauge}
     * @param name  The name of the metric
     * @param level The {@link Level} used for metric
     * @param gauge An implementation of {@link Gauge}
     */
    public <T> void gauge(String name, Level level, Gauge<T> gauge) {
        getOrCreateMetric(name, level, new GaugeBuilder<T>(gauge));
    }

    /**
     * Get or create a cached {@link Gauge} for the given name
     *
     * @param <T>         The type of the value used in the {@link Gauge}
     * @param name        The name of the metric
     * @param level       The {@link Level} used for metric
     * @param timeout     the timeout
     * @param timeoutUnit the unit of {@code timeout}
     * @param gauge       An implementation of {@link Gauge}
     */
    public <T> void cachedGauge(String name, Level level, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        getOrCreateMetric(name, level, new CachedGaugeBuilder<T>(gauge, timeout, timeoutUnit));
    }

    /**
     * Return the number of metrics used
     *
     * @return The metrics count
     */
    public int getMetricsCount() {
        return metricsMap.size();
    }

    private void registerJVMMetrics() {
        registerAll(Level.INFO, "jvm.memory", new MemoryUsageGaugeSet());
        registerAll(Level.INFO, "jvm.os", new OperatingSystemMetricSet());
        registerAll(Level.INFO, "jvm.class-loading", new ClassLoadingGaugeSet());
        registerAll(Level.DEBUG, "jvm.gc", new GarbageCollectorMetricSet());
        registerAll(Level.DEBUG, "jvm.threads", new ThreadStatesGaugeSet());
        registerAll(Level.TRACE, "jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
    }

    private void registerAll(Level level, String prefix, MetricSet metrics) throws IllegalArgumentException {
        for (Map.Entry<String, com.codahale.metrics.Metric> entry : metrics.getMetrics().entrySet()) {
            if (entry.getValue() instanceof MetricSet) {
                registerAll(level, MetricRegistry.name(prefix, entry.getKey()), (MetricSet) entry.getValue());
            } else if (filterJVMMetric(entry.getKey())) {
                String name = MetricRegistry.name(prefix, entry.getKey());
                com.codahale.metrics.Metric metric = entry.getValue();
                if (metric instanceof com.codahale.metrics.Gauge) {
                    com.codahale.metrics.Gauge<?> gauge = (com.codahale.metrics.Gauge<?>) metric;
                    gauge(name, level, new JVMGaugeWrapper(gauge));
                } else {
                    logger.warn(String.format("Unexpected Metric found. Name: %s, Class: %s", name, metric.getClass()));
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
    /**
     * Invoke report method of all scheduled reporters.
     */
    public void report() {
        reporters.stream().filter(reporter -> reporter instanceof ScheduledReporter).forEach(reporter -> {
            ((ScheduledReporter) reporter).report();
        });
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
        reporters.stream().filter(reporter -> reporter instanceof ListeningReporter).forEach(reporter -> {
            ListeningReporter listeningReporter = (ListeningReporter) reporter;
            listeningReporter.stop();
            listeningReporter.start();
        });
    }

    /**
     * A {@code MetricFilter} to filter metrics based on enabled status
     */
    private class EnabledMetricFilter implements MetricFilter {
        @Override
        public boolean matches(String name, com.codahale.metrics.Metric metric) {
            MetricWrapper metricWrapper = metricsMap.get(name);
            return metricWrapper != null &&
                    isMetricEnabled(metricWrapper.name, metricWrapper.level, levelConfiguration.getLevel(name), true);
        }
    }

}