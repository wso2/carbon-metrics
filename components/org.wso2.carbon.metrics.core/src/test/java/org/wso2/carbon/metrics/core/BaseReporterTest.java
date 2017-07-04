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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.config.ConfigurationException;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;

/**
 * Base Class for all Reporter Based Test Cases.
 */
public abstract class BaseReporterTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseReporterTest.class);

    protected static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    protected static Metrics metrics;

    protected static MetricService metricService;

    protected static MetricManagementService metricManagementService;

    @BeforeSuite
    protected static void init() throws ConfigurationException {
        if (logger.isInfoEnabled()) {
            logger.info("Creating Metrics");
        }
        System.setProperty("metrics.target", "target");
        metrics = new Metrics(TestUtils.getConfigProvider("metrics-reporter.yaml"));
        metrics.activate();
        metricService = metrics.getMetricService();
        metricManagementService = metrics.getMetricManagementService();
    }

    @AfterSuite
    protected static void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Deactivating Metrics");
        }
        metrics.deactivate();
    }
}
