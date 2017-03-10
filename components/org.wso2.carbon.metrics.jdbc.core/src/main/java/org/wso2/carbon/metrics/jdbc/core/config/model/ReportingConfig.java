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

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for JDBC reporter
 */
public class ReportingConfig {

    private Set<JdbcReporterConfig> jdbc;

    public ReportingConfig() {
        jdbc = new HashSet<>();
        jdbc.add(new JdbcReporterConfig());
    }

    public Set<JdbcReporterConfig> getJdbc() {
        return jdbc;
    }

    public void setJdbc(Set<JdbcReporterConfig> jdbc) {
        this.jdbc = jdbc;
    }

}
