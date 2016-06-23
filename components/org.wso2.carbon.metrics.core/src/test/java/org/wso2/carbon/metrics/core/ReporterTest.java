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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.metrics.core.config.model.CsvReporterConfig;
import org.wso2.carbon.metrics.core.config.model.DasConfig;
import org.wso2.carbon.metrics.core.config.model.DasReporterConfig;
import org.wso2.carbon.metrics.core.config.model.DataSourceConfig;
import org.wso2.carbon.metrics.core.config.model.JdbcReporterConfig;
import org.wso2.carbon.metrics.core.config.model.JmxReporterConfig;
import org.wso2.carbon.metrics.core.config.model.Slf4jReporterConfig;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Test Cases for Reporters
 */
public class ReporterTest extends BaseReporterTest {

    private static final Logger logger = LoggerFactory.getLogger(ReporterTest.class);

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
        metricManagementService.stopReporters();
    }

    @Test
    public void testJMXReporter() {
        metricManagementService.startReporter("JMX");
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX"));
        String meterName = MetricService.name(this.getClass(), "test-jmx-meter");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricService.name(this.getClass(), "test-jmx-gauge");
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
        metricManagementService.stopReporter("JMX");
    }

    @Test(expectedExceptions = MetricNotFoundException.class)
    public void testDisabledGauge() throws MetricNotFoundException {
        metricManagementService.startReporter("JMX");
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX"));
        String gaugeName = MetricService.name(this.getClass(), "test-disabled-gauge");
        metricService.gauge(gaugeName, Level.INFO, gauge);
        metricManagementService.setMetricLevel(gaugeName, Level.OFF);

        try {
            getAttributes(gaugeName, "Value");
            Assert.fail("Gauge should not be available");
        } finally {
            metricManagementService.stopReporter("JMX");
        }
    }

    @Test
    public void testMetricRemove() {
        metricManagementService.startReporter("JMX");
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX"));
        String counterName = MetricService.name(this.getClass(), "remove[+].sub.counter");
        String subName = MetricService.name(this.getClass(), "remove.sub.counter");
        String mainName = MetricService.name(this.getClass(), "remove.counter");
        metricService.counter(counterName, Level.INFO, Level.INFO);

        // Test sub counter remove
        testCounterRemove(subName);

        // Test main counter remove
        testCounterRemove(mainName);

        Assert.assertTrue(metricService.remove(counterName));
        Assert.assertFalse(metricService.remove(counterName));

        metricManagementService.stopReporter("JMX");
    }

    private void testCounterRemove(String name) {
        try {
            AttributeList gaugeAttributes = getAttributes(name, "Count");
            SortedMap<String, Object> gaugeMap = values(gaugeAttributes);
            Assert.assertTrue(gaugeMap.containsKey("Count"), "Counter should be available");
        } catch (MetricNotFoundException e) {
            Assert.fail("Counter should be available");
        }

        Assert.assertTrue(metricService.remove(name));
        Assert.assertFalse(metricService.remove(name));

        try {
            getAttributes(name, "Count");
            Assert.fail("Counter should not be available");
        } catch (MetricNotFoundException e) {
            // Ignore. Cannot throw as there are more asserts
        }
    }

    @Test
    public void testInvalidReporter() {
        try {
            metricManagementService.startReporter("INVALID");
            Assert.fail("The reporter should not be started");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testCSVReporter() {
        metricManagementService.startReporter("CSV");
        Assert.assertTrue(metricManagementService.isReporterRunning("CSV"));
        String meterName = MetricService.name(this.getClass(), "test-csv-meter");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricService.name(this.getClass(), "test-csv-gauge");
        metricService.gauge(gaugeName, Level.INFO, gauge);

        metricManagementService.report();
        Assert.assertTrue(new File("target/metrics", meterName + ".csv").exists(), "Meter CSV file should be created");
        Assert.assertTrue(new File("target/metrics", gaugeName + ".csv").exists(), "Gauge CSV file should be created");
        metricManagementService.stopReporter("CSV");
        Assert.assertFalse(metricManagementService.isReporterRunning("CSV"));
    }

    @Test
    public void testConsoleReporter() {
        metricManagementService.startReporter("Console");
        Assert.assertTrue(metricManagementService.isReporterRunning("Console"));
        metricManagementService.report();
        metricManagementService.stopReporter("Console");
        Assert.assertFalse(metricManagementService.isReporterRunning("Console"));
    }

    @Test
    public void testSlf4jReporter() {
        metricManagementService.startReporter("SLF4J");
        Assert.assertTrue(metricManagementService.isReporterRunning("SLF4J"));
        metricManagementService.report();
        metricManagementService.stopReporter("SLF4J");
        Assert.assertFalse(metricManagementService.isReporterRunning("SLF4J"));
    }

    @Test
    public void testCSVReporterRestart() {
        metricManagementService.startReporter("CSV");
        Assert.assertTrue(metricManagementService.isReporterRunning("CSV"));
        String meterName = MetricService.name(this.getClass(), "test-csv-meter1");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();

        metricManagementService.report();
        Assert.assertTrue(new File("target/metrics", meterName + ".csv").exists(), "Meter CSV file should be created");

        metricManagementService.stopReporter("CSV");
        Assert.assertFalse(metricManagementService.isReporterRunning("CSV"));
        String meterName2 = MetricService.name(this.getClass(), "test-csv-meter2");
        File meter2File = new File("target/metrics", meterName2 + ".csv");
        // Delete the file first, it might be there from a previous execution
        meter2File.delete();
        meter = metricService.meter(meterName2, Level.INFO);
        meter.mark();

        metricManagementService.report();
        Assert.assertFalse(meter2File.exists(), "Meter CSV file should NOT be created");

        metricManagementService.startReporter("CSV");
        Assert.assertTrue(metricManagementService.isReporterRunning("CSV"));
        metricManagementService.report();

        Assert.assertTrue(meter2File.exists(), "Meter2 CSV file should be created");
        metricManagementService.stopReporter("CSV");
        Assert.assertFalse(metricManagementService.isReporterRunning("CSV"));
    }

    @Test
    public void testCSVReporterRestart2() {
        metricManagementService.startReporter("CSV");
        Assert.assertTrue(metricManagementService.isReporterRunning("CSV"));
        String meterName3 = MetricService.name(this.getClass(), "test-csv-meter3");
        Meter meter = metricService.meter(meterName3, Level.INFO);
        meter.mark();

        metricManagementService.report();
        Assert.assertTrue(new File("target/metrics", meterName3 + ".csv").exists(), "Meter CSV file should be created");

        metricManagementService.stopReporter("CSV");
        String meterName4 = MetricService.name(this.getClass(), "test-csv-meter4");
        File meter4File = new File("target/metrics", meterName4 + ".csv");
        // Delete the file first, it might be there from a previous execution
        meter4File.delete();
        meter = metricService.meter(meterName4, Level.INFO);
        meter.mark();

        metricManagementService.report();
        Assert.assertFalse(meter4File.exists(), "Meter CSV file should not be created");

        metricManagementService.startReporter("CSV");
        metricManagementService.report();
        Assert.assertTrue(meter4File.exists(), "Meter CSV file should be created");
        metricManagementService.stopReporter("CSV");
        Assert.assertFalse(metricManagementService.isReporterRunning("CSV"));
    }

    @Test
    public void testDasReporter() {
        metricManagementService.startReporter("DAS");
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
        metricManagementService.stopReporter("DAS");
        Assert.assertFalse(metricManagementService.isReporterRunning("DAS"));
    }

    @Test
    public void testJDBCReporter() {
        metricManagementService.startReporter("JDBC");
        Assert.assertTrue(metricManagementService.isReporterRunning("JDBC"));
        String meterName = MetricService.name(this.getClass(), "test-jdbc-meter");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricService.name(this.getClass(), "test-jdbc-gauge");
        metricService.gauge(gaugeName, Level.INFO, gauge);

        metricManagementService.report();
        List<Map<String, Object>> meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);
        Assert.assertEquals(meterResult.get(0).get("NAME"), meterName);
        Assert.assertEquals(meterResult.get(0).get("COUNT"), 1L);
        Assert.assertEquals(meterResult.get(0).get("SOURCE"), "Carbon-jdbc");

        List<Map<String, Object>> gaugeResult =
                template.queryForList("SELECT * FROM METRIC_GAUGE WHERE NAME = ?", gaugeName);
        Assert.assertEquals(gaugeResult.size(), 1);
        Assert.assertEquals(gaugeResult.get(0).get("NAME"), gaugeName);
        Assert.assertEquals(gaugeResult.get(0).get("VALUE"), "1");
        Assert.assertEquals(gaugeResult.get(0).get("SOURCE"), "Carbon-jdbc");
        metricManagementService.stopReporter("JDBC");
        Assert.assertFalse(metricManagementService.isReporterRunning("JDBC"));
    }

    @Test
    public void testJDBCReporterCachedGauge() {
        metricManagementService.startReporter("JDBC");
        Assert.assertTrue(metricManagementService.isReporterRunning("JDBC"));
        String gaugeName = MetricService.name(this.getClass(), "test-jdbc-cached-gauge");
        LongAdder adder = new LongAdder();
        adder.increment();
        Assert.assertEquals(adder.longValue(), 1L);

        Gauge<Long> gauge = () -> adder.longValue();
        metricService.cachedGauge(gaugeName, Level.INFO, 1, TimeUnit.HOURS, gauge);

        metricManagementService.report();
        List<Map<String, Object>> gaugeResult =
                template.queryForList("SELECT * FROM METRIC_GAUGE WHERE NAME = ?", gaugeName);
        Assert.assertEquals(gaugeResult.size(), 1);
        Assert.assertEquals(gaugeResult.get(0).get("NAME"), gaugeName);
        Assert.assertEquals(gaugeResult.get(0).get("VALUE"), "1");
        Assert.assertEquals(gaugeResult.get(0).get("SOURCE"), "Carbon-jdbc");

        adder.increment();
        Assert.assertEquals(adder.longValue(), 2L);

        metricManagementService.report();
        List<Map<String, Object>> gaugeResult2 =
                template.queryForList("SELECT * FROM METRIC_GAUGE WHERE NAME = ? ORDER BY TIMESTAMP", gaugeName);
        Assert.assertEquals(gaugeResult2.size(), 2);
        Assert.assertEquals(gaugeResult2.get(1).get("NAME"), gaugeResult2.get(0).get("NAME"));
        Assert.assertEquals(gaugeResult2.get(1).get("VALUE"), gaugeResult2.get(0).get("VALUE"));
        Assert.assertEquals(gaugeResult2.get(1).get("SOURCE"), gaugeResult2.get(0).get("SOURCE"));

        metricManagementService.stopReporter("JDBC");
        Assert.assertFalse(metricManagementService.isReporterRunning("JDBC"));
    }

    @Test
    public void testJDBCReporterRestart() {
        metricManagementService.startReporter("JDBC");
        Assert.assertTrue(metricManagementService.isReporterRunning("JDBC"));
        String meterName = MetricService.name(this.getClass(), "test-jdbc-meter1");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();

        metricManagementService.report();
        List<Map<String, Object>> meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);

        metricManagementService.stopReporter("JDBC");
        Assert.assertFalse(metricManagementService.isReporterRunning("JDBC"));
        metricManagementService.report();
        meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);

        metricManagementService.startReporter("JDBC");
        Assert.assertTrue(metricManagementService.isReporterRunning("JDBC"));
        metricManagementService.report();

        meterResult = template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 2);
        metricManagementService.stopReporter("JDBC");
        Assert.assertFalse(metricManagementService.isReporterRunning("JDBC"));
    }

    @Test
    public void testJDBCReporterCustomDatasource() {
        // reload with custom jdbc config
        System.setProperty("metrics.conf", RESOURCES_DIR + File.separator + "conf" + File.separator
                + "metrics-jdbc.yml");
        System.setProperty("metrics.datasource.conf", RESOURCES_DIR + File.separator + "conf" + File.separator
                + "metrics-datasource.properties");
        Metrics metrics = new Metrics.Builder().build();
        metrics.activate();
        MetricService metricService = metrics.getMetricService();
        MetricManagementService metricManagementService = metrics.getMetricManagementService();
        Assert.assertTrue(metricManagementService.isReporterRunning("JDBC"));

        String meterName = MetricService.name(this.getClass(), "test-jdbc-datasource");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();

        metricManagementService.report("JDBC");
        List<Map<String, Object>> meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);
        Assert.assertEquals(meterResult.get(0).get("NAME"), meterName);
        Assert.assertEquals(meterResult.get(0).get("COUNT"), 1L);
        metricManagementService.stopReporter("JDBC");
        Assert.assertFalse(metricManagementService.isReporterRunning("JDBC"));
        metrics.deactivate();
    }

    @Test
    public void testJVMMetricSetLevel() {
        metricManagementService.startReporter("JMX");
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX"));
        // This test is to check restarting of listener reporters
        String name = "jvm.threads.runnable.count";
        // Initially this gauge is set to OFF and when changing the level, we need to restart JMXReporter
        metricManagementService.setMetricLevel(name, Level.TRACE);
        Assert.assertEquals(metricManagementService.getMetricLevel(name), Level.TRACE,
                "Configured level should be TRACE");
        try {
            AttributeList gaugeAttributes = getAttributes(name, "Value");
            SortedMap<String, Object> gaugeMap = values(gaugeAttributes);
            Assert.assertTrue(gaugeMap.containsKey("Value"), "Gauge should be available");
            Assert.assertTrue(((Integer) gaugeMap.get("Value")) > 0, "Gauge value should be a positive number");
        } catch (MetricNotFoundException e) {
            Assert.fail(e.getMessage());
        }
        metricManagementService.stopReporter("JMX");
        Assert.assertFalse(metricManagementService.isReporterRunning("JMX"));
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
    public void testJmxReporterValidationsAndRegex() {
        JmxReporterConfig jmxReporterConfig = new JmxReporterConfig();
        jmxReporterConfig.setEnabled(true);
        jmxReporterConfig.setDomain("");
        addReporter(jmxReporterConfig);

        jmxReporterConfig.setDomain("org.wso2.carbon.metrics.valid");
        jmxReporterConfig.setUseRegexFilters(true);
        jmxReporterConfig.setIncludes(new HashSet<>(Arrays.asList("(include")));
        addReporter(jmxReporterConfig);

        jmxReporterConfig.setIncludes(Collections.emptySet());
        jmxReporterConfig.setExcludes(new HashSet<>(Arrays.asList("(exclude")));
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
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        jdbcReporterConfig.setDataSource(dataSourceConfig);
        jdbcReporterConfig.setEnabled(true);
        dataSourceConfig.setLookupDataSource(true);
        addReporter(jdbcReporterConfig);
        dataSourceConfig.setDataSourceName("");
        addReporter(jdbcReporterConfig);

        dataSourceConfig.setDataSourceName("jdbc/Invalid");
        addReporter(jdbcReporterConfig);

        dataSourceConfig.setLookupDataSource(false);
        addReporter(jdbcReporterConfig);
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

        dasConfig.setReceiverURL("tcp://localhost:7611");
        addReporter(dasReporterConfig);

        dasConfig.setUsername("");
        addReporter(dasReporterConfig);

        dasConfig.setUsername("admin");
        addReporter(dasReporterConfig);

        dasConfig.setPassword("");
        addReporter(dasReporterConfig);

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
}
