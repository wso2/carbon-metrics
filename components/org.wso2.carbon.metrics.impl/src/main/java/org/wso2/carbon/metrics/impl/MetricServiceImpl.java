/*
 * Copyright 2015 WSO2 Inc. (http://wso2.org)
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.reporter.CsvReporterImpl;
import org.wso2.carbon.metrics.impl.reporter.JDBCReporterImpl;
import org.wso2.carbon.metrics.impl.reporter.JmxReporterImpl;
import org.wso2.carbon.metrics.impl.reporter.Reporter;
import org.wso2.carbon.metrics.impl.task.ScheduledJDBCMetricsCleanupTask;
import org.wso2.carbon.metrics.manager.Counter;
import org.wso2.carbon.metrics.manager.Gauge;
import org.wso2.carbon.metrics.manager.Histogram;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.metrics.reporter.JDBCReporter;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

/**
 * Implementation class for {@link MetricService}, which will use the Metrics (https://dropwizard.github.io/metrics)
 * library. This is registered as an OSGi service
 */
public class MetricServiceImpl extends Observable implements MetricService {

    private static final Logger logger = LoggerFactory.getLogger(MetricServiceImpl.class);

    /**
     * The level configured for Metrics collection
     */
    private Level level;

    /**
     * The previous level to identify changes in level configuration
     */
    private Level previousLevel;

    /**
     * The {@link MetricRegistry} instance from the Metrics Implementation
     */
    private final MetricRegistry metricRegistry;

    private static final String SYSTEM_PROPERTY_METRICS_LEVEL = "metrics.level";
    private static final String LEVEL = "Level";

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

    private final MetricsConfiguration configuration;

    /**
     * JMX domain registered with MBean Server
     */
    private static final String JMX_REPORTING_DOMAIN = "org.wso2.carbon.metrics";

    private final List<Reporter> reporters = new ArrayList<Reporter>();

    /**
     * Initialize the MetricRegistry, Level and Reporters
     */
    public MetricServiceImpl(MetricsConfiguration configuration) {
        this.configuration = configuration;
        // Highest priority is for the System Property
        String configLevel = System.getProperty(SYSTEM_PROPERTY_METRICS_LEVEL);
        if (configLevel == null || configLevel.trim().isEmpty()) {
            configLevel = configuration.getFirstProperty(LEVEL);
        }

        Level level = Level.toLevel(configLevel, Level.OFF);

        metricRegistry = new MetricRegistry();
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

        // Initial level
        this.level = Level.OFF;
        setLevel(level);
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.previousLevel = this.level;
        this.level = level;
        boolean changed = !this.level.equals(this.previousLevel);
        if (changed) {
            setChanged();
            notifyObservers(level);
            if (this.level.compareTo(Level.OFF) > 0) {
                startReporters();
            } else if (this.level.equals(Level.OFF)) {
                stopReporters();
            }
        }
    }

    public int getObserverCount() {
        // This count may not be always equal to the actual number of metrics used in the system. The Metrics
        // implementation keeps a map based on the metric name. Hence if a user retrieves the same metric type with
        // existing name, this service will wrap an existing metric instance and add as an observer.
        return countObservers();
    }

    public int getMetricsCount() {
        return metricRegistry.getMetrics().size();
    }

    public Meter createMeter(Level level, String name) {
        MeterImpl meter = new MeterImpl(level, metricRegistry.meter(name));
        meter.setEnabled(getLevel());
        addObserver(meter);
        return meter;
    }

    public Counter createCounter(Level level, String name) {
        CounterImpl counter = new CounterImpl(level, metricRegistry.counter(name));
        counter.setEnabled(getLevel());
        addObserver(counter);
        return counter;
    }

    public Timer createTimer(Level level, String name) {
        TimerImpl timer = new TimerImpl(level, metricRegistry.timer(name));
        timer.setEnabled(getLevel());
        addObserver(timer);
        return timer;
    }

    public Histogram createHistogram(Level level, String name) {
        HistogramImpl histogram = new HistogramImpl(level, metricRegistry.histogram(name));
        histogram.setEnabled(getLevel());
        addObserver(histogram);
        return histogram;
    }

    public <T> void createGauge(Level level, String name, Gauge<T> gauge) {
        GaugeImpl<T> gaugeImpl = new GaugeImpl<T>(level, gauge);
        gaugeImpl.setEnabled(getLevel());
        addObserver(gaugeImpl);
        metricRegistry.register(name, gaugeImpl);
    }

