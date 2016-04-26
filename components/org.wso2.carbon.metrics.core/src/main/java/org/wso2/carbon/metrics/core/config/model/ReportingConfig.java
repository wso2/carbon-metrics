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

import java.util.Arrays;
import java.util.List;

/**
 * Configuration for all reporters
 */
public class ReportingConfig {

    private JmxReporterConfig jmx = new JmxReporterConfig();

    private ConsoleReporterConfig console = new ConsoleReporterConfig();

    private CsvReporterConfig csv = new CsvReporterConfig();

    private Slf4jReporterConfig slf4j = new Slf4jReporterConfig();

    private JdbcReporterConfig jdbc = new JdbcReporterConfig();

    private DasReporterConfig das = new DasReporterConfig();

    public JmxReporterConfig getJmx() {
        return jmx;
    }

    public void setJmx(JmxReporterConfig jmx) {
        this.jmx = jmx;
    }

    public ConsoleReporterConfig getConsole() {
        return console;
    }

    public void setConsole(ConsoleReporterConfig console) {
        this.console = console;
    }

    public CsvReporterConfig getCsv() {
        return csv;
    }

    public void setCsv(CsvReporterConfig csv) {
        this.csv = csv;
    }

    public Slf4jReporterConfig getSlf4j() {
        return slf4j;
    }

    public void setSlf4j(Slf4jReporterConfig slf4j) {
        this.slf4j = slf4j;
    }

    public JdbcReporterConfig getJdbc() {
        return jdbc;
    }

    public void setJdbc(JdbcReporterConfig jdbc) {
        this.jdbc = jdbc;
    }

    public DasReporterConfig getDas() {
        return das;
    }

    public void setDas(DasReporterConfig das) {
        this.das = das;
    }

    public List<? extends ReporterBuilder> getReporterBuilders() {
        return Arrays.asList(jmx, console, csv, slf4j, jdbc, das);
    }
}
