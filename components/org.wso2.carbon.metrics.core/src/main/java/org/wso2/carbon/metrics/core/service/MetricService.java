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
package org.wso2.carbon.metrics.core.service;

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
import org.wso2.carbon.metrics.core.Gauge;
import org.wso2.carbon.metrics.core.Histogram;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.Metric;
import org.wso2.carbon.metrics.core.MetricNotFoundException;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.carbon.metrics.core.config.model.MetricsConfig;
import org.wso2.carbon.metrics.core.config.model.MetricsLevelConfig;
import org.wso2.carbon.metrics.core.jmx.MetricManagerMXBean;
import org.wso2.carbon.metrics.core.metric.ClassLoadingGaugeSet;
import org.wso2.carbon.metrics.core.metric.OperatingSystemMetricSet;
import org.wso2.carbon.metrics.core.reporter.ListeningReporter;
import org.wso2.carbon.metrics.core.reporter.Reporter;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.metrics.core.reporter.ScheduledReporter;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation class for {@link MetricService}, which will use the Metrics (https://dropwizard.github.io/metrics)
 * library for creating various metrics
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
    private final ConcurrentMap<String, Metric> metricCollectionsMap = new ConcurrentHashMap<>();

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
    private static final String METRIC_PATH_DELIMITER = ".";

    /**
     * Hierarchy delimiter regex in Metric name
     */
    private static final String METRIC_PATH_DELIMITER_REGEX = "\\.";

    private final MetricsConfig metricsConfig;

    private final MetricsLevelConfig metricsLevelConfig;

    private final MetricFilter enabledMetricFilter = new EnabledMetricFilter();

    private final Map<String, Reporter> reporterMap = new ConcurrentHashMap<>();

    private static final Pattern METRIC_AGGREGATE_ANNOTATION_PATTERN = Pattern.compile("^(.+)\\[\\+\\]$");

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


    /**
     * Constructs a Metric Service with given {@link MetricRegistry} and other configurations.
     *
     * @param metricRegistry     The main {@link MetricRegistry} used by the MetricService.
     * @param metricsConfig      The {@link MetricsConfig} with main and reporter configurations.
     * @param metricsLevelConfig The {@link MetricsLevelConfig} with root level configuration and level configurations
     *                           for each metric.
     */
    public MetricService(MetricRegistry metricRegistry, MetricsConfig metricsConfig,
                         MetricsLevelConfig metricsLevelConfig) {
        this.metricRegistry = metricRegistry;
        this.metricsConfig = metricsConfig;
        this.metricsLevelConfig = metricsLevelConfig;

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
            metricsLevelConfig.setRootLevel(rootLevel.get());
        }

        // Build all reporters
        metricsConfig.getReporting().getReporterBuilders().forEach(reporterBuilder -> {
            try {
                addReporter(reporterBuilder);
            } catch (ReporterBuildException e) {
                logger.warn("Reporter build failed", e);
            }
        });

        // Set enabled
        setEnabled(enabled);

        // Register JVM Metrics
        // This should be the last method when initializing MetricService
        registerJVMMetrics();
    }

    public <T extends ReporterBuilder> void addReporter(T reporterBuilder) throws ReporterBuildException {
        Optional<? extends Reporter> reporter = reporterBuilder.build(metricRegistry, enabledMetricFilter);
        if (reporter.isPresent()) {
            Reporter r = reporter.get();
            Reporter previousReporter = reporterMap.put(r.getName(), r);
            if (previousReporter != null) {
                previousReporter.stop();
            }
        }
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
        }
    }

    private void notifyEnabledStatus() {
        for (MetricWrapper metricWrapper : metricsMap.values()) {
            AbstractMetric metric = metricWrapper.metric;
            metric.setEnabled(isMetricEnabled(metricWrapper.name, metric.getLevel(),
                    metricsLevelConfig.getLevel(metric.getName()), false));
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
    public void setLevel(String name, String level) {
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
        return metricsLevelConfig.getLevel(name);
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
        Level currentLevel = metricsLevelConfig.getLevel(name);
        if (currentLevel == null || !currentLevel.equals(level)) {
            // Set new level only if there is no existing level or the new level is different from existing level
            metricsLevelConfig.setLevel(name, level);
            AbstractMetric metric = metricWrapper.metric;
            metric.setEnabled(isMetricEnabled(metricWrapper.name, metric.getLevel(), level, false));
            restartListeningReporters();
        }
    }

    /**
     * @return The current root {@link Level}
     */
    public String getRootLevel() {
        return metricsLevelConfig.getRootLevel().name();
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
        boolean changed = !metricsLevelConfig.getRootLevel().equals(level);
        metricsLevelConfig.setRootLevel(level);
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
            return this.enabled && configLevel.compareTo(metricLevel) >= 0
                    && configLevel.compareTo(Level.OFF) > 0;
        } else {
            String parentName;
            int index = name.lastIndexOf(METRIC_PATH_DELIMITER);
            if (index != -1) {
                parentName = name.substring(0, index);
                configLevel = metricsLevelConfig.getLevel(parentName);
            } else {
                parentName = ROOT_METRIC_NAME;
                configLevel = metricsLevelConfig.getRootLevel();
            }
            return isMetricEnabledBasedOnHierarchyLevel(parentName, metricLevel, configLevel);
        }
    }

    /**
     * Get metric for a given name
     *
     * @param name The name of the metric
     * @return The existing {@code AbstractMetric}
     */
    @SuppressWarnings("unchecked")
    private <T extends Metric> T getMetric(String name, Class metricClass) throws MetricNotFoundException {
        Metric metric = null;
        MetricWrapper metricWrapper = metricsMap.get(name);
        if (metricWrapper != null && metricWrapper.metric != null) {
            metric = metricWrapper.metric;
        }

        if (metric == null) {
            metric = metricCollectionsMap.get(name);
        }

        if (metric == null) {
            throw new MetricNotFoundException("Metric \"" + name + "\" is not found");
        }
        if (!metricClass.isInstance(metric)) {
            throw new IllegalArgumentException("The name \"" + name + "\" is used for a different type of metric");
        }
        return (T) metric;
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
            throw new IllegalArgumentException("The metric name should not be annotated");
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
            boolean enabled = isMetricEnabledBasedOnHierarchyLevel(name, level, metricsLevelConfig.getLevel(name));
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
     * @param name          The name of the metric
     * @param levels        The {@code Level}s for affected metrics
     * @param metricBuilder A {@code MetricBuilder} instance used to create the relevant metric
     * @return The created {@link Metric} collection
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractMetric, M extends Metric> M getOrCreateMetricCollection(
            String name, Level[] levels, MetricBuilder<T> metricBuilder,
            MetricCollectionBuilder<M, T> metricCollectionBuilder) {
        String[] metricNames = getMetricHierarchyNames(name);
        if (levels.length != metricNames.length) {
            throw new IllegalArgumentException("The metric levels don't match the annotated name");
        }
        Metric metricCollection = metricCollectionsMap.get(name);
        if (metricCollection != null && !metricCollectionBuilder.isInstance(metricCollection)) {
            throw new IllegalArgumentException(name + " is already used for a different type of metric collection");
        } else {
            metricCollection = metricCollectionBuilder.createMetricCollection(metricNames, levels, metricBuilder);
            metricCollectionsMap.put(name, metricCollection);
        }
        return (M) metricCollection;
    }

    private boolean isAnnotated(String name) {
        String[] nameParts = name.split(METRIC_PATH_DELIMITER_REGEX);
        for (int i = 0; i < nameParts.length; i++) {
            Matcher matcher = METRIC_AGGREGATE_ANNOTATION_PATTERN.matcher(nameParts[i]);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    private String[] getMetricHierarchyNames(String name) {
        String[] nameParts = name.split(METRIC_PATH_DELIMITER_REGEX);
        if (nameParts.length < 3) {
            throw new IllegalArgumentException("At least three parts should be there in the annotated metric name \""
                    + name + "\".");
        }
        for (int i = 1; i <= 2; i++) {
            // Check last two parts
            if (METRIC_AGGREGATE_ANNOTATION_PATTERN.matcher(nameParts[nameParts.length - i]).find()) {
                throw new IllegalArgumentException("The last two parts of the metric name \"" + name
                        + "\" should not be annotated.");
            }
        }

        String metricName = nameParts[nameParts.length - 1];
        StringBuilder parentNameBuilder = new StringBuilder();
        List<String> childNames = new ArrayList<>();

        for (int i = 0; i < nameParts.length; i++) {
            Matcher matcher = METRIC_AGGREGATE_ANNOTATION_PATTERN.matcher(nameParts[i]);
            if (i > 0) {
                parentNameBuilder.append(METRIC_PATH_DELIMITER);
            }
            if (matcher.find()) {
                parentNameBuilder.append(matcher.group(1));
                childNames.add(String.format("%s.%s", parentNameBuilder.toString(), metricName));
            } else {
                parentNameBuilder.append(nameParts[i]);
            }
        }

        String parentName = parentNameBuilder.toString();

        String[] names = new String[childNames.size() + 1];
        names[0] = parentName;
        for (int i = 0; i < childNames.size(); i++) {
            names[i + 1] = childNames.get(i);
        }
        return names;
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
            GaugeImpl<T> gaugeImpl = new GaugeImpl<>(name, level, gauge);
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

        CachedGaugeBuilder(Gauge<T> gauge, long timeout, TimeUnit timeoutUnit) {
            super();
            this.gauge = gauge;
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
        }

        @Override
        public CachedGaugeImpl<T> createMetric(String name, Level level) {
            CachedGaugeImpl<T> gaugeImpl = new CachedGaugeImpl<>(name, level, timeout, timeoutUnit, gauge);
            metricRegistry.register(name, gaugeImpl);
            return gaugeImpl;
        }

        @Override
        public boolean isInstance(AbstractMetric metric) {
            return CachedGaugeImpl.class.isInstance(metric);
        }
    }

    /**
     * An interface for creating a new metric
     */
    private interface MetricCollectionBuilder<T extends Metric, M extends AbstractMetric> {
        T createMetricCollection(String[] names, Level[] levels, MetricBuilder<M> metricBuilder);

        boolean isInstance(Metric metric);
    }

    /**
     * Default Metric Collection Builder for {@code Counter}
     */
    private final MetricCollectionBuilder<Counter, CounterImpl> counterCollectionBuilder =
            new MetricCollectionBuilder<Counter, CounterImpl>() {

                @Override
                public Counter createMetricCollection(String[] names, Level[] levels,
                                                      MetricBuilder<CounterImpl> metricBuilder) {
                    Counter parentCounter = getOrCreateMetric(names[0], levels[0], metricBuilder);
                    List<Counter> childCounters = new ArrayList<>(names.length - 1);
                    for (int i = 1; i < names.length; i++) {
                        childCounters.add(getOrCreateMetric(names[i], levels[i], metricBuilder));
                    }
                    return new CounterCollection(parentCounter, childCounters);
                }

                @Override
                public boolean isInstance(Metric metric) {
                    return Counter.class.isInstance(metric);
                }

            };

    /**
     * Default Metric Collection Builder for {@code Meter}
     */
    private final MetricCollectionBuilder<Meter, MeterImpl> meterCollectionBuilder =
            new MetricCollectionBuilder<Meter, MeterImpl>() {

                @Override
                public Meter createMetricCollection(String[] names, Level[] levels,
                                                    MetricBuilder<MeterImpl> metricBuilder) {
                    Meter parentMeter = getOrCreateMetric(names[0], levels[0], metricBuilder);
                    List<Meter> childMeters = new ArrayList<>(names.length - 1);
                    for (int i = 1; i < names.length; i++) {
                        childMeters.add(getOrCreateMetric(names[i], levels[i], metricBuilder));
                    }
                    return new MeterCollection(parentMeter, childMeters);
                }

                @Override
                public boolean isInstance(Metric metric) {
                    return Meter.class.isInstance(metric);
                }

            };

    /**
     * Default Metric Collection Builder for {@code Histogram}
     */
    private final MetricCollectionBuilder<Histogram, HistogramImpl> histogramCollectionBuilder =
            new MetricCollectionBuilder<Histogram, HistogramImpl>() {

                @Override
                public Histogram createMetricCollection(String[] names, Level[] levels,
                                                        MetricBuilder<HistogramImpl> metricBuilder) {
                    Histogram parentHistogram = getOrCreateMetric(names[0], levels[0], metricBuilder);
                    List<Histogram> childHistograms = new ArrayList<>(names.length - 1);
                    for (int i = 1; i < names.length; i++) {
                        childHistograms.add(getOrCreateMetric(names[i], levels[i], metricBuilder));
                    }
                    return new HistogramCollection(parentHistogram, childHistograms);
                }

                @Override
                public boolean isInstance(Metric metric) {
                    return Histogram.class.isInstance(metric);
                }

            };

    private Level[] levels(Level level, Level[] levels) {
        Level[] levelArray = new Level[levels.length + 1];
        System.arraycopy(levels, 0, levelArray, 1, levels.length);
        levelArray[0] = level;
        return levelArray;
    }

    /**
     * <p>Get an existing {@link Meter} instance or {@link Meter}s bundle registered under a given name.</p>
     *
     * @param name The name of the metric (This name can be annotated. eg. org.wso2.parent[+].child.metric)
     * @return a single {@link Meter} instance or a {@link Meter} bundle.
     * @throws MetricNotFoundException when there is no {@link Meter} for the given name.
     * @see #meter(String, Level, Level...)
     */
    public Meter getMeter(String name) throws MetricNotFoundException {
        return getMetric(name, Meter.class);
    }

    /**
     * <p>Get or create a {@link Meter} instance or a {@link Meter} bundle registered under given name.</p> <p>The name
     * must be annotated with "[+]" to get or create a {@link Meter} bundle. The number of {@link Level}s must match the
     * number of {@link Meter}s in the {@link Meter} bundle. In a {@link Meter} bundle, any action performed will be
     * delegated to the {@link Meter}s denoted by the annotated name.</p>
     *
     * @param name   The name of the metric (This name can be annotated to get or create a {@link Meter} bundle. eg.
     *               org.wso2.parent[+].child.metric)
     * @param level  The {@link Level} used for the metric
     * @param levels The additional {@link Level}s used for each annotated metric (The total number of {@link Level}s
     *               and the metrics in a bundle should be equal)
     * @return a {@link Meter} or a {@link Meter} bundle if the name is annotated
     * @see #getMeter(String)
     */
    public Meter meter(String name, Level level, Level... levels) {
        if (levels.length == 0) {
            return getOrCreateMetric(name, level, meterBuilder);
        } else {
            return getOrCreateMetricCollection(name, levels(level, levels), meterBuilder, meterCollectionBuilder);
        }
    }

    /**
     * <p>Get an existing {@link Counter} instance or {@link Counter}s bundle registered under a given name.</p>
     *
     * @param name The name of the metric (This name can be annotated. eg. org.wso2.parent[+].child.metric)
     * @return a single {@link Counter} instance or a {@link Counter} bundle.
     * @throws MetricNotFoundException when there is no {@link Counter} for the given name.
     * @see #counter(String, Level, Level...)
     */
    public Counter getCounter(String name) throws MetricNotFoundException {
        return getMetric(name, Counter.class);
    }


    /**
     * <p>Get or create a {@link Counter} instance or a {@link Counter} bundle registered under given name.</p> <p>The
     * name must be annotated with "[+]" to get or create a {@link Counter} bundle. The number of {@link Level}s must
     * match the number of {@link Counter}s in the {@link Counter} bundle. In a {@link Counter} bundle, any action
     * performed will be delegated to the {@link Counter}s denoted by the annotated name.</p>
     *
     * @param name   The name of the metric (This name can be annotated to get or create a {@link Counter} bundle. eg.
     *               org.wso2.parent[+].child.metric)
     * @param level  The {@link Level} used for the metric
     * @param levels The additional {@link Level}s used for each annotated metric (The total number of {@link Level}s
     *               and the metrics in a bundle should be equal)
     * @return a {@link Counter} or a {@link Counter} bundle if the name is annotated
     * @see #getCounter(String)
     */
    public Counter counter(String name, Level level, Level... levels) {
        if (levels.length == 0) {
            return getOrCreateMetric(name, level, counterBuilder);
        } else {
            return getOrCreateMetricCollection(name, levels(level, levels), counterBuilder, counterCollectionBuilder);
        }
    }


    /**
     * <p>Get the {@link Timer} instance registered under given name.</p>
     *
     * @param name The name of the metric
     * @return a {@link Timer} instance
     * @throws MetricNotFoundException when there is no  {@link Timer} for the given name.
     * @see #timer(String, Level)
     */
    public Timer getTimer(String name) throws MetricNotFoundException {
        return getMetric(name, Timer.class);
    }

    /**
     * <p>Get or create a {@link Timer} instance registered under given name.</p>
     *
     * @param name  The name of the metric
     * @param level The {@link Level} used for metric
     * @return a {@link Timer} instance
     * @see #getTimer(String)
     */
    public Timer timer(String name, Level level) {
        return getOrCreateMetric(name, level, timerBuilder);
    }

    /**
     * <p>Get an existing {@link Histogram} instance or {@link Histogram}s bundle registered under a given name.</p>
     *
     * @param name The name of the metric (This name can be annotated. eg. org.wso2.parent[+].child.metric)
     * @return a single {@link Histogram} instance or a {@link Histogram} bundle.
     * @throws MetricNotFoundException when there is no {@link Histogram} for the given name.
     * @see #histogram(String, Level, Level...)
     */
    public Histogram getHistogram(String name) throws MetricNotFoundException {
        return getMetric(name, Histogram.class);
    }

    /**
     * <p>Get or create a {@link Histogram} instance or a {@link Histogram} bundle registered under given name.</p>
     * <p>The name must be annotated with "[+]" to get or create a {@link Histogram} bundle. The number of {@link
     * Level}s must match the number of {@link Histogram}s in the {@link Histogram} bundle. In a {@link Histogram}
     * bundle, any action performed will be delegated to the {@link Histogram}s denoted by the annotated name.</p>
     *
     * @param name   The name of the metric (This name can be annotated to get or create a {@link Histogram} bundle. eg.
     *               org.wso2.parent[+].child.metric)
     * @param level  The {@link Level} used for the metric
     * @param levels The additional {@link Level}s used for each annotated metric (The total number of {@link Level}s
     *               and the metrics in a bundle should be equal)
     * @return a {@link Histogram} or a {@link Histogram} bundle if the name is annotated
     * @see #getHistogram(String)
     */
    public Histogram histogram(String name, Level level, Level... levels) {
        if (levels.length == 0) {
            return getOrCreateMetric(name, level, histogramBuilder);
        } else {
            return getOrCreateMetricCollection(name, levels(level, levels), histogramBuilder,
                    histogramCollectionBuilder);
        }
    }

    /**
     * Register a {@link Gauge} instance under given name
     *
     * @param <T>   The type of the value used in the {@link Gauge}
     * @param name  The name of the metric
     * @param level The {@link Level} used for metric
     * @param gauge An implementation of {@link Gauge}
     * @see #cachedGauge(String, Level, long, TimeUnit, Gauge)
     */
    public <T> void gauge(String name, Level level, Gauge<T> gauge) {
        getOrCreateMetric(name, level, new GaugeBuilder<>(gauge));
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
     */
    public <T> void cachedGauge(String name, Level level, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        getOrCreateMetric(name, level, new CachedGaugeBuilder<>(gauge, timeout, timeoutUnit));
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
        registerAllJVMMetrics(Level.INFO, "jvm.memory", new MemoryUsageGaugeSet());
        registerAllJVMMetrics(Level.INFO, "jvm.os", new OperatingSystemMetricSet());
        registerAllJVMMetrics(Level.INFO, "jvm.class-loading", new ClassLoadingGaugeSet());
        registerAllJVMMetrics(Level.DEBUG, "jvm.gc", new GarbageCollectorMetricSet());
        registerAllJVMMetrics(Level.DEBUG, "jvm.threads", new ThreadStatesGaugeSet());
        registerAllJVMMetrics(Level.TRACE, "jvm.buffers",
                new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
    }

    private void registerAllJVMMetrics(Level level, String prefix, MetricSet metrics) throws IllegalArgumentException {
        for (Map.Entry<String, com.codahale.metrics.Metric> entry : metrics.getMetrics().entrySet()) {
            if (filterJVMMetric(entry.getKey())) {
                String name = MetricRegistry.name(prefix, entry.getKey());
                com.codahale.metrics.Metric metric = entry.getValue();
                if (metric instanceof com.codahale.metrics.Gauge) {
                    com.codahale.metrics.Gauge<?> gauge = (com.codahale.metrics.Gauge<?>) metric;
                    gauge(name, level, new JVMGaugeWrapper(gauge));
                }
            }
        }
    }

    private boolean filterJVMMetric(String name) {
        // Remove "deadlocks" as it is a String Set.
        return !"deadlocks".equals(name);
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
     * Invoke report method of all scheduled reporters.
     */
    public void report() {
        reporterMap.values().stream().filter(reporter -> reporter instanceof ScheduledReporter)
                .forEach(reporter -> ((ScheduledReporter) reporter).report());
    }

    private Reporter getReporter(String name) {
        Reporter reporter = reporterMap.get(name);
        if (reporter == null) {
            throw new IllegalArgumentException("Invalid Reporter Name");
        }
        return reporter;
    }

    @Override
    public void startReporter(String name) {
        getReporter(name).start();
    }

    @Override
    public void stopReporter(String name) {
        getReporter(name).stop();
    }

    @Override
    public boolean isReporterRunning(String name) {
        return getReporter(name).isRunning();
    }

    @Override
    public void startReporters() {
        reporterMap.values().forEach(reporter -> {
            try {
                reporter.start();
            } catch (Throwable e) {
                logger.error("Error when starting the reporter", e);
            }
        });
    }

    @Override
    public void stopReporters() {
        reporterMap.values().forEach(reporter -> {
            try {
                reporter.stop();
            } catch (Throwable e) {
                logger.error("Error when stopping the reporter", e);
            }
        });
    }

    private void restartListeningReporters() {
        reporterMap.values().stream()
                .filter(reporter -> reporter instanceof ListeningReporter)
                .filter(reporter -> reporter.isRunning())
                .forEach(reporter -> {
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
                    isMetricEnabled(metricWrapper.name, metricWrapper.level, metricsLevelConfig.getLevel(name), true);
        }
    }

}
