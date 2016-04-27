/*
 * Copyright 2014 WSO2 Inc. (http://wso2.org)
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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.metrics.core.config.MetricsConfigBuilder;
import org.wso2.carbon.metrics.core.config.MetricsLevelConfigBuilder;
import org.wso2.carbon.metrics.core.config.model.CsvReporterConfig;
import org.wso2.carbon.metrics.core.config.model.DasReporterConfig;
import org.wso2.carbon.metrics.core.config.model.JdbcReporterConfig;
import org.wso2.carbon.metrics.core.config.model.JmxReporterConfig;
import org.wso2.carbon.metrics.core.config.model.MetricsConfig;
import org.wso2.carbon.metrics.core.config.model.Slf4jReporterConfig;
import org.wso2.carbon.metrics.core.jmx.MetricManagerMXBean;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.metrics.core.service.MetricService;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Test Cases for Reporters
 */
public class ReporterTest extends BaseReporterTest {

    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    private static final String MBEAN_NAME = "org.wso2.carbon:type=MetricManager";

    private final Gauge<Integer> gauge = () -> 1;

    @BeforeMethod
    private void deleteData() {
        template.execute("DELETE FROM METRIC_GAUGE;");
        template.execute("DELETE FROM METRIC_TIMER;");
        template.execute("DELETE FROM METRIC_METER;");
        template.execute("DELETE FROM METRIC_HISTOGRAM;");
        template.execute("DELETE FROM METRIC_COUNTER;");
    }

    @BeforeClass
    private void stopReporters() {
        metricService.stopReporters();
    }

    @Test
    public void testJMXReporter() {
        metricService.startReporter("JMX");
        Assert.assertTrue(metricService.isReporterRunning("JMX"));
        String meterName = MetricManager.name(this.getClass(), "test-jmx-meter");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricManager.name(this.getClass(), "test-jmx-gauge");
        metricService.gauge(gaugeName, Level.INFO, gauge);

        try {
            AttributeList meterAttributes = getAttributes(meterName, "Count");
            SortedMap<String, Object> meterMap = values(meterAttributes);
            Assert.assertTrue(meterMap.containsKey("Count"), "Meter should be available");
            Assert.assertTrue(meterMap.containsValue(1L), "Meter count should be one");

            AttributeList gaugeAttributes = getAttributes(gaugeName, "Value");
            SortedMap<String, Object> gaugeMap = values(gaugeAttributes);
            Assert.assertTrue(gaugeMap.containsKey("Value"), "Gauge should be available");
            Assert.assertTrue(gaugeMap.containsValue(1), "Gauge value should be one");
        } catch (MetricNotFoundException e) {
            Assert.fail(e.getMessage());
        }
        metricService.stopReporter("JMX");
    }

    @Test
    public void testDisabledGauge() {
        metricService.startReporter("JMX");
        Assert.assertTrue(metricService.isReporterRunning("JMX"));
        String gaugeName = MetricManager.name(this.getClass(), "test-disabled-gauge");
        metricService.gauge(gaugeName, Level.INFO, gauge);
        metricService.setMetricLevel(gaugeName, Level.OFF);

        try {
            getAttributes(gaugeName, "Value");
            Assert.fail("Gauge should not be available");
        } catch (MetricNotFoundException e) {
            // This is expected
        }
        metricService.stopReporter("JMX");
    }

