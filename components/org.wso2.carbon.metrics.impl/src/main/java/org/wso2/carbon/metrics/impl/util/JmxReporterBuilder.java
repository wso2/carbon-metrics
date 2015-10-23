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
import org.wso2.carbon.metrics.impl.reporter.JmxReporterImpl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class JmxReporterBuilder implements ReporterBuilder<JmxReporterImpl> {

    private static final Logger logger = LoggerFactory.getLogger(JmxReporterBuilder.class);

    /**
     * JMX domain registered with MBean Server
     */
    private static final String JMX_REPORTING_DOMAIN = "org.wso2.carbon.metrics";

    private static final String JMX_REPORTING_ENABLED = "Reporting.JMX.Enabled";

    private boolean enabled = true;

    @Override
    public ReporterBuilder<JmxReporterImpl> configure(MetricsConfiguration configuration) {
        enabled = Boolean.parseBoolean(configuration.getFirstProperty(JMX_REPORTING_ENABLED));
        return this;
    }

    public ReporterBuilder<JmxReporterImpl> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public JmxReporterImpl build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterDisabledException, ReporterBuildException {
        if (!enabled) {
            throw new ReporterDisabledException("JMX Reporting for Metrics is not enabled");
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format("Creating JMX reporter for Metrics with domain '%s'", JMX_REPORTING_DOMAIN));
        }

        return new JmxReporterImpl(metricRegistry, metricFilter, JMX_REPORTING_DOMAIN);
    }
}
