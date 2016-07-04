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

import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for all reporters in Metrics Core
 */
public class ReportingConfig {

    private Set<JmxReporterConfig> jmx;

    private Set<ConsoleReporterConfig> console;

    private Set<CsvReporterConfig> csv;

    private Set<Slf4jReporterConfig> slf4j;

    public Set<JmxReporterConfig> getJmx() {
        return jmx;
    }

    public void setJmx(Set<JmxReporterConfig> jmx) {
        this.jmx = jmx;
    }

    public Set<ConsoleReporterConfig> getConsole() {
        return console;
    }

    public void setConsole(Set<ConsoleReporterConfig> console) {
        this.console = console;
    }

    public Set<CsvReporterConfig> getCsv() {
        return csv;
    }

    public void setCsv(Set<CsvReporterConfig> csv) {
        this.csv = csv;
    }

    public Set<Slf4jReporterConfig> getSlf4j() {
        return slf4j;
    }

    public void setSlf4j(Set<Slf4jReporterConfig> slf4j) {
        this.slf4j = slf4j;
    }

    public Set<? extends ReporterBuilder> getReporterBuilders() {
        Set<ReporterBuilder> reporterBuilders = new HashSet<>();
        if (jmx != null) {
            reporterBuilders.addAll(jmx);
        }
        if (console != null) {
            reporterBuilders.addAll(console);
        }
        if (csv != null) {
            reporterBuilders.addAll(csv);
        }
        if (slf4j != null) {
            reporterBuilders.addAll(slf4j);
        }
        return reporterBuilders;
    }
}
