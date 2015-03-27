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

import org.wso2.carbon.metrics.impl.task.ScheduledJDBCMetricsCleanupTask;
import org.wso2.carbon.metrics.reporter.JDBCReporter;

public class JDBCReporterImpl extends AbstractReporter {

    private final JDBCReporter jdbcReporter;

    private final long pollingPeriod;

    // This task can be null
    private final ScheduledJDBCMetricsCleanupTask scheduledJDBCMetricsCleanupTask;

    private final long cleanupPeriod;

    public JDBCReporterImpl(JDBCReporter jdbcReporter, long pollingPeriod,
            ScheduledJDBCMetricsCleanupTask scheduledJDBCMetricsCleanupTask, long cleanupPeriod) {
        super("JDBC");
        this.jdbcReporter = jdbcReporter;
        this.pollingPeriod = pollingPeriod;
        this.scheduledJDBCMetricsCleanupTask = scheduledJDBCMetricsCleanupTask;
        this.cleanupPeriod = cleanupPeriod;
    }

    @Override
    public void report() {
        jdbcReporter.report();
    }

    @Override
    public void startReporter() {
        jdbcReporter.start(pollingPeriod, TimeUnit.SECONDS);
        if (scheduledJDBCMetricsCleanupTask != null) {
            scheduledJDBCMetricsCleanupTask.start(cleanupPeriod, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stopReporter() {
        jdbcReporter.stop();
        if (scheduledJDBCMetricsCleanupTask != null) {
            scheduledJDBCMetricsCleanupTask.stop();
        }
    }
}
