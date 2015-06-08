/*
 * Copyright 2015 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.data.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.wso2.carbon.metrics.data.common.Metric;
import org.wso2.carbon.metrics.data.common.MetricAttribute;
import org.wso2.carbon.metrics.data.common.MetricDataFormat;
import org.wso2.carbon.metrics.data.common.MetricList;
import org.wso2.carbon.metrics.data.common.MetricType;

/**
 * Test Metrics Data Service
 */
public class MetricsDataServiceTest extends TestCase {

    private MetricsDataService metricsDataService;

    private static JdbcTemplate template;

    private static final String SOURCE = "carbon-server";

    private static final long START_TIME = 1428567356013L;

    private static final long END_TIME = 1428567416013L;

    public static Test suite() {
        return new TestSetup(new TestSuite(MetricsDataServiceTest.class)) {

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

        metricsDataService = new MetricsDataService();
        metricsDataService.init(Utils.getConfiguration());
    }

    public void testAllData() {
        List<Map<String, Object>> gaugeResult = template.queryForList("SELECT * FROM METRIC_GAUGE");
        assertEquals("There are 66 results", 66, gaugeResult.size());
    }

    public void testSpecificData() {
        String gaugeName = "jvm.memory.heap.init";
        List<Map<String, Object>> gaugeResult = template.queryForList("SELECT * FROM METRIC_GAUGE WHERE NAME = ?",
                gaugeName);
        assertEquals("There are two results", 2, gaugeResult.size());
    }

    public void testSources() {
        List<String> sources = metricsDataService.getAllSources();
        assertEquals("There is one source", 1, sources.size());
        assertEquals("The source is " + SOURCE, SOURCE, sources.get(0));
    }

    public void testLast1MinuteJMXMemoryMetrics() {
        MetricData metricData = metricsDataService.findLastMetrics(getMemoryMetrics(), SOURCE, "-1m");
        assertNotNull("Metric Data is not null", metricData);

    }

    public void testLast1HourJMXMemoryMetrics() {
        MetricData metricData = metricsDataService.findLastMetrics(getMemoryMetrics(), SOURCE, "-1h");
        assertNotNull("Metric Data is not null", metricData);
    }

    public void testLast1DayJMXMemoryMetrics() {
        MetricData metricData = metricsDataService.findLastMetrics(getMemoryMetrics(), SOURCE, "-1d");
        assertNotNull("Metric Data is not null", metricData);
    }

    public void testLastJMXMemoryMetrics() {
        MetricData metricData = metricsDataService.findLastMetrics(getMemoryMetrics(), SOURCE,
                String.valueOf(START_TIME));
        assertEquals("There are two results", 2, metricData.getData().length);
    }

    public void testJMXMemoryMetrics() {
        MetricData metricData = metricsDataService.findMetricsByTimePeriod(getMemoryMetrics(), SOURCE, START_TIME,
                END_TIME);
        assertEquals("There are two results", 2, metricData.getData().length);
        assertEquals("There are nine names", 9, metricData.getMetadata().getNames().length);
        assertEquals("There are nine types", 9, metricData.getMetadata().getTypes().length);
        for (int i = 0; i < metricData.getData().length; i++) {
            assertEquals("There are nine values", 9, metricData.getData()[i].length);
        }
    }

    private MetricList getMemoryMetrics() {
        List<Metric> metrics = new ArrayList<Metric>();

        addMemoryMetrics(metrics, "heap", "Heap");
        addMemoryMetrics(metrics, "non-heap", "Non-Heap");

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));
        return metricList;
    }

    private void addMemoryMetrics(List<Metric> metrics, String type, String displayType) {
        metrics.add(new Metric(MetricType.GAUGE, String.format("jvm.memory.%s.init", type), String.format("%s Init",
                displayType), MetricAttribute.VALUE, MetricDataFormat.B));
        metrics.add(new Metric(MetricType.GAUGE, String.format("jvm.memory.%s.used", type), String.format("%s Used",
                displayType), MetricAttribute.VALUE, MetricDataFormat.B));
        metrics.add(new Metric(MetricType.GAUGE, String.format("jvm.memory.%s.committed", type), String.format(
                "%s Committed", displayType), MetricAttribute.VALUE, MetricDataFormat.B));
        metrics.add(new Metric(MetricType.GAUGE, String.format("jvm.memory.%s.max", type), String.format("%s Max",
                displayType), MetricAttribute.VALUE, MetricDataFormat.B));

    }

    public void testJMXCPULoadMetrics() {
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.cpu.load.process", "Process CPU Load", MetricAttribute.VALUE,
                MetricDataFormat.P));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.cpu.load.system", "System CPU Load", MetricAttribute.VALUE,
                MetricDataFormat.P));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);

        assertEquals("There are two results", 2, metricData.getData().length);
        assertEquals("There are three names", 3, metricData.getMetadata().getNames().length);
        assertEquals("There are three types", 3, metricData.getMetadata().getTypes().length);
        for (int i = 0; i < metricData.getData().length; i++) {
            assertEquals("There are three values", 3, metricData.getData()[i].length);
            for (int j = 1; j < metricData.getData()[i].length; j++) {
                BigDecimal value = metricData.getData()[i][j];
                assertTrue("Value is greater than or equal to zero", value.compareTo(BigDecimal.ZERO) >= 0);
                assertTrue("Value is less than or equal to 100%", value.compareTo(new BigDecimal("100")) <= 0);
            }
        }
    }

    public void testJMXLoadAverageMetrics() {
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.system.load.average", "System Load Average",
                MetricAttribute.VALUE, null));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);

        assertEquals("There are two results", 2, metricData.getData().length);
        assertEquals("There are two names", 2, metricData.getMetadata().getNames().length);
        assertEquals("There are two types", 2, metricData.getMetadata().getTypes().length);
        for (int i = 0; i < metricData.getData().length; i++) {
            assertEquals("There are two values", 2, metricData.getData()[i].length);
        }
    }

    public void testJMXFileDescriptorMetrics() {
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.file.descriptor.open.count", "Open File Descriptor Count",
                MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.file.descriptor.max.count", "Max File Descriptor Count",
                MetricAttribute.VALUE, null));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);
        assertEquals("There are two results", 2, metricData.getData().length);
        assertEquals("There are three names", 3, metricData.getMetadata().getNames().length);
        assertEquals("There are three types", 3, metricData.getMetadata().getTypes().length);
        for (int i = 0; i < metricData.getData().length; i++) {
            assertEquals("There are three values", 3, metricData.getData()[i].length);
        }
    }

    public void testJMXPhysicalMemoryMetrics() {
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.physical.memory.free.size", "Free Physical Memory Size",
                MetricAttribute.VALUE, MetricDataFormat.B));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.physical.memory.total.size", "Total Physical Memory Size",
                MetricAttribute.VALUE, MetricDataFormat.B));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.swap.space.free.size", "Free Swap Space Size",
                MetricAttribute.VALUE, MetricDataFormat.B));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.swap.space.total.size", "Total Swap Space Size",
                MetricAttribute.VALUE, MetricDataFormat.B));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.virtual.memory.committed.size",
                "Committed Virtual Memory Size", MetricAttribute.VALUE, MetricDataFormat.B));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);
        assertEquals("There are two results", 2, metricData.getData().length);
        assertEquals("There are six names", 6, metricData.getMetadata().getNames().length);
        assertEquals("There are six types", 6, metricData.getMetadata().getTypes().length);
        for (int i = 0; i < metricData.getData().length; i++) {
            assertEquals("There are six values", 6, metricData.getData()[i].length);
        }
    }

    public void testJMXThreadingMetrics() {
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.threads.count", "Live Threads", MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.threads.daemon.count", "Daemon Threads", MetricAttribute.VALUE,
                null));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);
        assertEquals("There are two results", 2, metricData.getData().length);
        assertEquals("There are three names", 3, metricData.getMetadata().getNames().length);
        assertEquals("There are three types", 3, metricData.getMetadata().getTypes().length);
        for (int i = 0; i < metricData.getData().length; i++) {
            assertEquals("There are three values", 3, metricData.getData()[i].length);
        }
    }

    public void testJMXClassLoadingMetrics() {
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading.loaded.current", "Current Classes Loaded",
                MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading.loaded.total", "Total Classes Loaded",
                MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading.unloaded.total", "Total Classes Unloaded",
                MetricAttribute.VALUE, null));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);

        assertEquals("There are two results", 2, metricData.getData().length);
        assertEquals("There are four names", 4, metricData.getMetadata().getNames().length);
        assertEquals("There are four types", 4, metricData.getMetadata().getTypes().length);
        for (int i = 0; i < metricData.getData().length; i++) {
            assertEquals("There are four values", 4, metricData.getData()[i].length);
        }
    }

}
