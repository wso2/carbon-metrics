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
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.metrics.core.config.model.JmxConfig;
import org.wso2.carbon.metrics.core.config.model.MetricsConfig;
import org.wso2.carbon.metrics.core.config.model.MetricsLevelConfig;
import org.wso2.carbon.metrics.core.config.model.ReservoirConfig;
import org.wso2.carbon.metrics.core.impl.MetricManagementServiceImpl;
import org.wso2.carbon.metrics.core.impl.MetricManager;
import org.wso2.carbon.metrics.core.impl.MetricServiceImpl;
import org.wso2.carbon.metrics.core.impl.MetricsMXBeanImpl;
import org.wso2.carbon.metrics.core.jmx.MetricsMXBean;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.spi.MetricsExtension;
import org.wso2.carbon.metrics.core.utils.Utils;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Initialize the implementations of {@link MetricService} and {@link MetricManagementService}.
 */
public class Metrics {

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    private final boolean enabled;

    private final boolean registerMBean;

    private final String mBeanName;

    private final MetricService metricService;

    private final MetricManagementService metricManagementService;

    private final List<MetricsExtension> metricsExtensions;

    private final ConfigProvider configProvider;

    /**
     * Create a {@link Metrics} instance with a {@link MetricService} and a {@link MetricManagementService}.
     *
     * @param configProvider Configuration Provider Service
     */
    public Metrics(ConfigProvider configProvider) {
        metricsExtensions = new CopyOnWriteArrayList<>();
        this.configProvider = configProvider;

        MetricRegistry metricRegistry = new MetricRegistry();

        MetricsConfig metricsConfig;
        try {
            metricsConfig = configProvider.getConfigurationObject(MetricsConfig.class);
        } catch (ConfigurationException e) {
            logger.error("Error loading Metrics Configuration", e);
            metricsConfig = new MetricsConfig();
        }
        MetricsLevelConfig metricsLevelConfig = metricsConfig.getLevels();
        ReservoirConfig reservoirConfig = metricsConfig.getReservoir();

        MetricManager metricManager = new MetricManager(metricRegistry, metricsLevelConfig, reservoirConfig);

        metricService = new MetricServiceImpl(metricManager);
        metricManagementService = new MetricManagementServiceImpl(metricManager);

        // Build all reporters
        metricsConfig.getReporting().getReporterBuilders().forEach(reporterBuilder -> {
            try {
                metricManagementService.addReporter(reporterBuilder);
            } catch (ReporterBuildException e) {
                logger.warn("Reporter build failed", e);
            }
        });

        JmxConfig jmxConfig = metricsConfig.getJmx();
        this.enabled = metricsConfig.isEnabled();
        this.registerMBean = jmxConfig.isRegisterMBean();
        this.mBeanName = jmxConfig.getName();

        if (!Utils.isCarbonEnvironment()) {
            ServiceLoader.load(MetricsExtension.class).forEach(this::addMetricsExtension);
        }
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
        // Enable Metrics. The change listeners should start the reporters
        if (enabled) {
            metricManagementService.enable();
        }
        if (registerMBean) {
            MetricsMXBean metricsMXBean = new MetricsMXBeanImpl(metricManagementService);
            registerMXBean(metricsMXBean);
        }
        if (!Utils.isCarbonEnvironment()) {
            metricsExtensions.forEach(extension -> extension.activate(configProvider, metricService,
                    metricManagementService));
        }
    }

    /**
     * Unregister the MXBean for the MetricService and disable metrics.
     */
    public void deactivate() {
        if (registerMBean) {
            unregisterMXBean();
        }
        metricManagementService.disable();
        if (!Utils.isCarbonEnvironment()) {
            metricsExtensions.forEach(extension -> extension.deactivate(metricService, metricManagementService));
        }
    }

    /**
     * Adds a {@link MetricsExtension} to a collection of extensions that will be notified if the Metrics is activated
     * or deactivated.
     *
     * @param extension the metrics extension
     */
    private void addMetricsExtension(MetricsExtension extension) {
        metricsExtensions.add(extension);
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

}
