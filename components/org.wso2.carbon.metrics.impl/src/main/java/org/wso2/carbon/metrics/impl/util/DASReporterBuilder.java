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
import org.wso2.carbon.metrics.common.DefaultSourceValueProvider;
import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.impl.reporter.DASReporterImpl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class DASReporterBuilder implements ReporterBuilder<DASReporterImpl> {

    private static final Logger logger = LoggerFactory.getLogger(DASReporterBuilder.class);

    private static final String DAS_REPORTING_ENABLED = "Reporting.DAS.Enabled";
    private static final String DAS_REPORTING_SOURCE = "Reporting.DAS.Source";
    private static final String DAS_REPORTING_TYPE = "Reporting.DAS.Type";
    private static final String DAS_REPORTING_RECEIVER_URL = "Reporting.DAS.ReceiverURL";
    private static final String DAS_REPORTING_AUTH_URL = "Reporting.DAS.AuthURL";
    private static final String DAS_REPORTING_USERNAME = "Reporting.DAS.Username";
    private static final String DAS_REPORTING_PASSWORD = "Reporting.DAS.Password";
    private static final String DAS_REPORTING_POLLING_PERIOD = "Reporting.DAS.PollingPeriod";

    private boolean enabled = true;

    // Default polling period for DAS reporter is 60 seconds
    private long dasReporterPollingPeriod = 60;

    private String source;

    private String type;

    private String receiverURL;

    private String authURL;

    private String username;

    private String password;

    @Override
    public ReporterBuilder<DASReporterImpl> configure(MetricsConfiguration configuration) {
        enabled = Boolean.parseBoolean(configuration.getFirstProperty(DAS_REPORTING_ENABLED));

        String pollingPeriod = configuration.getFirstProperty(DAS_REPORTING_POLLING_PERIOD,
                String.valueOf(dasReporterPollingPeriod));
        try {
            dasReporterPollingPeriod = Long.parseLong(pollingPeriod);
        } catch (NumberFormatException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("Error parsing the polling period for DAS Reporting. Using %d seconds",
                        dasReporterPollingPeriod));
            }
        }

        source = configuration.getFirstProperty(DAS_REPORTING_SOURCE, new DefaultSourceValueProvider());

        type = configuration.getFirstProperty(DAS_REPORTING_TYPE);

        receiverURL = configuration.getFirstProperty(DAS_REPORTING_RECEIVER_URL);

        authURL = configuration.getFirstProperty(DAS_REPORTING_AUTH_URL);

        username = configuration.getFirstProperty(DAS_REPORTING_USERNAME);

        password = configuration.getFirstProperty(DAS_REPORTING_PASSWORD);

        return this;
    }

    public ReporterBuilder<DASReporterImpl> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ReporterBuilder<DASReporterImpl> setSource(String source) {
        this.source = source;
        return this;
    }

    public ReporterBuilder<DASReporterImpl> setDasReporterPollingPeriod(long dasReporterPollingPeriod) {
        this.dasReporterPollingPeriod = dasReporterPollingPeriod;
        return this;
    }

    public ReporterBuilder<DASReporterImpl> setType(String type) {
        this.type = type;
        return this;
    }

    public ReporterBuilder<DASReporterImpl> setReceiverURL(String receiverURL) {
        this.receiverURL = receiverURL;
        return this;
    }

    public ReporterBuilder<DASReporterImpl> setAuthURL(String authURL) {
        this.authURL = authURL;
        return this;
    }

    public ReporterBuilder<DASReporterImpl> setUsername(String username) {
        this.username = username;
        return this;
    }

    public ReporterBuilder<DASReporterImpl> setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public DASReporterImpl build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterDisabledException, ReporterBuildException {
        if (!enabled) {
            throw new ReporterDisabledException("DAS Reporting for Metrics is not enabled");
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

        if (logger.isInfoEnabled()) {
            logger.info(String.format(
                    "Creating DAS reporter for Metrics with source '%s', data source '%s' and %d seconds polling period",
                    source, type, dasReporterPollingPeriod));
        }

        return new DASReporterImpl(metricRegistry, metricFilter, source, type, receiverURL, authURL, username, password,
                dasReporterPollingPeriod);
    }
}
