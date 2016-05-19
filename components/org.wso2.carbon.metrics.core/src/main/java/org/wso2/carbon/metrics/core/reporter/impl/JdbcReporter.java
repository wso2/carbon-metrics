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
package org.wso2.carbon.metrics.core.reporter.impl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.wso2.carbon.metrics.core.reporter.ScheduledReporter;

import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

/**
 * A scheduled reporter for inserting Metrics data to database tables via JDBC.
 */
public class JdbcReporter extends AbstractReporter implements ScheduledReporter {

    private final MetricRegistry metricRegistry;

    private final MetricFilter metricFilter;

    private final String source;

    private final DataSource dataSource;

    private final long pollingPeriod;

    private final boolean runCleanupTask;

    private final int daysToKeep;

    private final long cleanupPeriod;

    private org.wso2.carbon.metrics.jdbc.reporter.JdbcReporter jdbcReporter;

    // This task can be null
    private ScheduledJdbcMetricsCleanupTask scheduledJdbcMetricsCleanupTask;

    public JdbcReporter(String name, MetricRegistry metricRegistry, MetricFilter metricFilter, String source,
                        DataSource dataSource, long pollingPeriod, boolean runCleanupTask, int daysToKeep,
                        long cleanupPeriod) {
        super(name);
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
        jdbcReporter = org.wso2.carbon.metrics.jdbc.reporter.JdbcReporter.forRegistry(metricRegistry)
                .filter(metricFilter).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertTimestampTo(TimeUnit.MILLISECONDS).build(source, dataSource);
        jdbcReporter.start(pollingPeriod, TimeUnit.SECONDS);
        if (runCleanupTask) {
            scheduledJdbcMetricsCleanupTask = new ScheduledJdbcMetricsCleanupTask(dataSource, daysToKeep);
            scheduledJdbcMetricsCleanupTask.start(cleanupPeriod, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stopReporter() {
        if (jdbcReporter != null) {
            jdbcReporter.stop();
            jdbcReporter = null;
        }
        if (scheduledJdbcMetricsCleanupTask != null) {
            scheduledJdbcMetricsCleanupTask.stop();
        }
    }
}
