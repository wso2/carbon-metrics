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
package org.wso2.carbon.metrics.impl.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.common.DefaultSourceValueProvider;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.internal.LocalDatabaseCreator;
import org.wso2.carbon.metrics.impl.reporter.JDBCReporterImpl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class JDBCReporterBuilder implements ReporterBuilder<JDBCReporterImpl> {

    private static final Logger logger = LoggerFactory.getLogger(JDBCReporterBuilder.class);

    private static final String JDBC_REPORTING_ENABLED = "Reporting.JDBC.Enabled";
    private static final String JDBC_REPORTING_POLLING_PERIOD = "Reporting.JDBC.PollingPeriod";
    private static final String JDBC_REPORTING_SOURCE = "Reporting.JDBC.Source";
    private static final String JDBC_REPORTING_DATASOURCE_NAME = "Reporting.JDBC.DataSourceName";

    private static final String JDBC_REPORTING_SCHEDULED_CLEANUP_ENABLED = "Reporting.JDBC.ScheduledCleanup.Enabled";
    private static final String JDBC_REPORTING_SCHEDULED_CLEANUP_PERIOD = "Reporting.JDBC.ScheduledCleanup.ScheduledCleanupPeriod";
    private static final String JDBC_REPORTING_SCHEDULED_CLEANUP_DAYS_TO_KEEP = "Reporting.JDBC.ScheduledCleanup.DaysToKeep";

    /**
     * Select query to check whether database tables are created
     */
    private static final String DB_CHECK_SQL = "SELECT NAME FROM METRIC_GAUGE";

    private boolean enabled;

    // Default polling period for JDBC reporter is 60 seconds
    private long jdbcReporterPollingPeriod = 60;

    private String source;

    private String dataSourceName;

    private boolean runCleanupTask;

    // Default days to keep is 7 days
    private int daysToKeep = 7;

    // Default cleanup period for JDBC is 86400 seconds
    private long jdbcScheduledCleanupPeriod = 86400;

    @Override
    public ReporterBuilder<JDBCReporterImpl> configure(MetricsConfiguration configuration) {
        enabled = Boolean.parseBoolean(configuration.getFirstProperty(JDBC_REPORTING_ENABLED));

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

        source = configuration.getFirstProperty(JDBC_REPORTING_SOURCE, new DefaultSourceValueProvider());

        dataSourceName = configuration.getFirstProperty(JDBC_REPORTING_DATASOURCE_NAME);

        runCleanupTask = Boolean.parseBoolean(configuration.getFirstProperty(JDBC_REPORTING_SCHEDULED_CLEANUP_ENABLED));

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

        return this;
    }

    public ReporterBuilder<JDBCReporterImpl> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ReporterBuilder<JDBCReporterImpl> setJdbcReporterPollingPeriod(long jdbcReporterPollingPeriod) {
        this.jdbcReporterPollingPeriod = jdbcReporterPollingPeriod;
        return this;
    }

    public ReporterBuilder<JDBCReporterImpl> setSource(String source) {
        this.source = source;
        return this;
    }

    public ReporterBuilder<JDBCReporterImpl> setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        return this;
    }

    public ReporterBuilder<JDBCReporterImpl> setRunCleanupTask(boolean runCleanupTask) {
        this.runCleanupTask = runCleanupTask;
        return this;
    }

    public ReporterBuilder<JDBCReporterImpl> setDaysToKeep(int daysToKeep) {
        this.daysToKeep = daysToKeep;
        return this;
    }

    public ReporterBuilder<JDBCReporterImpl> setJdbcScheduledCleanupPeriod(long jdbcScheduledCleanupPeriod) {
        this.jdbcScheduledCleanupPeriod = jdbcScheduledCleanupPeriod;
        return this;
    }

    @Override
    public JDBCReporterImpl build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterDisabledException, ReporterBuildException {
        if (!enabled) {
            throw new ReporterDisabledException("JDBC Reporting for Metrics is not enabled");
        }
        if (dataSourceName == null || dataSourceName.trim().length() == 0) {
            throw new ReporterBuildException("Data Source Name is not specified for JDBC Reporting.");
        }

        DataSource dataSource = null;
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            throw new ReporterBuildException(
                    String.format("Error when looking up the Data Source: '%s'.", dataSourceName), e);
        }
        // Setup Database if required
        try {
            setupMetricsDatabase(dataSource);
        } catch (Exception e) {
            throw new ReporterBuildException(
                    String.format("Error when setting up metrics tables in Data Source: '%s'.", dataSourceName), e);
        }
        if (logger.isInfoEnabled()) {
            logger.info(String.format(
                    "Creating JDBC reporter for Metrics with source '%s', data source '%s' and %d seconds polling period",
                    source, dataSourceName, jdbcReporterPollingPeriod));
        }

        return new JDBCReporterImpl(metricRegistry, metricFilter, source, dataSource, jdbcReporterPollingPeriod,
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

}
