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
package org.wso2.carbon.metrics.das.core.config.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for DAS reporter.
 */
public class ReportingConfig {

    private Set<DasReporterConfig> das;

    public ReportingConfig() {
        das = new HashSet<>();
        das.add(new DasReporterConfig());
    }

    public Set<DasReporterConfig> getDas() {
        return das;
    }

    public void setDas(Set<DasReporterConfig> das) {
        this.das = das;
    }

}
