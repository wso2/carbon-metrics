/*
 * Copyright 2016 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.jdbc.core.config.model;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Configuration for JDBC Scheduled Cleanup.
 */
@Configuration(description = "Schedule regular deletion of metrics data older than a set number of days.\n" +
        "It is recommended that you enable this job to ensure your metrics tables do not get extremely large.\n" +
        "Deleting data older than seven days should be sufficient.")
public class JdbcScheduledCleanupConfig {

    @Element(description = "Enable scheduled cleanup to delete Metrics data in the database.")
    private boolean enabled = true;

    @Element(description = "This is the period for each cleanup operation in seconds.")
    // Default cleanup period for JDBC is 86400 seconds
    private long scheduledCleanupPeriod = 86400;

    @Element(description = "The scheduled job will cleanup all data older than the specified days")
    // Default days to keep is 7 days
    private int daysToKeep = 7;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getScheduledCleanupPeriod() {
        return scheduledCleanupPeriod;
    }

    public void setScheduledCleanupPeriod(long scheduledCleanupPeriod) {
        this.scheduledCleanupPeriod = scheduledCleanupPeriod;
    }

    public int getDaysToKeep() {
        return daysToKeep;
    }

    public void setDaysToKeep(int daysToKeep) {
        this.daysToKeep = daysToKeep;
    }
}
