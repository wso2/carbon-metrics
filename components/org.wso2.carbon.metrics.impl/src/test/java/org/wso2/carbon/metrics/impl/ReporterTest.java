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
package org.wso2.carbon.metrics.impl;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.wso2.carbon.metrics.manager.Gauge;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

/**
 * Test Cases for Reporters in {@link MetricService}
 */
public class ReporterTest extends TestCase {

    private MetricServiceImpl metricService;

    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    private static JdbcTemplate template;

    private String meterName = MetricManager.name(this.getClass(), "test-meter");

    private String gaugeName = MetricManager.name(this.getClass(), "test-gauge");

    public static Test suite() {
        return new TestSetup(new TestSuite(ReporterTest.class)) {

            protected void setUp() throws Exception {
                DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
                template = new JdbcTemplate(dataSource);
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("dbscripts/h2.sql"));
                populator.populate(dataSource.getConnection());

                // Create initial context
                System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
                System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
                InitialContext ic = new InitialContext();
                ic.createSubcontext("jdbc");
                ic.bind("jdbc/WSO2MetricsDB", dataSource);

                // Set setup system property to cover database creator logic
                System.setProperty("setup", "");
            }

            protected void tearDown() throws Exception {
                InitialContext ic = new InitialContext();
                ic.unbind("jdbc/WSO2MetricsDB");
                ic.unbind("jdbc");
            }
        };
    }

    protected void setUp() throws Exception {
        super.setUp();

        metricService = new MetricServiceImpl(Utils.getConfigurationWithReporters(), Utils.getLevelConfiguration());
        metricService.setRootLevel(Level.ALL);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);

        Meter meter = MetricManager.meter(Level.INFO, meterName);
        meter.mark();

        Gauge<Integer> gauge = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 1;
            }
        };

        MetricManager.gauge(Level.INFO, gaugeName, gauge);

        template.execute("DELETE FROM METRIC_GAUGE;");
        template.execute("DELETE FROM METRIC_TIMER;");
        template.execute("DELETE FROM METRIC_METER;");
        template.execute("DELETE FROM METRIC_HISTOGRAM;");
        template.execute("DELETE FROM METRIC_COUNTER;");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // Disable to stop reporters
        metricService.disable();
    }

    public void testJMXReporter() {
        AttributeList meterAttributes = getAttributes(meterName, "Count");
        SortedMap<String, Object> meterMap = values(meterAttributes);
        assertTrue("Meter is available", meterMap.containsKey("Count"));
        assertTrue("Meter count is one", meterMap.containsValue(1L));

        AttributeList gaugeAttributes = getAttributes(gaugeName, "Value");
        SortedMap<String, Object> gaugeMap = values(gaugeAttributes);
        assertTrue("Gauge is available", gaugeMap.containsKey("Value"));
        assertTrue("Gauge value is one", gaugeMap.containsValue(1));
    }

    public void testCSVReporter() {
        metricService.report();
        assertTrue("Meter CSV file is created", new File("target/metrics-logs", meterName + ".csv").exists());
        assertTrue("Gauge CSV file is created", new File("target/metrics-logs", gaugeName + ".csv").exists());
    }

    public void testCSVReporterRestart() {
        metricService.report();
        assertTrue("Meter CSV file is created", new File("target/metrics-logs", meterName + ".csv").exists());

        metricService.disable();
        String meterName2 = MetricManager.name(this.getClass(), "test-meter2");
        Meter meter = MetricManager.meter(Level.INFO, meterName2);
        meter.mark();

        metricService.report();
        metricService.enable();
        metricService.report();

        assertTrue("Meter2 CSV file is created", new File("target/metrics-logs", meterName2 + ".csv").exists());
    }

    public void testJDBCReporter() {
        metricService.report();
        List<Map<String, Object>> meterResult = template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?",
                meterName);
        assertEquals("There is one result", 1, meterResult.size());
        assertEquals("Meter is available", meterName, meterResult.get(0).get("NAME"));
        assertEquals("Meter count is one", 1L, meterResult.get(0).get("COUNT"));

        List<Map<String, Object>> gaugeResult = template.queryForList("SELECT * FROM METRIC_GAUGE WHERE NAME = ?",
                gaugeName);
        assertEquals("There is one result", 1, gaugeResult.size());
        assertEquals("Gauge is available", gaugeName, gaugeResult.get(0).get("NAME"));
        assertEquals("Gauge value is one", "1", gaugeResult.get(0).get("VALUE"));
    }

    public void testJDBCReporterRestart() {
        metricService.report();
        List<Map<String, Object>> meterResult = template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?",
                meterName);
        assertEquals("There is one result", 1, meterResult.size());

        metricService.disable();
        metricService.report();
        metricService.enable();
        metricService.report();

        meterResult = template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        assertEquals("There are two results", 2, meterResult.size());
    }

    private AttributeList getAttributes(String name, String... attributeNames) {
        ObjectName n;
        try {
            n = new ObjectName("org.wso2.carbon.metrics", "name", name);
            return mBeanServer.getAttributes(n, attributeNames);
        } catch (MalformedObjectNameException e) {
            fail(e.getMessage());
        } catch (InstanceNotFoundException e) {
            fail(e.getMessage());
        } catch (ReflectionException e) {
            fail(e.getMessage());
        }
        return null;
    }

    private SortedMap<String, Object> values(AttributeList attributes) {
        final TreeMap<String, Object> values = new TreeMap<String, Object>();
        if (attributes != null) {
            for (Object o : attributes) {
                final Attribute attribute = (Attribute) o;
                values.put(attribute.getName(), attribute.getValue());
            }
        }
        return values;
    }

}
