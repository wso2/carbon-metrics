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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.reporter.CsvReporterImpl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class CsvReporterBuilder implements ReporterBuilder<CsvReporterImpl> {

    private static final Logger logger = LoggerFactory.getLogger(CsvReporterBuilder.class);

    private static final String CSV_REPORTING_ENABLED = "Reporting.CSV.Enabled";
    private static final String CSV_REPORTING_LOCATION = "Reporting.CSV.Location";
    private static final String CSV_REPORTING_POLLING_PERIOD = "Reporting.CSV.PollingPeriod";

    private boolean enabled;

    // Default polling period for CSV reporter is 60 seconds
    private long csvReporterPollingPeriod = 60;

    private File location;

    @Override
    public ReporterBuilder<CsvReporterImpl> configure(MetricsConfiguration configuration) {
        enabled = Boolean.parseBoolean(configuration.getFirstProperty(CSV_REPORTING_ENABLED));

        String pollingPeriod = configuration.getFirstProperty(CSV_REPORTING_POLLING_PERIOD,
                String.valueOf(csvReporterPollingPeriod));
        try {
            csvReporterPollingPeriod = Long.parseLong(pollingPeriod);
        } catch (NumberFormatException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("Error parsing the polling period for CSV Reporting. Using %d seconds",
                        csvReporterPollingPeriod));
            }
        }

        String location = configuration.getFirstProperty(CSV_REPORTING_LOCATION);
        if (location != null && !location.trim().isEmpty()) {
            this.location = new File(location);
        }

        return this;
    }

    public ReporterBuilder<CsvReporterImpl> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ReporterBuilder<CsvReporterImpl> setCsvReporterPollingPeriod(long csvReporterPollingPeriod) {
        this.csvReporterPollingPeriod = csvReporterPollingPeriod;
        return this;
    }

    public ReporterBuilder<CsvReporterImpl> setLocation(File location) {
        this.location = location;
        return this;
    }

    @Override
    public CsvReporterImpl build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterDisabledException, ReporterBuildException {
        if (!enabled) {
            throw new ReporterDisabledException("CSV Reporting for Metrics is not enabled");
        }
        if (location == null) {
            throw new ReporterBuildException("CSV Reporting location is not specified");
        }

        if (!location.exists()) {
            if (!location.mkdir()) {
                throw new ReporterBuildException("CSV Reporting location was not created!. Location: " + location);
            }
        }
        if (!location.isDirectory()) {
            throw new ReporterBuildException("CSV Reporting location is not a directory");
        }

        if (logger.isInfoEnabled()) {
            logger.info(
                    String.format("Creating CSV reporter for Metrics with location '%s' and %d seconds polling period",
                            location, csvReporterPollingPeriod));
        }

        return new CsvReporterImpl(metricRegistry, metricFilter, location, csvReporterPollingPeriod);
    }
}
