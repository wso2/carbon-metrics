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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.config.model.ConsoleReporterConfig;
import org.wso2.carbon.metrics.core.config.model.CsvReporterConfig;
import org.wso2.carbon.metrics.core.config.model.DasReporterConfig;
import org.wso2.carbon.metrics.core.config.model.JdbcReporterConfig;
import org.wso2.carbon.metrics.core.config.model.JmxReporterConfig;
import org.wso2.carbon.metrics.core.config.model.MetricsConfig;
import org.wso2.carbon.metrics.core.config.model.Slf4jReporterConfig;

import java.io.File;

/**
 * Test Cases for {@link MetricsConfig}
 */
public class MetricsConfigTest extends BaseReporterTest {

    private static MetricsConfig metricsConfig;

    @BeforeClass
    private void load() {
        metricsConfig = metricService.getMetricsConfig();
    }

    @Test
    public void testConfigLoad() {
        Assert.assertEquals(metricsConfig.isEnabled(), true);
    }

    @Test
    public void testJmxReporterConfigLoad() {
        JmxReporterConfig config = metricsConfig.getReporting().getJmx();
        Assert.assertEquals(config.getName(), "JMX");
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getDomain(), "org.wso2.carbon.metrics.test");
    }

    @Test
    public void testConsoleReporterConfigLoad() {
        ConsoleReporterConfig config = metricsConfig.getReporting().getConsole();
        Assert.assertEquals(config.getName(), "Console");
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getPollingPeriod(), 600L);
    }

    @Test
    public void testCsvReporterConfigLoad() {
        CsvReporterConfig config = metricsConfig.getReporting().getCsv();
        Assert.assertEquals(config.getName(), "CSV");
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getPollingPeriod(), 600L);
        Assert.assertEquals(config.getLocation(), "target/metrics");
    }

    @Test
    public void testSlf4jReporterConfigLoad() {
        Slf4jReporterConfig config = metricsConfig.getReporting().getSlf4j();
        Assert.assertEquals(config.getName(), "SLF4J");
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getPollingPeriod(), 600L);
        Assert.assertEquals(config.getLoggerName(), "metrics.test");
        Assert.assertEquals(config.getMarkerName(), "metrics");
    }

    @Test
    public void testJdbcReporterConfigLoad() {
        JdbcReporterConfig config = metricsConfig.getReporting().getJdbc();
        Assert.assertEquals(config.getName(), "JDBC");
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getPollingPeriod(), 600L);
        Assert.assertEquals(config.getSource(), "Carbon-jdbc");
        Assert.assertEquals(config.isLookupDataSource(), true);
        Assert.assertEquals(config.getDataSourceName(), "jdbc/WSO2MetricsDB");
        Assert.assertEquals(config.getScheduledCleanup().isEnabled(), true);
        Assert.assertEquals(config.getScheduledCleanup().getDaysToKeep(), 2);
        Assert.assertEquals(config.getScheduledCleanup().getScheduledCleanupPeriod(), 10000L);
    }

    @Test
    public void testDasReporterConfigLoad() {
        DasReporterConfig config = metricsConfig.getReporting().getDas();
        Assert.assertEquals(config.getName(), "DAS");
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getPollingPeriod(), 600L);
        Assert.assertEquals(config.getSource(), "Carbon-das");
        Assert.assertEquals(config.getReceiverURL(), "tcp://localhost:51840");
        Assert.assertNull(config.getAuthURL());
        Assert.assertEquals(config.getType(), "thrift");
        Assert.assertEquals(config.getUsername(), "admin");
        Assert.assertEquals(config.getPassword(), "admin");
        // This path is updated in runtime as we have specified the system property "metrics.dataagent.conf"
        Assert.assertEquals(config.getDataAgentConfigPath(), TEST_RESOURCES_DIR + File.separator
                + "data-agent-config.xml");
    }

    @Test
    public void testReporterCount() {
        Assert.assertEquals(metricsConfig.getReporting().getReporterBuilders().size(), 6);
    }

}
