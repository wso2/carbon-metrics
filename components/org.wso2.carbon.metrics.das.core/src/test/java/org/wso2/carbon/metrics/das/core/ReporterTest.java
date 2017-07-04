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
package org.wso2.carbon.metrics.das.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.metrics.core.Gauge;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Metrics;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.metrics.das.core.config.model.DasConfig;
import org.wso2.carbon.metrics.das.core.config.model.DasReporterConfig;
import org.wso2.carbon.metrics.das.reporter.TestEventServer;

import java.io.File;

/**
 * Test Cases for DAS Reporter.
 */
public class ReporterTest {

    private static final Logger logger = LoggerFactory.getLogger(ReporterTest.class);

    private final Gauge<Integer> gauge = () -> 1;

    private static final String TEST_RESOURCES_DIR = "target" + File.separator + "test-resources";

    private Metrics metrics;

    private MetricService metricService;

    private MetricManagementService metricManagementService;

    private TestEventServer testServer = new TestEventServer(TEST_RESOURCES_DIR);

    private int serverPort;

    @Parameters("server-port")
    @BeforeClass
    private void init(String serverPort) throws ConfigurationException {
        if (logger.isInfoEnabled()) {
            logger.info("Creating the DAS Test Receiver");
        }
        this.serverPort = Integer.parseInt(serverPort);
        testServer.start("localhost", this.serverPort);

        if (logger.isInfoEnabled()) {
            logger.info("Creating MetricService");
        }
        System.setProperty("metrics.dataagent.conf", TEST_RESOURCES_DIR + File.separator + "data-agent-config.xml");
        metrics = new Metrics(TestUtils.getConfigProvider("metrics.yaml"));
        metrics.activate();
        metricService = metrics.getMetricService();
        metricManagementService = metrics.getMetricManagementService();
    }

    @AfterClass
    private void destroy() {
        if (logger.isInfoEnabled()) {
            logger.info("Stopping reporters");
        }
        metrics.deactivate();
        testServer.stop();
    }

    @Test
    public void testDasReporterValidations() {
        String name = "DAS-TEST";
        DasReporterConfig dasReporterConfig = new DasReporterConfig();
        DasConfig dasConfig = new DasConfig();
        dasReporterConfig.setDas(dasConfig);
        dasReporterConfig.setName(name);
        dasReporterConfig.setEnabled(true);
        dasConfig.setAuthURL("ssl://localhost:7711");
        dasConfig.setType(null);
        dasConfig.setReceiverURL(null);
        dasConfig.setUsername(null);
        dasConfig.setPassword(null);
        addReporter(dasReporterConfig);

        dasConfig.setType("");
        addReporter(dasReporterConfig);

        dasConfig.setType("thrift");
        addReporter(dasReporterConfig);

        dasConfig.setReceiverURL("");
        addReporter(dasReporterConfig);

        dasConfig.setReceiverURL("tcp://localhost:" + serverPort);
        addReporter(dasReporterConfig);

        dasConfig.setUsername("");
        addReporter(dasReporterConfig);

        dasConfig.setUsername("admin");
        addReporter(dasReporterConfig);

        dasConfig.setPassword("");
        addReporter(dasReporterConfig);

        dasConfig.setAuthURL(null);
        dasConfig.setPassword("admin");
        System.setProperty("metrics.dataagent.conf", "invalid.xml");
        try {
            metricManagementService.addReporter(dasReporterConfig);
            // Add again to update
            metricManagementService.addReporter(dasReporterConfig);
        } catch (ReporterBuildException e) {
            Assert.fail("Reporter should be created");
        }
        Assert.assertTrue(metricManagementService.removeReporter(name));
        Assert.assertFalse(metricManagementService.removeReporter(name));
    }


    private <T extends ReporterBuilder> void addReporter(T reporterBuilder) {
        try {
            metricManagementService.addReporter(reporterBuilder);
            Assert.fail("Add Reporter should fail.");
        } catch (ReporterBuildException e) {
            logger.info("Exception message from Add Reporter: {}", e.getMessage());
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addDisabledReporter() throws ReporterBuildException {
        String name = "DAS-DISABLED";
        DasReporterConfig dasReporterConfig = new DasReporterConfig();
        dasReporterConfig.setEnabled(false);
        dasReporterConfig.setName(name);
        metricManagementService.addReporter(dasReporterConfig);
        metricManagementService.stopReporter(name);
    }

    @Test
    public void testDasReporter() {
        Assert.assertTrue(metricManagementService.isReporterRunning("DAS"));
        String meterName = MetricService.name(this.getClass(), "test-das-meter");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricService.name(this.getClass(), "test-das-gauge");
        metricService.gauge(gaugeName, Level.INFO, gauge);

        metricManagementService.report();

        Event event = testServer.getEvent("Meter", meterName);
        Assert.assertEquals(event.getPayloadData()[2], 1L);

        event = testServer.getEvent("Gauge", gaugeName);
        Assert.assertEquals(event.getPayloadData()[2], 1.0D);
    }


}