    @Test
    public void testInvalidReporter() {
        try {
            metricService.startReporter("INVALID");
            Assert.fail("The reporter should not be started");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testCSVReporter() {
        metricService.startReporter("CSV");
        Assert.assertTrue(metricService.isReporterRunning("CSV"));
        String meterName = MetricManager.name(this.getClass(), "test-csv-meter");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricManager.name(this.getClass(), "test-csv-gauge");
        metricService.gauge(gaugeName, Level.INFO, gauge);

        metricService.report();
        Assert.assertTrue(new File("target/metrics", meterName + ".csv").exists(), "Meter CSV file should be created");
        Assert.assertTrue(new File("target/metrics", gaugeName + ".csv").exists(), "Gauge CSV file should be created");
        metricService.stopReporter("CSV");
        Assert.assertFalse(metricService.isReporterRunning("CSV"));
    }

    @Test
    public void testConsoleReporter() {
        metricService.startReporter("Console");
        Assert.assertTrue(metricService.isReporterRunning("Console"));
        metricService.report();
        metricService.stopReporter("Console");
        Assert.assertFalse(metricService.isReporterRunning("Console"));
    }

    @Test
    public void testSlf4jReporter() {
        metricService.startReporter("SLF4J");
        Assert.assertTrue(metricService.isReporterRunning("SLF4J"));
        metricService.report();
        metricService.stopReporter("SLF4J");
        Assert.assertFalse(metricService.isReporterRunning("SLF4J"));
    }

    @Test
    public void testCSVReporterRestart() {
        metricService.startReporter("CSV");
        Assert.assertTrue(metricService.isReporterRunning("CSV"));
        String meterName = MetricManager.name(this.getClass(), "test-csv-meter1");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();

        metricService.report();
        Assert.assertTrue(new File("target/metrics", meterName + ".csv").exists(), "Meter CSV file should be created");

        metricService.stopReporter("CSV");
        Assert.assertFalse(metricService.isReporterRunning("CSV"));
        String meterName2 = MetricManager.name(this.getClass(), "test-csv-meter2");
        File meter2File = new File("target/metrics", meterName2 + ".csv");
        // Delete the file first, it might be there from a previous execution
        meter2File.delete();
        meter = metricService.meter(meterName2, Level.INFO);
        meter.mark();

        metricService.report();
        Assert.assertFalse(meter2File.exists(), "Meter CSV file should NOT be created");

        metricService.startReporter("CSV");
        Assert.assertTrue(metricService.isReporterRunning("CSV"));
        metricService.report();

        Assert.assertTrue(meter2File.exists(), "Meter2 CSV file should be created");
        metricService.stopReporter("CSV");
        Assert.assertFalse(metricService.isReporterRunning("CSV"));
    }

    @Test
    public void testCSVReporterRestart2() {
        metricService.startReporter("CSV");
        Assert.assertTrue(metricService.isReporterRunning("CSV"));
        String meterName3 = MetricManager.name(this.getClass(), "test-csv-meter3");
        Meter meter = metricService.meter(meterName3, Level.INFO);
        meter.mark();

        metricService.report();
        Assert.assertTrue(new File("target/metrics", meterName3 + ".csv").exists(), "Meter CSV file should be created");

        metricService.stopReporter("CSV");
        String meterName4 = MetricManager.name(this.getClass(), "test-csv-meter4");
        File meter4File = new File("target/metrics", meterName4 + ".csv");
        // Delete the file first, it might be there from a previous execution
        meter4File.delete();
        meter = metricService.meter(meterName4, Level.INFO);
        meter.mark();

        metricService.report();
        Assert.assertFalse(meter4File.exists(), "Meter CSV file should not be created");

        metricService.startReporter("CSV");
        metricService.report();
        Assert.assertTrue(meter4File.exists(), "Meter CSV file should be created");
        metricService.stopReporter("CSV");
        Assert.assertFalse(metricService.isReporterRunning("CSV"));
    }

    @Test
    public void testDasReporter() {
        metricService.startReporter("DAS");
        Assert.assertTrue(metricService.isReporterRunning("DAS"));
        String meterName = MetricManager.name(this.getClass(), "test-das-meter");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricManager.name(this.getClass(), "test-das-gauge");
        metricService.gauge(gaugeName, Level.INFO, gauge);

        metricService.report();

        Event event = TEST_EVENT_SERVER.getEvent("meter", meterName);
        Assert.assertEquals(event.getPayloadData()[2], 1L);

        event = TEST_EVENT_SERVER.getEvent("gauge", gaugeName);
        Assert.assertEquals(event.getPayloadData()[2], 1.0D);
        metricService.stopReporter("DAS");
        Assert.assertFalse(metricService.isReporterRunning("DAS"));
    }

    @Test
    public void testJDBCReporter() {
        metricService.startReporter("JDBC");
        Assert.assertTrue(metricService.isReporterRunning("JDBC"));
        String meterName = MetricManager.name(this.getClass(), "test-jdbc-meter");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricManager.name(this.getClass(), "test-jdbc-gauge");
        metricService.gauge(gaugeName, Level.INFO, gauge);

        metricService.report();
        List<Map<String, Object>> meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);
        Assert.assertEquals(meterResult.get(0).get("NAME"), meterName);
        Assert.assertEquals(meterResult.get(0).get("COUNT"), 1L);

        List<Map<String, Object>> gaugeResult =
                template.queryForList("SELECT * FROM METRIC_GAUGE WHERE NAME = ?", gaugeName);
        Assert.assertEquals(gaugeResult.size(), 1);
        Assert.assertEquals(gaugeResult.get(0).get("NAME"), gaugeName);
        Assert.assertEquals(gaugeResult.get(0).get("VALUE"), "1");
        metricService.stopReporter("JDBC");
        Assert.assertFalse(metricService.isReporterRunning("JDBC"));
    }

    @Test
    public void testJDBCReporterRestart() {
        metricService.startReporter("JDBC");
        Assert.assertTrue(metricService.isReporterRunning("JDBC"));
        String meterName = MetricManager.name(this.getClass(), "test-jdbc-meter1");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();

        metricService.report();
        List<Map<String, Object>> meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);

        metricService.stopReporter("JDBC");
        Assert.assertFalse(metricService.isReporterRunning("JDBC"));
        metricService.report();
        meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);

