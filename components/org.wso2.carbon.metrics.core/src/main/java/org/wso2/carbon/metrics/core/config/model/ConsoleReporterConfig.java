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
import org.wso2.carbon.metrics.core.reporter.impl.ConsoleReporter;

import java.util.Optional;

/**
 * Configuration for Console Reporter. Implements {@link ReporterBuilder} to construct a {@link ConsoleReporter}
 */
public class ConsoleReporterConfig extends ScheduledReporterConfig implements ReporterBuilder<ConsoleReporter> {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleReporterConfig.class);

    public ConsoleReporterConfig() {
        name = "Console";
    }

    /**
     * Build the Console Reporter
     *
     * @param metricRegistry The {@link MetricRegistry} for the reporter
     * @param metricFilter   The {@link MetricFilter} for the reporter
     * @return an {@link Optional} with {@link ConsoleReporter}, if the reporter is built successfully, otherwise an
     * empty {@code Optional}
     * @throws ReporterBuildException when there was a failure in constructing the reporter
     */
    @Override
    public Optional<ConsoleReporter> build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterBuildException {
        if (!enabled) {
            return Optional.empty();
        }
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Creating Console Reporter for Metrics with %d seconds polling period",
                    pollingPeriod));
        }

        return Optional.of(new ConsoleReporter(name, metricRegistry, metricFilter, pollingPeriod));
    }
}
