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

import org.wso2.carbon.config.annotation.Element;

/**
 * Configuration for scheduled reporters
 */
public abstract class ScheduledReporterConfig extends ReporterConfig {

    // Default polling period is 60 seconds
    @Element(description = "Polling Period in seconds.\n" +
            "This is the period for polling metrics from the metric registry and reporting")
    private long pollingPeriod = 60;

    public ScheduledReporterConfig(String name) {
        super(name);
    }

    public long getPollingPeriod() {
        return pollingPeriod;
    }

    public void setPollingPeriod(long pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }
}
