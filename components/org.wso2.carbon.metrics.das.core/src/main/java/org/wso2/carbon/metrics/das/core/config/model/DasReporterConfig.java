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
package org.wso2.carbon.metrics.das.core.config.model;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.config.model.ScheduledReporterConfig;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.metrics.core.utils.Utils;
import org.wso2.carbon.metrics.das.core.reporter.impl.DasReporter;

import java.io.File;
import java.util.Optional;

/**
 * Configuration for DAS Reporter. Implements {@link ReporterBuilder} to construct a {@link DasReporter}
 */
public class DasReporterConfig extends ScheduledReporterConfig implements ReporterBuilder<DasReporter> {

    private static final Logger logger = LoggerFactory.getLogger(DasReporterConfig.class);

    private String source = Utils.getDefaultSource();

    private DasConfig das;

    public DasReporterConfig() {
        super("DAS");
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public DasConfig getDas() {
        return das;
    }

    public void setDas(DasConfig das) {
        this.das = das;
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
        if (!isEnabled()) {
            return Optional.empty();
        }

        String type = das.getType();
        String receiverURL = das.getReceiverURL();
        String authURL = das.getAuthURL();
        String username = das.getUsername();
        String password = das.getPassword();
        String dataAgentConfigPath = das.getDataAgentConfigPath();

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

        Optional<File> dataAgentConfigFile = Utils.getConfigFile("metrics.dataagent.conf", "data-agent-config.xml");

        if (dataAgentConfigFile.isPresent()) {
            dataAgentConfigPath = dataAgentConfigFile.get().getPath();
        } else if (logger.isDebugEnabled()) {
            logger.debug("Data Agent config was not found for DAS Reporting.");
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format(
                    "Creating DAS reporter for Metrics with source '%s', protocol '%s' and %d seconds polling period",
                    source, type, getPollingPeriod()));
        }

        return Optional.of(new DasReporter(getName(), metricRegistry, getFilter(metricFilter), source, type,
                receiverURL, authURL, username, password, dataAgentConfigPath, getPollingPeriod()));
    }
}