        metricService.startReporter("JDBC");
        Assert.assertTrue(metricService.isReporterRunning("JDBC"));
        metricService.report();

        meterResult = template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 2);
        metricService.stopReporter("JDBC");
        Assert.assertFalse(metricService.isReporterRunning("JDBC"));
    }

    @Test
    public void testJDBCReporterCustomDatasource() {
        // reload with custom jdbc config
        System.setProperty("metrics.conf", "src/test/resources/conf/metrics-jdbc.yml");
        System.setProperty("metrics.datasource.conf", "src/test/resources/conf/metrics-datasource.properties");
        MetricService metricService = new MetricService(new MetricRegistry(), MetricsConfigBuilder.build(),
                MetricsLevelConfigBuilder.build());
        metricService.startReporter("JDBC");
        Assert.assertTrue(metricService.isReporterRunning("JDBC"));

        String meterName = MetricManager.name(this.getClass(), "test-jdbc-datasource");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();

        metricService.report();
        List<Map<String, Object>> meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);
        Assert.assertEquals(meterResult.get(0).get("NAME"), meterName);
        Assert.assertEquals(meterResult.get(0).get("COUNT"), 1L);
        metricService.stopReporter("JDBC");
        Assert.assertFalse(metricService.isReporterRunning("JDBC"));
    }

    @Test
    public void testJVMMetricSetLevel() {
        metricService.startReporter("JMX");
        Assert.assertTrue(metricService.isReporterRunning("JMX"));
        // This test is to check restarting of listener reporters
        String name = "jvm.threads.runnable.count";
        // Initially this gauge is set to OFF and when changing the level, we need to restart JMXReporter
        metricService.setMetricLevel(name, Level.TRACE);
        Assert.assertEquals(metricService.getMetricLevel(name), Level.TRACE, "Configured level should be TRACE");
        try {
            AttributeList gaugeAttributes = getAttributes(name, "Value");
            SortedMap<String, Object> gaugeMap = values(gaugeAttributes);
            Assert.assertTrue(gaugeMap.containsKey("Value"), "Gauge should be available");
            Assert.assertTrue(((Integer) gaugeMap.get("Value")) > 0, "Gauge value should be a positive number");
        } catch (MetricNotFoundException e) {
            Assert.fail(e.getMessage());
        }
        metricService.stopReporter("JMX");
        Assert.assertFalse(metricService.isReporterRunning("JMX"));
    }

    @Test
    public void testJMXReport() throws ReporterBuildException {
        MetricsConfig metricsConfig = metricService.getMetricsConfig();
        MetricManager.getMetricService().addReporter(metricsConfig.getReporting().getJdbc());
        MetricManager.getMetricService().startReporter("JDBC");
        Assert.assertTrue(MetricManager.getMetricService().isReporterRunning("JDBC"));
        String meterName = MetricManager.name(this.getClass(), "test-jmx-report-meter");
        Meter meter = MetricManager.meter(meterName, Level.INFO);
        meter.mark();

        invokeJMXReportOperation();
        List<Map<String, Object>> meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);
        Assert.assertEquals(meterResult.get(0).get("NAME"), meterName, "Meter should be available");
        Assert.assertEquals(meterResult.get(0).get("COUNT"), 1L, "Meter count should be one");
        MetricManager.getMetricService().stopReporter("JDBC");
        Assert.assertFalse(metricService.isReporterRunning("JDBC"));
    }

    private void invokeJMXReportOperation() {
        ObjectName n;
        try {
            n = new ObjectName(MBEAN_NAME);
            MetricManagerMXBean metricManagerMXBean = JMX.newMXBeanProxy(mBeanServer, n, MetricManagerMXBean.class);
            metricManagerMXBean.report();
        } catch (MalformedObjectNameException e) {
            Assert.fail(e.getMessage());
        }
    }

    private AttributeList getAttributes(String name, String... attributeNames) throws MetricNotFoundException {
        ObjectName n;
        try {
            n = new ObjectName("org.wso2.carbon.metrics.test", "name", name);
            return mBeanServer.getAttributes(n, attributeNames);
        } catch (MalformedObjectNameException | ReflectionException e) {
            Assert.fail(e.getMessage());
        } catch (InstanceNotFoundException e) {
            throw new MetricNotFoundException(e);
        }
        return null;
    }

    private SortedMap<String, Object> values(AttributeList attributes) {
        final TreeMap<String, Object> values = new TreeMap<>();
        if (attributes != null) {
            for (Object o : attributes) {
                final Attribute attribute = (Attribute) o;
                values.put(attribute.getName(), attribute.getValue());
            }
        }
        return values;
    }

    @Test
    public void testJmxReporterValidations() {
        JmxReporterConfig jmxReporterConfig = new JmxReporterConfig();
        jmxReporterConfig.setEnabled(true);
        jmxReporterConfig.setDomain("");
        addReporter(jmxReporterConfig);
    }

    @Test
    public void testCSVReporterValidations() {
        CsvReporterConfig csvReporterConfig = new CsvReporterConfig();
        csvReporterConfig.setEnabled(true);
        csvReporterConfig.setLocation("");
        addReporter(csvReporterConfig);

        csvReporterConfig.setLocation(TEST_RESOURCES_DIR + File.separator + "log4j2.xml");
        addReporter(csvReporterConfig);
    }

    @Test
    public void testSlf4jReporterValidations() {
        Slf4jReporterConfig slf4jReporterConfig = new Slf4jReporterConfig();
        slf4jReporterConfig.setEnabled(true);
        slf4jReporterConfig.setLoggerName("");
        addReporter(slf4jReporterConfig);
    }

    @Test
    public void testJDBCReporterValidations() {
        System.setProperty("metrics.datasource.conf", "invalid");
        JdbcReporterConfig jdbcReporterConfig = new JdbcReporterConfig();
        jdbcReporterConfig.setEnabled(true);
        jdbcReporterConfig.setLookupDataSource(true);
        addReporter(jdbcReporterConfig);
        jdbcReporterConfig.setDataSourceName("");
        addReporter(jdbcReporterConfig);

        jdbcReporterConfig.setDataSourceName("jdbc/Invalid");
        addReporter(jdbcReporterConfig);

        jdbcReporterConfig.setLookupDataSource(false);
        addReporter(jdbcReporterConfig);
    }

    @Test
    public void testDasReporterValidations() {
        String name = "DAS-TEST";
        DasReporterConfig dasReporterConfig = new DasReporterConfig();
        dasReporterConfig.setName(name);
        dasReporterConfig.setEnabled(true);
        dasReporterConfig.setAuthURL("ssl://localhost:7711");
        dasReporterConfig.setType(null);
        dasReporterConfig.setReceiverURL(null);
        dasReporterConfig.setUsername(null);
        dasReporterConfig.setPassword(null);
        addReporter(dasReporterConfig);

        dasReporterConfig.setType("");
        addReporter(dasReporterConfig);

        dasReporterConfig.setType("thrift");
        addReporter(dasReporterConfig);

        dasReporterConfig.setReceiverURL("");
        addReporter(dasReporterConfig);

        dasReporterConfig.setReceiverURL("tcp://localhost:7611");
        addReporter(dasReporterConfig);

        dasReporterConfig.setUsername("");
        addReporter(dasReporterConfig);

        dasReporterConfig.setUsername("admin");
        addReporter(dasReporterConfig);

        dasReporterConfig.setPassword("");
        addReporter(dasReporterConfig);

        dasReporterConfig.setPassword("admin");
        System.setProperty("metrics.dataagent.conf", "invalid.xml");
        try {
            metricService.addReporter(dasReporterConfig);
            // Add again to update
            metricService.addReporter(dasReporterConfig);
        } catch (ReporterBuildException e) {
            Assert.fail("Reporter should be created");
        }
        Assert.assertTrue(metricService.removeReporter(name));
        Assert.assertFalse(metricService.removeReporter(name));
    }

    private <T extends ReporterBuilder> void addReporter(T reporterBuilder) {
        try {
            metricService.addReporter(reporterBuilder);
            Assert.fail("Add Reporter should fail.");
        } catch (ReporterBuildException e) {
        }
    }
}
