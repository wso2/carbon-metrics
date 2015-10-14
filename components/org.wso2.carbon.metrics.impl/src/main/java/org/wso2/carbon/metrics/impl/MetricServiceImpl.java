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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.common.DefaultSourceValueProvider;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.internal.LocalDatabaseCreator;
import org.wso2.carbon.metrics.impl.metric.ClassLoadingGaugeSet;
import org.wso2.carbon.metrics.impl.metric.OperatingSystemMetricSet;
import org.wso2.carbon.metrics.impl.reporter.CsvReporterImpl;
import org.wso2.carbon.metrics.impl.reporter.DASReporterImpl;
import org.wso2.carbon.metrics.impl.reporter.JDBCReporterImpl;
import org.wso2.carbon.metrics.impl.reporter.JmxReporterImpl;
import org.wso2.carbon.metrics.impl.reporter.ListeningReporter;
import org.wso2.carbon.metrics.impl.reporter.Reporter;
import org.wso2.carbon.metrics.impl.reporter.ScheduledReporter;
import org.wso2.carbon.metrics.manager.Counter;
import org.wso2.carbon.metrics.manager.Gauge;
import org.wso2.carbon.metrics.manager.Histogram;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.Timer;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

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

    private static final String ENABLED = "Enabled";

    /**
     * Name of the root metric. This is set to empty string.
     */
    private static final String ROOT_METRIC_NAME = "";

    /**
     * Hierarchy delimiter in Metric name
     */
    private static final String HIERARCHY_DELIMITER = ".";

    private static final String JMX_REPORTING_ENABLED = "Reporting.JMX.Enabled";
    private static final String CSV_REPORTING_ENABLED = "Reporting.CSV.Enabled";
    private static final String CSV_REPORTING_LOCATION = "Reporting.CSV.Location";
    private static final String CSV_REPORTING_POLLING_PERIOD = "Reporting.CSV.PollingPeriod";
    private static final String JDBC_REPORTING_ENABLED = "Reporting.JDBC.Enabled";
    private static final String JDBC_REPORTING_POLLING_PERIOD = "Reporting.JDBC.PollingPeriod";
    private static final String JDBC_REPORTING_SOURCE = "Reporting.JDBC.Source";
    private static final String JDBC_REPORTING_DATASOURCE_NAME = "Reporting.JDBC.DataSourceName";

    private static final String JDBC_REPORTING_SCHEDULED_CLEANUP_ENABLED = "Reporting.JDBC.ScheduledCleanup.Enabled";
    private static final String JDBC_REPORTING_SCHEDULED_CLEANUP_PERIOD = "Reporting.JDBC.ScheduledCleanup.ScheduledCleanupPeriod";
    private static final String JDBC_REPORTING_SCHEDULED_CLEANUP_DAYS_TO_KEEP = "Reporting.JDBC.ScheduledCleanup.DaysToKeep";

    private static final String DAS_REPORTING_ENABLED = "Reporting.DAS.Enabled";
    private static final String DAS_REPORTING_SOURCE = "Reporting.DAS.Source";
    private static final String DAS_REPORTING_TYPE = "Reporting.DAS.Type";
    private static final String DAS_REPORTING_RECEIVER_URL = "Reporting.DAS.ReceiverURL";
    private static final String DAS_REPORTING_AUTH_URL = "Reporting.DAS.AuthURL";
    private static final String DAS_REPORTING_USERNAME = "Reporting.DAS.Username";
    private static final String DAS_REPORTING_PASSWORD = "Reporting.DAS.Password";
    private static final String DAS_REPORTING_POLLING_PERIOD = "Reporting.DAS.PollingPeriod";

    private final MetricsConfiguration configuration;
    private final MetricsLevelConfiguration levelConfiguration;

    /**
     * JMX domain registered with MBean Server
     */
    private static final String JMX_REPORTING_DOMAIN = "org.wso2.carbon.metrics";

    /**
     * Select query to check whether database tables are created
     */
    private static final String DB_CHECK_SQL = "SELECT NAME FROM METRIC_GAUGE";

    private final List<Reporter> reporters = new ArrayList<Reporter>();

    private final MetricFilter enabledMetricFilter = new EnabledMetricFilter();

    /**
     * MetricWrapper class is used for the metrics map. This class keeps the associated {@link Level} and enabled status
     * for a metric. The main reason to keep the enabled status separately is that EnabledMetricFilter gets called as
     * soon as a Metric is added to MetricRegistry. The JMXReporter registers MBeans via a listener added to
     * MetricRegistry. The wrapper is not available when the listener gets called and by keeping enabled status
     * separately, we can check whether the metric should be filtered without having the metric wrapper
     */
    private class MetricWrapper {

        private final Level level;

        private Boolean enabled;

        private AbstractMetric metric;

        private MetricWrapper(Level level, Boolean enabled) {
            this.level = level;
            this.enabled = enabled;
        }
    }

    /**
     * Initialize the MetricRegistry, Level and Reporters
     */
    public MetricServiceImpl(final MetricsConfiguration configuration,
            final MetricsLevelConfiguration levelConfiguration) {
        this.configuration = configuration;
        this.levelConfiguration = levelConfiguration;

        // Highest priority is given for the System Properties
        String metricsEnabled = System.getProperty(SYSTEM_PROPERTY_METRICS_ENABLED);
        if (metricsEnabled == null || metricsEnabled.trim().isEmpty()) {
            metricsEnabled = configuration.getFirstProperty(ENABLED);
        }

        String rootLevel = System.getProperty(SYSTEM_PROPERTY_METRICS_ROOT_LEVEL);
        Level level = null;
        if (rootLevel != null && !rootLevel.trim().isEmpty()) {
            level = Level.getLevel(rootLevel);
        }

        if (level != null) {
            levelConfiguration.setRootLevel(level);
        }

        boolean enabled = Boolean.valueOf(metricsEnabled);

        // Initialize Metric Registry
        metricRegistry = new MetricRegistry();

        // Configure all reporters
        configureReporters();

        // Set enabled
        setEnabled(enabled);

        // Register JVM Metrics
        // This should be the last method when initializing MetricService
        registerJVMMetrics();
    }

    private void configureReporters() {
        Reporter jmxReporter = null;
        try {
            jmxReporter = configureJMXReporter();
        } catch (Throwable e) {
            logger.error("Error when configuring JMX reporter", e);
        }

        if (jmxReporter != null) {
            reporters.add(jmxReporter);
        }

        Reporter csvReporter = null;
        try {
            csvReporter = configureCSVReporter();
        } catch (Throwable e) {
            logger.error("Error when configuring CSV reporter", e);
        }

        if (csvReporter != null) {
            reporters.add(csvReporter);
        }

        Reporter jdbcReporter = null;
        try {
            jdbcReporter = configureJDBCReporter();
        } catch (Throwable e) {
            logger.error("Error when configuring JDBC reporter", e);
        }

        if (jdbcReporter != null) {
            reporters.add(jdbcReporter);
        }

        Reporter dasReporter = null;
        try {
            dasReporter = configureDASReporter();
        } catch (Throwable e) {
            logger.error("Error when configuring DAS reporter", e);
        }

        if (dasReporter != null) {
            reporters.add(dasReporter);
        }
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
            metric.setEnabled(isMetricEnabled(metric.getName(), metric.getLevel(),
                    levelConfiguration.getLevel(metric.getName()), false));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.MetricService#getMetricLevel(java.lang.String)
     */
    @Override
    public Level getMetricLevel(String name) {
        if (!metricsMap.containsKey(name)) {
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
    public void setMetricLevel(String name, Level level) {
        MetricWrapper metricWrapper = metricsMap.get(name);
        if (metricWrapper == null) {
            throw new IllegalArgumentException("Invalid Metric Name");
        }
        Level currentLevel = levelConfiguration.getLevel(name);
        if (currentLevel == null || !currentLevel.equals(level)) {
            // Set new level only if there is no existing level or the new level is different from exisiting level
            levelConfiguration.setLevel(name, level);
            AbstractMetric metric = metricWrapper.metric;
            metric.setEnabled(isMetricEnabled(metric.getName(), metric.getLevel(), level, false));
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
    private <T extends AbstractMetric> T getOrCreateMetric(Level level, String name, MetricBuilder<T> metricBuilder) {
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
            metricWrapper = new MetricWrapper(level, enabled);
            metricsMap.put(name, metricWrapper);
            T newMetric = metricBuilder.createMetric(level, name);
            metricWrapper.metric = newMetric;
            newMetric.setEnabled(enabled);
            return newMetric;
        }
    }

    /**
     * An interface for creating a new metric
     */
    private interface MetricBuilder<T extends AbstractMetric> {
        T createMetric(Level level, String name);

        boolean isInstance(AbstractMetric metric);
    }

    /**
     * Default Metric Builder for {@code MeterImpl}
     */
    private final MetricBuilder<MeterImpl> METER_BUILDER = new MetricBuilder<MeterImpl>() {
        @Override
        public MeterImpl createMetric(Level level, String name) {
            return new MeterImpl(level, name, metricRegistry.meter(name));
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
        public CounterImpl createMetric(Level level, String name) {
            return new CounterImpl(level, name, metricRegistry.counter(name));
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
        public TimerImpl createMetric(Level level, String name) {
            return new TimerImpl(level, name, metricRegistry.timer(name));
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
        public HistogramImpl createMetric(Level level, String name) {
            return new HistogramImpl(level, name, metricRegistry.histogram(name));
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
        public GaugeImpl<T> createMetric(Level level, String name) {
            GaugeImpl<T> gaugeImpl = new GaugeImpl<T>(level, name, gauge);
            metricRegistry.register(name, gaugeImpl);
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
        public CachedGaugeImpl<T> createMetric(Level level, String name) {
            CachedGaugeImpl<T> gaugeImpl = new CachedGaugeImpl<T>(level, name, timeout, timeoutUnit, gauge);
            metricRegistry.register(name, gaugeImpl);
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
    public Meter meter(Level level, String name) {
        return getOrCreateMetric(level, name, METER_BUILDER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.MetricService#counter(org.wso2.carbon.metrics.manager.Level,
     * java.lang.String)
     */
    @Override
    public Counter counter(Level level, String name) {
        return getOrCreateMetric(level, name, COUNTER_BUILDER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.MetricService#timer(org.wso2.carbon.metrics.manager.Level, java.lang.String)
     */
    @Override
    public Timer timer(Level level, String name) {
        return getOrCreateMetric(level, name, TIMER_BUILDER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.MetricService#histogram(org.wso2.carbon.metrics.manager.Level,
     * java.lang.String)
     */
    @Override
    public Histogram histogram(Level level, String name) {
        return getOrCreateMetric(level, name, HISTOGRAM_BUILDER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.MetricService#gauge(org.wso2.carbon.metrics.manager.Level, java.lang.String,
     * org.wso2.carbon.metrics.manager.Gauge)
     */
    @Override
    public <T> void gauge(Level level, String name, Gauge<T> gauge) {
        getOrCreateMetric(level, name, new GaugeBuilder<T>(gauge));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.MetricService#cachedGauge(org.wso2.carbon.metrics.manager.Level,
     * java.lang.String, long, java.util.concurrent.TimeUnit, org.wso2.carbon.metrics.manager.Gauge)
     */
    @Override
    public <T> void cachedGauge(Level level, String name, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        getOrCreateMetric(level, name, new CachedGaugeBuilder<T>(gauge, timeout, timeoutUnit));
    }

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
                    gauge(level, name, new JVMGaugeWrapper(gauge));
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

    private class JVMGaugeWrapper implements Gauge<Object> {

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
            reporter.start();
        }
    }

    private void stopReporters() {
        for (Reporter reporter : reporters) {
            reporter.stop();
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
                return isMetricEnabled(name, metricWrapper.level, levelConfiguration.getLevel(name), true);
            }
            return false;
        }
    }

    private Reporter configureJMXReporter() {
        if (!Boolean.parseBoolean(configuration.getFirstProperty(JMX_REPORTING_ENABLED))) {
            if (logger.isTraceEnabled()) {
                logger.trace("JMX Reporting for Metrics is not enabled");
            }
            return null;
        }
        return new JmxReporterImpl(metricRegistry, enabledMetricFilter, JMX_REPORTING_DOMAIN);
    }

    private Reporter configureCSVReporter() {
        if (!Boolean.parseBoolean(configuration.getFirstProperty(CSV_REPORTING_ENABLED))) {
            if (logger.isTraceEnabled()) {
                logger.trace("CSV Reporting for Metrics is not enabled");
            }
            return null;
        }
        String location = configuration.getFirstProperty(CSV_REPORTING_LOCATION);
        if (location == null || location.trim().isEmpty()) {
            if (logger.isWarnEnabled()) {
                logger.warn("CSV Reporting location is not specified");
            }
            return null;
        }
        File file = new File(location);
        if (!file.exists()) {
            if (!file.mkdir()) {
                if (logger.isWarnEnabled()) {
                    logger.warn("CSV Reporting location was not created!. Location: " + location);
                }
                return null;
            }
        }
        if (!file.isDirectory()) {
            if (logger.isWarnEnabled()) {
                logger.warn("CSV Reporting location is not a directory");
            }
            return null;
        }
        String pollingPeriod = configuration.getFirstProperty(CSV_REPORTING_POLLING_PERIOD);
        // Default polling period for CSV reporter is 60 seconds
        long csvReporterPollingPeriod = 60;
        try {
            csvReporterPollingPeriod = Long.parseLong(pollingPeriod);
        } catch (NumberFormatException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("Error parsing the polling period for CSV Reporting. Using %d seconds",
                        csvReporterPollingPeriod));
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info(
                    String.format("Creating CSV reporter for Metrics with location '%s' and %d seconds polling period",
                            location, csvReporterPollingPeriod));
        }

        return new CsvReporterImpl(metricRegistry, enabledMetricFilter, file, csvReporterPollingPeriod);
    }

    private Reporter configureJDBCReporter() {
        if (!Boolean.parseBoolean(configuration.getFirstProperty(JDBC_REPORTING_ENABLED))) {
            if (logger.isTraceEnabled()) {
                logger.trace("JDBC Reporting for Metrics is not enabled");
            }
            return null;
        }
        // Default polling period for JDBC reporter is 30 seconds
        long jdbcReporterPollingPeriod = 30;
        String pollingPeriod = configuration.getFirstProperty(JDBC_REPORTING_POLLING_PERIOD,
                String.valueOf(jdbcReporterPollingPeriod));
        try {
            jdbcReporterPollingPeriod = Long.parseLong(pollingPeriod);
        } catch (NumberFormatException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("Error parsing the polling period for JDBC Reporting. Using %d seconds",
                        jdbcReporterPollingPeriod));
            }
        }

        String source = configuration.getFirstProperty(JDBC_REPORTING_SOURCE, new DefaultSourceValueProvider());

        String dataSourceName = configuration.getFirstProperty(JDBC_REPORTING_DATASOURCE_NAME);

        if (dataSourceName == null || dataSourceName.trim().length() == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "Data Source Name is not specified for JDBC Reporting. The JDBC reporting will not be enabled");
            }
            return null;
        }

        DataSource dataSource = null;
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format(
                        "Error when looking up the Data Source: '%s'. The JDBC reporting will not be enabled",
                        dataSourceName));
            }
            return null;
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format(
                    "Creating JDBC reporter for Metrics with source '%s', data source '%s' and %d seconds polling period",
                    source, dataSourceName, jdbcReporterPollingPeriod));
        }

        // Setup Database if required
        try {
            setupMetricsDatabase(dataSource);
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format(
                        "Error when looking up the Data Source: '%s'. The JDBC reporting will not be enabled",
                        dataSourceName));
            }
            return null;
        }

        boolean runCleanupTask = Boolean
                .parseBoolean(configuration.getFirstProperty(JDBC_REPORTING_SCHEDULED_CLEANUP_ENABLED));
        // Default cleanup period for JDBC is 86400 seconds
        long jdbcScheduledCleanupPeriod = 86400;
        // Default days to keep is 7 days
        int daysToKeep = 7;
        if (runCleanupTask) {
            String cleanupPeriod = configuration.getFirstProperty(JDBC_REPORTING_SCHEDULED_CLEANUP_PERIOD);
            try {
                jdbcScheduledCleanupPeriod = Long.parseLong(cleanupPeriod);
            } catch (NumberFormatException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("Error parsing the period for JDBC Sceduled Cleanup. Using %d seconds",
                            jdbcReporterPollingPeriod));
                }
            }

            String daysToKeepValue = configuration.getFirstProperty(JDBC_REPORTING_SCHEDULED_CLEANUP_DAYS_TO_KEEP);

            try {
                daysToKeep = Integer.parseInt(daysToKeepValue);
            } catch (NumberFormatException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("Error parsing the period for JDBC Sceduled Cleanup. Using %d seconds",
                            jdbcReporterPollingPeriod));
                }
            }
        }

        return new JDBCReporterImpl(metricRegistry, enabledMetricFilter, source, dataSource, jdbcReporterPollingPeriod,
                runCleanupTask, daysToKeep, jdbcScheduledCleanupPeriod);
    }

    /**
     * Create Metrics Database Tables
     *
     * @throws Exception if an error occurred while creating the Metrics Tables.
     */
    private static void setupMetricsDatabase(DataSource dataSource) throws Exception {
        String value = System.getProperty("setup");
        if (value != null) {
            LocalDatabaseCreator databaseCreator = new LocalDatabaseCreator(dataSource);
            if (!databaseCreator.isDatabaseStructureCreated(DB_CHECK_SQL)) {
                databaseCreator.createRegistryDatabase();
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Metrics tables exist. Skipping the Metrics Database setup process.");
                }
            }
        }
    }

    private Reporter configureDASReporter() {
        if (!Boolean.parseBoolean(configuration.getFirstProperty(DAS_REPORTING_ENABLED))) {
            if (logger.isTraceEnabled()) {
                logger.trace("DAS Reporting for Metrics is not enabled");
            }
            return null;
        }
        // Default polling period for DAS reporter is 30 seconds
        long dasReporterPollingPeriod = 30;
        String pollingPeriod = configuration.getFirstProperty(DAS_REPORTING_POLLING_PERIOD,
                String.valueOf(dasReporterPollingPeriod));
        try {
            dasReporterPollingPeriod = Long.parseLong(pollingPeriod);
        } catch (NumberFormatException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("Error parsing the polling period for DAS Reporting. Using %d seconds",
                        dasReporterPollingPeriod));
            }
        }

        String source = configuration.getFirstProperty(DAS_REPORTING_SOURCE, new DefaultSourceValueProvider());

        String type = configuration.getFirstProperty(DAS_REPORTING_TYPE);

        if (type == null || type.trim().length() == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn("Type is not specified for DAS Reporting. The DAS reporting will not be enabled");
            }
            return null;
        }

        String receiverURL = configuration.getFirstProperty(DAS_REPORTING_RECEIVER_URL);

        if (receiverURL == null || receiverURL.trim().length() == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn("Receiver URL is not specified for DAS Reporting. The DAS reporting will not be enabled");
            }
            return null;
        }

        String authURL = configuration.getFirstProperty(DAS_REPORTING_AUTH_URL);

        String username = configuration.getFirstProperty(DAS_REPORTING_USERNAME);

        if (username == null || username.trim().length() == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn("Username is not specified for DAS Reporting. The DAS reporting will not be enabled");
            }
            return null;
        }

        String password = configuration.getFirstProperty(DAS_REPORTING_PASSWORD);

        if (password == null || password.trim().length() == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn("Password is not specified for DAS Reporting. The DAS reporting will not be enabled");
            }
            return null;
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format(
                    "Creating DAS reporter for Metrics with source '%s', data source '%s' and %d seconds polling period",
                    source, type, dasReporterPollingPeriod));
        }

        return new DASReporterImpl(metricRegistry, enabledMetricFilter, source, type, receiverURL, authURL, username,
                password, dasReporterPollingPeriod);
    }

}
