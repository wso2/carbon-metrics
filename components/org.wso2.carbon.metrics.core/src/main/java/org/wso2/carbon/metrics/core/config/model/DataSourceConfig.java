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

import org.wso2.carbon.metrics.core.internal.Utils;

/**
 * Configuration for data source
 */
public class DataSourceConfig {

    private boolean lookupDataSource = Utils.isCarbonEnvironment();

    private String dataSourceName;

    private JdbcScheduledCleanupConfig scheduledCleanup = new JdbcScheduledCleanupConfig();

    public boolean isLookupDataSource() {
        return lookupDataSource;
    }

    public void setLookupDataSource(boolean lookupDataSource) {
        this.lookupDataSource = lookupDataSource;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public JdbcScheduledCleanupConfig getScheduledCleanup() {
        return scheduledCleanup;
    }

    public void setScheduledCleanup(JdbcScheduledCleanupConfig scheduledCleanup) {
        this.scheduledCleanup = scheduledCleanup;
    }
}
