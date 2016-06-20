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
import org.wso2.carbon.metrics.core.config.MetricsConfigBuilder;
import org.wso2.carbon.metrics.core.config.model.ConsoleReporterConfig;
import org.wso2.carbon.metrics.core.config.model.CsvReporterConfig;
import org.wso2.carbon.metrics.core.config.model.DasConfig;
import org.wso2.carbon.metrics.core.config.model.DasReporterConfig;
import org.wso2.carbon.metrics.core.config.model.DataSourceConfig;
import org.wso2.carbon.metrics.core.config.model.JdbcReporterConfig;
import org.wso2.carbon.metrics.core.config.model.JmxReporterConfig;
import org.wso2.carbon.metrics.core.config.model.MetricsConfig;
import org.wso2.carbon.metrics.core.config.model.Slf4jReporterConfig;

import java.io.File;

import static org.wso2.carbon.metrics.core.BaseReporterTest.RESOURCES_DIR;

/**
 * Test Cases for {@link MetricsConfig}
 */
public class MetricsConfigTest extends BaseMetricTest {

    private static MetricsConfig metricsConfig;

    @BeforeClass
    private void load() {
        System.setProperty("metrics.conf", RESOURCES_DIR + File.separator + "conf" + File.separator
                + "metrics-reporter.yml");
        metricsConfig = MetricsConfigBuilder.build();
    }

    @Test
    public void testConfigLoad() {
        Assert.assertEquals(metricsConfig.isEnabled(), true);
    }

    @Test
    public void testJmxConfig() {
        Assert.assertEquals(metricsConfig.getJmx().getName(), "org.wso2.carbon:type=MetricsTest");
        Assert.assertTrue(metricsConfig.getJmx().isRegisterMBean());
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
    }

    @Test
    public void testDataSourceConfigLoad() {
        DataSourceConfig config = metricsConfig.getDataSource().get(0);
        Assert.assertEquals(config.isLookupDataSource(), true);
        Assert.assertEquals(config.getDataSourceName(), "jdbc/WSO2MetricsDB");
        Assert.assertEquals(config.getScheduledCleanup().isEnabled(), true);
        Assert.assertEquals(config.getScheduledCleanup().getDaysToKeep(), 2);
        Assert.assertEquals(config.getScheduledCleanup().getScheduledCleanupPeriod(), 10000L);

        JdbcReporterConfig jdbcReporterConfig = metricsConfig.getReporting().getJdbc();
        Assert.assertEquals(jdbcReporterConfig.getDataSource(), config);
    }

    @Test
    public void testDasReporterConfigLoad() {
        DasReporterConfig config = metricsConfig.getReporting().getDas();
        Assert.assertEquals(config.getName(), "DAS");
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getPollingPeriod(), 600L);
        Assert.assertEquals(config.getSource(), "Carbon-das");
    }

    @Test
    public void testDasConfigLoad() {
        DasConfig config = metricsConfig.getDas().get(0);
        Assert.assertEquals(config.getReceiverURL(), "tcp://localhost:51840");
        Assert.assertNull(config.getAuthURL());
        Assert.assertEquals(config.getType(), "thrift");
        Assert.assertEquals(config.getUsername(), "admin");
        Assert.assertEquals(config.getPassword(), "admin");
        Assert.assertEquals(config.getDataAgentConfigPath(), "data-agent-config.xml");

        DasReporterConfig dasReporterConfig = metricsConfig.getReporting().getDas();
        Assert.assertEquals(dasReporterConfig.getDas(), config);
    }

    @Test
    public void testReporterCount() {
        Assert.assertEquals(metricsConfig.getReporting().getReporterBuilders().size(), 6);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testInvalidFile() {
        System.setProperty("metrics.conf", RESOURCES_DIR + File.separator + "conf" + File.separator
                + "metrics.properties");
        MetricsConfigBuilder.build();
    }

}
