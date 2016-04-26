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
import org.wso2.carbon.metrics.core.reporter.impl.Slf4jReporter;

import java.util.Optional;

/**
 * Configuration for SLF4J Reporter. Implements {@link ReporterBuilder} to construct a {@link Slf4jReporter}
 */
public class Slf4jReporterConfig extends ScheduledReporterConfig implements ReporterBuilder<Slf4jReporter> {

    private static final Logger logger = LoggerFactory.getLogger(Slf4jReporterConfig.class);

    private String loggerName = "metrics";

    private String markerName;

    public Slf4jReporterConfig() {
        name = "SLF4J";
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getMarkerName() {
        return markerName;
    }

    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }

    /**
     * Build the SLF4J Reporter
     *
     * @param metricRegistry The {@link MetricRegistry} for the reporter
     * @param metricFilter   The {@link MetricFilter} for the reporter
     * @return an {@link Optional} with {@link Slf4jReporter}, if the reporter is built successfully, otherwise an empty
     * {@code Optional}
     * @throws ReporterBuildException when there was a failure in constructing the reporter
     */
    @Override
    public Optional<Slf4jReporter> build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterBuildException {
        if (!enabled) {
            return Optional.empty();
        }
        if (loggerName == null || loggerName.trim().isEmpty()) {
            throw new ReporterBuildException("Logger Name is not specified for SLF4J Reporting.");
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format(
                    "Creating SLF4J reporter for Metrics with logger name '%s' and %d seconds polling period",
                    loggerName, pollingPeriod));
        }

        return Optional.of(new Slf4jReporter(name, metricRegistry, metricFilter, loggerName, markerName,
                pollingPeriod));
    }

}
