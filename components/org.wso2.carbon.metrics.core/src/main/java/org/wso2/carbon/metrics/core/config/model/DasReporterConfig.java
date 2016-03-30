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
import org.wso2.carbon.metrics.core.reporter.impl.DasReporter;
import org.wso2.carbon.metrics.core.utils.Utils;

import java.util.Optional;

/**
 * Configuration for DAS Reporter. Implements {@link ReporterBuilder} to construct a {@link DasReporter}
 */
public class DasReporterConfig extends ScheduledReporterConfig implements ReporterBuilder<DasReporter> {

    private static final Logger logger = LoggerFactory.getLogger(DasReporterConfig.class);

    private String source = Utils.getDefaultSource();

    private String type = "thrift";

    private String receiverURL = "tcp://localhost:7611";

    private String authURL;

    private String username = "admin";

    private String password = "admin";

    private String dataAgentConfigPath;

    public String getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public String getReceiverURL() {
        return receiverURL;
    }

    public String getAuthURL() {
        return authURL;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDataAgentConfigPath() {
        return dataAgentConfigPath;
    }

    /**
     * Build the DAS Reporter
     *
     * @param metricRegistry The {@link MetricRegistry} for the reporter
     * @param metricFilter   The {@link MetricFilter} for the reporter
     * @return an {@link Optional} with {@link DasReporter}, if the reporter is built successfully, otherwise an empty
     * {@code Optional}
     * @throws ReporterBuildException when there was a failure in constructing the reporter
     */
    @Override
    public Optional<DasReporter> build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterBuildException {
        if (!enabled) {
            return Optional.empty();
        }
        if (type == null || type.trim().length() == 0) {
            throw new ReporterBuildException("Type is not specified for DAS Reporting.");
        }
        if (receiverURL == null || receiverURL.trim().length() == 0) {
            throw new ReporterBuildException("Receiver URL is not specified for DAS Reporting.");
        }

        if (username == null || username.trim().length() == 0) {
            throw new ReporterBuildException("Username is not specified for DAS Reporting.");
        }

        if (password == null || password.trim().length() == 0) {
            throw new ReporterBuildException("Password is not specified for DAS Reporting.");
        }

        if (dataAgentConfigPath == null || dataAgentConfigPath.trim().length() == 0) {
            throw new ReporterBuildException("Data Agent config path is not specified for DAS Reporting.");
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format(
                    "Creating DAS reporter for Metrics with source '%s', protocol '%s' and %d seconds polling period",
                    source, type, pollingPeriod));
        }

        return Optional.of(new DasReporter(metricRegistry, metricFilter, source, type, receiverURL, authURL, username,
                password, dataAgentConfigPath, pollingPeriod));
    }
}