    public <T> void createCachedGauge(Level level, String name, long timeout, TimeUnit timeoutUnit, Gauge<T> gauge) {
        CachedGaugeImpl<T> gaugeImpl = new CachedGaugeImpl<T>(level, timeout, timeoutUnit, gauge);
        gaugeImpl.setEnabled(getLevel());
        addObserver(gaugeImpl);
        metricRegistry.register(name, gaugeImpl);
    }

    private void startReporters() {
        for (Reporter reporter : reporters) {
            if (!reporter.isRunning()) {
                reporter.start();
            }
        }
    }

    private void stopReporters() {
        for (Reporter reporter : reporters) {
            if (reporter.isRunning()) {
                reporter.stop();
            }
        }
    }

    private Reporter configureJMXReporter() {
        if (!Boolean.parseBoolean(configuration.getFirstProperty(JMX_REPORTING_ENABLED))) {
            if (logger.isTraceEnabled()) {
                logger.trace("JMX Reporting for Metrics is not enabled");
            }
            return null;
        }
        final JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain(JMX_REPORTING_DOMAIN)
                .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
        return new JmxReporterImpl(jmxReporter);
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
            logger.info(String.format(
                    "Creating CSV reporter for Metrics with location '%s' and %d seconds polling period", location,
                    csvReporterPollingPeriod));
        }

        final CsvReporter csvReporter = CsvReporter.forRegistry(metricRegistry).formatFor(Locale.US)
                .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build(file);
        return new CsvReporterImpl(csvReporter, csvReporterPollingPeriod);
    }

    private Reporter configureJDBCReporter() {
        if (!Boolean.parseBoolean(configuration.getFirstProperty(JDBC_REPORTING_ENABLED))) {
            if (logger.isTraceEnabled()) {
                logger.trace("JDBC Reporting for Metrics is not enabled");
            }
            return null;
        }
        String pollingPeriod = configuration.getFirstProperty(JDBC_REPORTING_POLLING_PERIOD);
        // Default polling period for JDBC reporter is 60 seconds
        long jdbcReporterPollingPeriod = 60;
        try {
            jdbcReporterPollingPeriod = Long.parseLong(pollingPeriod);
        } catch (NumberFormatException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("Error parsing the polling period for JDBC Reporting. Using %d seconds",
                        jdbcReporterPollingPeriod));
            }
        }

        String source = configuration.getFirstProperty(JDBC_REPORTING_SOURCE);

        if (source == null || source.trim().length() == 0) {
            // Use host name if available
            String hostname = null;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                // Ignore exception
            }
            if (hostname == null || hostname.trim().length() == 0) {
                source = "Carbon";
            } else {
                source = hostname;
            }
        }

        String dataSourceName = configuration.getFirstProperty(JDBC_REPORTING_DATASOURCE_NAME);

        if (dataSourceName == null || dataSourceName.trim().length() == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn("Data Source Name is not specified for JDBC Reporting. The JDBC reporting will not be enabled");
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
            logger.info(String
                    .format("Creating JDBC reporter for Metrics with source '%s', data source '%s' and %d seconds polling period",
                            source, dataSourceName, jdbcReporterPollingPeriod));
        }

        ScheduledJDBCMetricsCleanupTask scheduledJDBCMetricsCleanupTask = null;
        // Default cleanup period for JDBC is 86400 seconds
        long jdbcScheduledCleanupPeriod = 86400;
        if (Boolean.parseBoolean(configuration.getFirstProperty(JDBC_REPORTING_SCHEDULED_CLEANUP_ENABLED))) {
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
            // Default days to keep is 7 days
            int daysToKeep = 7;
            try {
                daysToKeep = Integer.parseInt(daysToKeepValue);
            } catch (NumberFormatException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("Error parsing the period for JDBC Sceduled Cleanup. Using %d seconds",
                            jdbcReporterPollingPeriod));
                }
            }

            scheduledJDBCMetricsCleanupTask = new ScheduledJDBCMetricsCleanupTask(dataSource, daysToKeep);
        }

        final JDBCReporter jdbcReporter = JDBCReporter.forRegistry(metricRegistry).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS).build(source, dataSource);
        return new JDBCReporterImpl(jdbcReporter, jdbcReporterPollingPeriod, scheduledJDBCMetricsCleanupTask,
                jdbcScheduledCleanupPeriod);
    }

}
