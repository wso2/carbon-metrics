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
package org.wso2.carbon.metrics.core.config.model;

/**
 * Configuration for JDBC Scheduled Cleanup
 */
public class JdbcScheduledCleanupConfig {

    private boolean enabled = true;

    // Default cleanup period for JDBC is 86400 seconds
    private long scheduledCleanupPeriod = 86400;

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
