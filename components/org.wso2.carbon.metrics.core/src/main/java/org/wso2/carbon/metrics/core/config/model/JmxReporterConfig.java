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
import org.wso2.carbon.metrics.core.reporter.impl.JmxReporter;

import java.util.Optional;

/**
 * Configuration for JMX Reporter. Implements {@link ReporterBuilder} to construct a {@link JmxReporter}
 */
public class JmxReporterConfig extends ReporterConfig implements ReporterBuilder<JmxReporter> {

    private static final Logger logger = LoggerFactory.getLogger(JmxReporterConfig.class);

    /**
     * JMX domain registered with MBean Server
     */
    private String domain = "org.wso2.carbon.metrics";

    public JmxReporterConfig() {
        // Enable JMX by default
        enabled = true;
        name = "JMX";
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Build the JMX Reporter
     *
     * @param metricRegistry The {@link MetricRegistry} for the reporter
     * @param metricFilter   The {@link MetricFilter} for the reporter
     * @return an {@link Optional} with {@link JmxReporter}, if the reporter is built successfully, otherwise an empty
     * {@code Optional}
     * @throws ReporterBuildException when there was a failure in constructing the reporter
     */
    @Override
    public Optional<JmxReporter> build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterBuildException {
        if (!enabled) {
            return Optional.empty();
        }
        if (domain == null || domain.trim().isEmpty()) {
            throw new ReporterBuildException("Domain is not specified for JMX Reporting.");
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format("Creating JMX reporter for Metrics with domain '%s'", domain));
        }

        return Optional.of(new JmxReporter(name, metricRegistry, metricFilter, domain));
    }
}
