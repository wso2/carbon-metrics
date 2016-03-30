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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.jmx.MetricManagerMXBean;

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
public class ReporterTest extends BaseTest {

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

    @Test
    public void testJMXReporter() {
        String meterName = MetricManager.name(this.getClass(), "test-jmx-meter");
        Meter meter = MetricManager.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricManager.name(this.getClass(), "test-jmx-gauge");
        MetricManager.gauge(gaugeName, Level.INFO, gauge);

        AttributeList meterAttributes = getAttributes(meterName, "Count");
        SortedMap<String, Object> meterMap = values(meterAttributes);
        Assert.assertTrue(meterMap.containsKey("Count"), "Meter should be available");
        Assert.assertTrue(meterMap.containsValue(1L), "Meter count should be one");

        AttributeList gaugeAttributes = getAttributes(gaugeName, "Value");
        SortedMap<String, Object> gaugeMap = values(gaugeAttributes);
        Assert.assertTrue(gaugeMap.containsKey("Value"), "Gauge should be available");
        Assert.assertTrue(gaugeMap.containsValue(1), "Gauge value should be one");
    }

    @Test
    public void testCSVReporter() {
        String meterName = MetricManager.name(this.getClass(), "test-csv-meter");
        Meter meter = MetricManager.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricManager.name(this.getClass(), "test-csv-gauge");
        MetricManager.gauge(gaugeName, Level.INFO, gauge);

        metricService.report();
        Assert.assertTrue(new File("target/metrics", meterName + ".csv").exists(), "Meter CSV file should be created");
        Assert.assertTrue(new File("target/metrics", gaugeName + ".csv").exists(), "Gauge CSV file should be created");
    }

    @Test
    public void testCSVReporterRestart() {
        String meterName = MetricManager.name(this.getClass(), "test-csv-meter1");
        Meter meter = MetricManager.meter(meterName, Level.INFO);
        meter.mark();

        metricService.report();
        Assert.assertTrue(new File("target/metrics", meterName + ".csv").exists(), "Meter CSV file should be created");

        metricService.disable();
        String meterName2 = MetricManager.name(this.getClass(), "test-csv-meter2");
        meter = MetricManager.meter(meterName2, Level.INFO);
        meter.mark();

        metricService.report();
        Assert.assertFalse(new File("target/metrics", meterName2 + ".csv").exists(),
                "Meter CSV file should NOT be created");

        metricService.enable();
        metricService.report();

        Assert.assertTrue(new File("target/metrics", meterName2 + ".csv").exists(),
                "Meter2 CSV file should be created");
    }

    @Test
    public void testJDBCReporter() {
        String meterName = MetricManager.name(this.getClass(), "test-jdbc-meter");
        Meter meter = MetricManager.meter(meterName, Level.INFO);
        meter.mark();
        String gaugeName = MetricManager.name(this.getClass(), "test-jdbc-gauge");
        MetricManager.gauge(gaugeName, Level.INFO, gauge);

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
    }

    @Test
    public void testJDBCReporterRestart() {
        String meterName = MetricManager.name(this.getClass(), "test-jdbc-meter1");
        Meter meter = MetricManager.meter(meterName, Level.INFO);
        meter.mark();

        metricService.report();
        List<Map<String, Object>> meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);

        metricService.disable();
        metricService.report();
        meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);

        metricService.enable();
        metricService.report();

        meterResult = template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 2);
    }

    @Test
    public void testJVMMetricSetLevel() {
        // This test is to check restarting of listener reporters
        String name = "jvm.threads.runnable.count";
        // Initially this gauge is set to OFF and when changing the level, we need to restart JMXReporter
        metricService.setMetricLevel(name, Level.TRACE);
        Assert.assertEquals(metricService.getMetricLevel(name), Level.TRACE, "Configured level should be TRACE");
        AttributeList gaugeAttributes = getAttributes(name, "Value");
        SortedMap<String, Object> gaugeMap = values(gaugeAttributes);
        Assert.assertTrue(gaugeMap.containsKey("Value"), "Gauge should be available");
        Assert.assertTrue(((Integer) gaugeMap.get("Value")) > 0, "Gauge value should be a positive number");
    }

    @Test
    public void testJMXReport() {
        String meterName = MetricManager.name(this.getClass(), "test-jmx-report-meter");
        Meter meter = MetricManager.meter(meterName, Level.INFO);
        meter.mark();

        invokeJMXReportOperation();
        List<Map<String, Object>> meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);
        Assert.assertEquals(meterResult.get(0).get("NAME"), meterName, "Meter should be available");
        Assert.assertEquals(meterResult.get(0).get("COUNT"), 1L, "Meter count should be one");
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

    private AttributeList getAttributes(String name, String... attributeNames) {
        ObjectName n;
        try {
            n = new ObjectName("org.wso2.carbon.metrics", "name", name);
            return mBeanServer.getAttributes(n, attributeNames);
        } catch (MalformedObjectNameException e) {
            Assert.fail(e.getMessage());
        } catch (InstanceNotFoundException e) {
            Assert.fail(e.getMessage());
        } catch (ReflectionException e) {
            Assert.fail(e.getMessage());
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

}
