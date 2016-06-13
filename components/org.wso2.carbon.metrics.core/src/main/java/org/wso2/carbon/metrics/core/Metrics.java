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
package org.wso2.carbon.metrics.core;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.config.MetricsConfigBuilder;
import org.wso2.carbon.metrics.core.config.MetricsLevelConfigBuilder;
import org.wso2.carbon.metrics.core.config.model.JmxConfig;
import org.wso2.carbon.metrics.core.config.model.MetricsConfig;
import org.wso2.carbon.metrics.core.config.model.MetricsLevelConfig;
import org.wso2.carbon.metrics.core.impl.MetricManagementServiceImpl;
import org.wso2.carbon.metrics.core.impl.MetricManager;
import org.wso2.carbon.metrics.core.impl.MetricServiceImpl;
import org.wso2.carbon.metrics.core.impl.MetricsMXBeanImpl;
import org.wso2.carbon.metrics.core.jmx.MetricsMXBean;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;

import java.lang.management.ManagementFactory;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Initialize the implementations of {@link MetricService} and {@link MetricManagementService}
 */
public class Metrics {

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    private final boolean registerMBean;

    private final String mBeanName;

    private final MetricService metricService;

    private final MetricManagementService metricManagementService;

    private Metrics(boolean registerMBean, String mBeanName, MetricService metricService,
                    MetricManagementService metricManagementService) {
        this.registerMBean = registerMBean;
        this.mBeanName = mBeanName;
        this.metricService = metricService;
        this.metricManagementService = metricManagementService;
    }

    private void registerMXBean(MetricsMXBean metricsMXBean) {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = new ObjectName(mBeanName);
            if (mBeanServer.isRegistered(name)) {
                mBeanServer.unregisterMBean(name);
            }
            mBeanServer.registerMBean(metricsMXBean, name);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("MetricManagerMXBean registered under name: %s", mBeanName));
            }
        } catch (JMException e) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("MetricManagerMXBean registration failed. Name: %s", mBeanName), e);
            }
        }
    }

    private void unregisterMXBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = new ObjectName(mBeanName);
            if (mBeanServer.isRegistered(name)) {
                mBeanServer.unregisterMBean(name);
            }
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("MetricManagerMXBean with name '%s' was unregistered.", mBeanName));
            }
        } catch (JMException e) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("MetricManagerMXBean with name '%s' was failed to unregister", mBeanName),
                        e);
            }
        }
    }

    /**
     * Register the MXBean for the MetricService. The metrics is by default enabled via the configuration. However,
     * metrics can also be enabled later from the Metrics MBean.
     */
    public void activate() {
        if (registerMBean) {
            MetricsMXBean metricsMXBean = new MetricsMXBeanImpl(metricManagementService);
            registerMXBean(metricsMXBean);
        }
    }

    /**
     * Unregister the MXBean for the MetricService and disable metrics
     */
    public void deactivate() {
        if (registerMBean) {
            unregisterMXBean();
        }
        metricManagementService.disable();
    }

    /**
     * Access the main {@link MetricService} implementation.
     *
     * @return The {@link MetricService} in use
     */
    public MetricService getMetricService() {
        return metricService;
    }

    /**
     * Access the main {@link MetricManagementService} implementation.
     *
     * @return The {@link MetricManagementService} in use
     */
    public MetricManagementService getMetricManagementService() {
        return metricManagementService;
    }

    /**
     * A builder for {@link Metrics} instances.
     */
    public static class Builder {

        /**
         * Builds a {@link Metrics} instance with a {@link MetricService} and a {@link MetricManagementService}
         *
         * @return A {@link Metrics} instance
         */
        public Metrics build() {
            MetricRegistry metricRegistry = new MetricRegistry();
            MetricsConfig metricsConfig = MetricsConfigBuilder.build();
            MetricsLevelConfig metricsLevelConfig = MetricsLevelConfigBuilder.build();

            MetricManager metricManager = new MetricManager(metricRegistry, metricsLevelConfig);

            MetricService metricService = new MetricServiceImpl(metricManager);
            MetricManagementService metricManagementService = new MetricManagementServiceImpl(metricManager);

            // Build all reporters
            metricsConfig.getReporting().getReporterBuilders().forEach(reporterBuilder -> {
                try {
                    metricManagementService.addReporter(reporterBuilder);
                } catch (ReporterBuildException e) {
                    logger.warn("Reporter build failed", e);
                }
            });

            // Enable Metric Manager after adding reporters. The change listeners should start the reporters
            if (metricsConfig.isEnabled()) {
                metricManager.enable();
            }

            JmxConfig jmxConfig = metricsConfig.getJmx();
            return new Metrics(jmxConfig.isRegisterMBean(), jmxConfig.getName(), metricService,
                    metricManagementService);
        }
    }


}
