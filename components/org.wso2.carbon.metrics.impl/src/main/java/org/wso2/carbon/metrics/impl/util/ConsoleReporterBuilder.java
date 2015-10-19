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
package org.wso2.carbon.metrics.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.reporter.ConsoleReporterImpl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class ConsoleReporterBuilder implements ReporterBuilder<ConsoleReporterImpl> {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleReporterBuilder.class);

    private static final String CONSOLE_REPORTING_ENABLED = "Reporting.Console.Enabled";
    private static final String CONSOLE_REPORTING_POLLING_PERIOD = "Reporting.Console.PollingPeriod";

    private boolean enabled;

    // Default polling period for console reporter is 60 seconds
    private long consoleReporterPollingPeriod = 60;

    @Override
    public ReporterBuilder<ConsoleReporterImpl> configure(MetricsConfiguration configuration) {
        enabled = Boolean.parseBoolean(configuration.getFirstProperty(CONSOLE_REPORTING_ENABLED));

        String pollingPeriod = configuration.getFirstProperty(CONSOLE_REPORTING_POLLING_PERIOD,
                String.valueOf(consoleReporterPollingPeriod));
        try {
            consoleReporterPollingPeriod = Long.parseLong(pollingPeriod);
        } catch (NumberFormatException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("Error parsing the polling period for Console Reporting. Using %d seconds",
                        consoleReporterPollingPeriod));
            }
        }

        return this;
    }

    public ReporterBuilder<ConsoleReporterImpl> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ReporterBuilder<ConsoleReporterImpl> setCsvReporterPollingPeriod(long consoleReporterPollingPeriod) {
        this.consoleReporterPollingPeriod = consoleReporterPollingPeriod;
        return this;
    }

    @Override
    public ConsoleReporterImpl build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReportedDisabledException, ReporterBuildException {
        if (!enabled) {
            throw new ReportedDisabledException("Console Reporting for Metrics is not enabled");
        }
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Creating Console reporter for Metrics with %d seconds polling period",
                    consoleReporterPollingPeriod));
        }

        return new ConsoleReporterImpl(metricRegistry, metricFilter, consoleReporterPollingPeriod);
    }
}
