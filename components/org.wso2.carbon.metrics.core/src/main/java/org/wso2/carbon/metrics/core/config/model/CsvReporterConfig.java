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

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.metrics.core.reporter.impl.CsvReporter;

import java.io.File;
import java.util.Optional;

/**
 * Configuration for CSV Reporter. Implements {@link ReporterBuilder} to construct a {@link CsvReporter}
 */
public class CsvReporterConfig extends ScheduledReporterConfig implements ReporterBuilder<CsvReporter> {

    private static final Logger logger = LoggerFactory.getLogger(CsvReporterConfig.class);

    private String location;

    public CsvReporterConfig() {
        super("CSV");
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Build the CSV Reporter
     *
     * @param metricRegistry The {@link MetricRegistry} for the reporter
     * @param metricFilter   The {@link MetricFilter} for the reporter
     * @return an {@link Optional} with {@link CsvReporter}, if the reporter is built successfully, otherwise an empty
     * {@code Optional}
     * @throws ReporterBuildException when there was a failure in constructing the reporter
     */
    @Override
    public Optional<CsvReporter> build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterBuildException {
        if (!isEnabled()) {
            return Optional.empty();
        }
        if (location == null || location.trim().isEmpty()) {
            throw new ReporterBuildException("CSV Reporting location is not specified");
        }

        File csvLocation = new File(location);

        if (!csvLocation.exists()) {
            if (!csvLocation.mkdir()) {
                throw new ReporterBuildException("Could not create the CSV Reporting Location: " + location);
            }
        }
        if (!csvLocation.isDirectory()) {
            throw new ReporterBuildException("CSV Reporting location is not a directory");
        }

        if (logger.isInfoEnabled()) {
            logger.info(
                    String.format("Creating CSV reporter for Metrics with location '%s' and %d seconds polling period",
                            location, getPollingPeriod()));
        }

        return Optional.of(new CsvReporter(getName(), metricRegistry, getFilter(metricFilter), csvLocation,
                getPollingPeriod()));
    }
}
