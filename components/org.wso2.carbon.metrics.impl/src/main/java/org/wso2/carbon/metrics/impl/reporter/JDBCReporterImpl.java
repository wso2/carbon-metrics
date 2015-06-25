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
package org.wso2.carbon.metrics.impl.reporter;

import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.impl.task.ScheduledJDBCMetricsCleanupTask;
import org.wso2.carbon.metrics.reporter.JDBCReporter;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class JDBCReporterImpl extends AbstractReporter implements ScheduledReporter {

    private static final Logger logger = LoggerFactory.getLogger(JDBCReporterImpl.class);

    private final MetricRegistry metricRegistry;

    private final MetricFilter metricFilter;

    private final String source;

    private final DataSource dataSource;

    private final long pollingPeriod;

    private final boolean runCleanupTask;

    private final int daysToKeep;

    private final long cleanupPeriod;

    private JDBCReporter jdbcReporter;

    // This task can be null
    private ScheduledJDBCMetricsCleanupTask scheduledJDBCMetricsCleanupTask;

    public JDBCReporterImpl(MetricRegistry metricRegistry, MetricFilter metricFilter, String source,
            DataSource dataSource, long pollingPeriod, boolean runCleanupTask, int daysToKeep, long cleanupPeriod) {
        super("JDBC");
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.source = source;
        this.dataSource = dataSource;
        this.pollingPeriod = pollingPeriod;
        this.runCleanupTask = runCleanupTask;
        this.daysToKeep = daysToKeep;
        this.cleanupPeriod = cleanupPeriod;
    }

    @Override
    public void report() {
        if (jdbcReporter != null) {
            jdbcReporter.report();
        }
    }

    @Override
    public void startReporter() {
        jdbcReporter = JDBCReporter.forRegistry(metricRegistry).filter(metricFilter).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS).convertTimestampTo(TimeUnit.MILLISECONDS)
                .build(source, dataSource);
        jdbcReporter.start(pollingPeriod, TimeUnit.SECONDS);
        if (runCleanupTask) {
            scheduledJDBCMetricsCleanupTask = new ScheduledJDBCMetricsCleanupTask(dataSource, daysToKeep);
            scheduledJDBCMetricsCleanupTask.start(cleanupPeriod, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stopReporter() {
        try {
            jdbcReporter.stop();
            jdbcReporter = null;
        } catch (Throwable e) {
            logger.error("An error occurred when trying to stop the reporter", e);
        }
        try {
            if (scheduledJDBCMetricsCleanupTask != null) {
                scheduledJDBCMetricsCleanupTask.stop();
            }
        } catch (Throwable e) {
            logger.error("An error occurred when trying to stop the cleanup task", e);
        }
    }
}
