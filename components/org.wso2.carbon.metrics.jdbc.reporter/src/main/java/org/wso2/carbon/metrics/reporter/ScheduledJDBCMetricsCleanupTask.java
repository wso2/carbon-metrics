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
package org.wso2.carbon.metrics.reporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This task will cleanup all metrics older than the specified number of days
 */
public class ScheduledJDBCMetricsCleanupTask extends ScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledJDBCMetricsCleanupTask.class);

    private final DataSource dataSource;

    private final int daysToKeep;

    public ScheduledJDBCMetricsCleanupTask(DataSource dataSource, int daysToKeep) {
        super("metrics-jdbc-cleanup");
        this.dataSource = dataSource;
        this.daysToKeep = daysToKeep;
    }

    @Override
    public void run() {
        cleanMetricsTables("METRIC_GAUGE", "METRIC_COUNTER", "METRIC_METER", "METRIC_HISTOGRAM", "METRIC_TIMER");
    }

    private void cleanMetricsTables(String... tableNames) {
        for (String tableName : tableNames) {
            cleanMetricsTable(tableName);
        }
    }

    private void cleanMetricsTable(String tableName) {
        Connection connection = null;
        PreparedStatement ps = null;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM ");
        queryBuilder.append(tableName);
        queryBuilder.append(" WHERE TIMESTAMP < ?");
        String query = queryBuilder.toString();

        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            // Timestamp is in seconds. There are 86400 seconds for a day (24 hours)

            long timestamp = (System.currentTimeMillis() / 1000) - (daysToKeep * 86400);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Executing SQL Query [%s]. Parameter: %s", query, timestamp));
            }

            ps.setLong(1, timestamp);

            ps.execute();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Executed SQL Query [%s]. Update Count: %s", query, ps.getUpdateCount()));
            }

            ps.close();
            connection.close();
            ps = null;
            connection = null;
        } catch (SQLException e) {
            LOGGER.error("Error when deleting metrics in " + tableName, e);
        } finally {
            closeQuietly(connection, ps);
        }
    }

    private void closeQuietly(Connection connection, PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

}
