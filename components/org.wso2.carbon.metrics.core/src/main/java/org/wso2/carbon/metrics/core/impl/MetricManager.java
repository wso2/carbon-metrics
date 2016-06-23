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
import org.wso2.carbon.metrics.core.Counter;
import org.wso2.carbon.metrics.core.Gauge;
import org.wso2.carbon.metrics.core.Histogram;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.Metric;
import org.wso2.carbon.metrics.core.MetricNotFoundException;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.carbon.metrics.core.config.model.MetricsLevelConfig;
import org.wso2.carbon.metrics.core.impl.listener.EnabledStatusChangeListener;
import org.wso2.carbon.metrics.core.impl.listener.MetricLevelChangeListener;
import org.wso2.carbon.metrics.core.impl.listener.RootLevelChangeListener;
import org.wso2.carbon.metrics.core.metric.ClassLoadingGaugeSet;
import org.wso2.carbon.metrics.core.metric.OperatingSystemMetricSet;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link MetricManager} implementation using the Metrics (https://dropwizard.github.io/metrics) library for
 * creating various metrics
 */
public final class MetricManager {

    /**
     * Keep all metrics created via this Metrics Manager
     */
    private final ConcurrentMap<String, MetricWrapper> metricsMap = new ConcurrentHashMap<>();

    /**
     * Keep all metric collections created via this impl
     */
    private final ConcurrentMap<String, Metric> metricCollectionsMap = new ConcurrentHashMap<>();

    /**
     * Metrics feature enabling flag. This flag should be initially updated from the configuration.
     */
    private boolean enabled;

    /**
     * The {@link MetricRegistry} instance from the Metrics Implementation
     */
    private final MetricRegistry metricRegistry;

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

    private final MetricsLevelConfig metricsLevelConfig;

    private final MetricFilter enabledMetricFilter = new EnabledMetricFilter();

    private static final Pattern METRIC_AGGREGATE_ANNOTATION_PATTERN = Pattern.compile("^(.+)\\[\\+\\]$");

    private final List<EnabledStatusChangeListener> enabledStatusChangeListeners;

    private final List<RootLevelChangeListener> rootLevelChangeListeners;

    private final List<MetricLevelChangeListener> metricLevelChangeListeners;

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
     * @param metricsLevelConfig The {@link MetricsLevelConfig} with root level configuration and level configurations
     *                           for each metric.
     */
    public MetricManager(MetricRegistry metricRegistry, MetricsLevelConfig metricsLevelConfig) {
        this.metricRegistry = metricRegistry;
        this.metricsLevelConfig = metricsLevelConfig;
        this.enabledStatusChangeListeners = new CopyOnWriteArrayList<>();
        this.rootLevelChangeListeners = new CopyOnWriteArrayList<>();
        this.metricLevelChangeListeners = new CopyOnWriteArrayList<>();

        // Register JVM Metrics
        // This should be the done when other initializations are completed
        registerJVMMetrics();

        addEnabledStatusChangeListener(enabledStatus -> notifyEnabledStatus());
        addRootLevelChangeListener((oldLevel, newLevel) -> notifyEnabledStatus());
        addMetricLevelChangeListener((metric, oldLevel, newLevel) ->
                metric.setEnabled(isMetricEnabled(metric.getName(), metric.getLevel(), newLevel, false)));
    }

    /**
     * Get the {@link MetricRegistry} used to create metrics. This is used to create Metric Reporters
     *
     * @return The {@link MetricRegistry} used by the {@link MetricManager}
     */
    MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    /**
     * Get the {@link MetricFilter} used to filter disabled metrics. This filter is used in Metric Reporters
     *
     * @return The {@link MetricFilter} used to filter disabled metrics
     */
    MetricFilter getEnabledMetricFilter() {
        return enabledMetricFilter;
    }

    /**
     * Adds a {@link EnabledStatusChangeListener} to a collection of listeners that will be notified if the enabled
     * status was changed.  Listeners will be notified in the order in which they are added.
     *
     * @param listener the listener that will be notified
     */
    public void addEnabledStatusChangeListener(EnabledStatusChangeListener listener) {
        enabledStatusChangeListeners.add(listener);
    }

    /**
     * Removes a {@link EnabledStatusChangeListener} from this Metric Manager's collection of enabled status change
     * listeners.
     *
     * @param listener the listener that will be removed
     */
    public void removeEnabledStatusChangeListener(EnabledStatusChangeListener listener) {
        enabledStatusChangeListeners.remove(listener);
    }

    /**
     * Adds a {@link RootLevelChangeListener} to a collection of listeners that will be notified if the root level was
     * changed.  Listeners will be notified in the order in which they are added.
     *
     * @param listener the listener that will be notified
     */
    public void addRootLevelChangeListener(RootLevelChangeListener listener) {
        rootLevelChangeListeners.add(listener);
    }

    /**
     * Removes a {@link RootLevelChangeListener} from this Metric Manager's collection of root level change
     * listeners.
     *
     * @param listener the listener that will be removed
     */
    public void removeRootLevelChangeListener(RootLevelChangeListener listener) {
        rootLevelChangeListeners.remove(listener);
    }

    /**
     * Adds a {@link MetricLevelChangeListener} to a collection of listeners that will be notified if the level of a
     * particular metric was changed.  Listeners will be notified in the order in which they are added.
     *
     * @param listener the listener that will be notified
     */
    public void addMetricLevelChangeListener(MetricLevelChangeListener listener) {
        metricLevelChangeListeners.add(listener);
    }

    /**
     * Removes a {@link MetricLevelChangeListener} from this Metric Manager's collection of metric level change
     * listeners.
     *
     * @param listener the listener that will be removed
     */
    public void removeMetricLevelChangeListener(MetricLevelChangeListener listener) {
        metricLevelChangeListeners.remove(listener);
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
     * Get the current enabled status of Metrics
     *
     * @return {@code true} if the Metrics feature is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the enabled status of Metrics
     *
     * @param enabled The enabled flag
     */
    private void setEnabled(boolean enabled) {
        boolean changed = (this.enabled != enabled);
        this.enabled = enabled;
        if (changed) {
            enabledStatusChangeListeners.forEach(listener -> listener.stateChanged(enabled));
        }
    }

    /**
     * Notify enabled status to all metrics
     */
    private void notifyEnabledStatus() {
        metricsMap.values().forEach(metricWrapper -> {
            AbstractMetric metric = metricWrapper.metric;
            metric.setEnabled(isMetricEnabled(metricWrapper.name, metric.getLevel(),
                    metricsLevelConfig.getLevel(metric.getName()), false));
        });
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
            metricLevelChangeListeners.forEach(listener -> listener.levelChanged(metric, currentLevel, level));
        }
    }

    /**
     * Get the root level configured in the Metric Manager
     *
     * @return The Root {@link Level}
     */
    public Level getRootLevel() {
        return metricsLevelConfig.getRootLevel();
    }

    /**
     * Set a new root level to the Metric Manager
     *
     * @param level New Root {@link Level}
     */
    public void setRootLevel(Level level) {
        Level oldLevel = metricsLevelConfig.getRootLevel();
        boolean changed = !oldLevel.equals(level);
        metricsLevelConfig.setRootLevel(level);
        if (changed) {
            rootLevelChangeListeners.forEach(listener -> listener.levelChanged(oldLevel, level));
        }
    }

    /**
     * @param name         The name of the metric
     * @param metricLevel  The {@link Level} used when creating the metric
     * @param configLevel  The configured {@link Level} the metric
     * @param getFromCache A boolean flag to indicate whether the metric enabled status should be checked from the
     *                     cached value or to calculate the enabled status based on the metric name hierarchy
     * @return {@code true} if the metric should be enabled
     */
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
        if (metricWrapper != null) {
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

    /**
     * @param name The name used for a metric
     * @return {@code true} if the name is annotated with [+]
     */
    private boolean isAnnotated(String name) {
        String[] nameParts = name.split(METRIC_PATH_DELIMITER_REGEX);
        for (String namePart : nameParts) {
            Matcher matcher = METRIC_AGGREGATE_ANNOTATION_PATTERN.matcher(namePart);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the names for creating a metric collection
     *
     * @param name The annotated name
     * @return The names use to create a collection of metrics
     */
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
     * An interface for creating a new metric collection
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

    /**
     * Create a new array by inserting a level as the first element and copying the rest of the elements from another
     * level array to the index starting with 1.
     *
     * @param level  The first {@link Level} passed to create a metric
     * @param levels The {@link Level} array used to create a metric collection
     * @return An array of {@link Level}s
     */
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
    public Meter meter(String name) throws MetricNotFoundException {
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
     * @see #meter(String)
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
    public Counter counter(String name) throws MetricNotFoundException {
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
     * @see #counter(String)
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
    public Timer timer(String name) throws MetricNotFoundException {
        return getMetric(name, Timer.class);
    }

    /**
     * <p>Get or create a {@link Timer} instance registered under given name.</p>
     *
     * @param name  The name of the metric
     * @param level The {@link Level} used for metric
     * @return a {@link Timer} instance
     * @see #timer(String)
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
    public Histogram histogram(String name) throws MetricNotFoundException {
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
     * @see #histogram(String)
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
     * Removes the metric or metric collection with the given name.
     *
     * @param name the name of the metric or the annotated name for the metric collection
     * @return whether or not the metric was removed
     */
    public boolean remove(String name) {
        boolean removed;
        if (isAnnotated(name)) {
            Metric metric = metricCollectionsMap.remove(name);
            removed = metric != null;
            String[] metricNames = getMetricHierarchyNames(name);
            for (String metricName : metricNames) {
                // Child metrics might have removed earlier
                removed = removeMetric(metricName) || removed;
            }
        } else {
            removed = removeMetric(name);
        }
        return removed;
    }


    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     * @return whether or not the metric was removed
     */
    private boolean removeMetric(String name) {
        MetricWrapper metricWrapper = metricsMap.remove(name);
        if (metricWrapper != null) {
            // Remove from metric registry. This is needed to remove metrics from reporters
            return metricRegistry.remove(name);
        }
        return false;
    }

    /**
     * Return the number of metrics used
     *
     * @return The metrics count
     */
    public long getMetricsCount() {
        return metricsMap.size();
    }

    /**
     * Return the number of enabled metrics used
     *
     * @return The enabled metrics count
     */
    public long getEnabledMetricsCount() {
        return metricsMap.values().stream().filter(metricWrapper -> metricWrapper.enabled).count();
    }

    /**
     * Return the number of metric collections used
     *
     * @return The metric collections count
     */
    public long getMetricCollectionsCount() {
        return metricCollectionsMap.size();
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
        metrics.getMetrics().entrySet().stream().filter(entry -> filterJVMMetric(entry.getKey())).forEach(entry -> {
            String name = MetricRegistry.name(prefix, entry.getKey());
            com.codahale.metrics.Metric metric = entry.getValue();
            com.codahale.metrics.Gauge<?> gauge = (com.codahale.metrics.Gauge<?>) metric;
            gauge(name, level, new JVMGaugeWrapper(gauge));
        });
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
     * A {@code MetricFilter} to filter metrics based on enabled status
     */
    private class EnabledMetricFilter implements MetricFilter {
        @Override
        public boolean matches(String name, com.codahale.metrics.Metric metric) {
            MetricWrapper metricWrapper = metricsMap.get(name);
            return isMetricEnabled(metricWrapper.name, metricWrapper.level, metricsLevelConfig.getLevel(name), true);
        }
    }

}
