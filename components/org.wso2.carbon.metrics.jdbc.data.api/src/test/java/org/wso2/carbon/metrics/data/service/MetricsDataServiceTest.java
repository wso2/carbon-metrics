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

import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.data.common.Metric;
import org.wso2.carbon.metrics.data.common.MetricAttribute;
import org.wso2.carbon.metrics.data.common.MetricDataFormat;
import org.wso2.carbon.metrics.data.common.MetricList;
import org.wso2.carbon.metrics.data.common.MetricType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;


/**
 * Test Metrics Data Service
 */
public class MetricsDataServiceTest {

    private MetricsDataService metricsDataService;

    private static JdbcTemplate template;

    private static final String SOURCE = "carbon-server";

    private static final long START_TIME = 1428567356013L;

    private static final long END_TIME = 1428567416013L;

    @BeforeTest
    private void initialize() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
        template = new JdbcTemplate(dataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("dbscripts/h2.sql"));
        populator.populate(dataSource.getConnection());
        metricsDataService = new MetricsDataService(dataSource);
    }

    @Test
    public void testAllData() {
        List<Map<String, Object>> gaugeResult = template.queryForList("SELECT * FROM METRIC_GAUGE");
        Assert.assertEquals(gaugeResult.size(), 66, "There should be 66 results");
    }

    @Test
    public void testSpecificData() {
        String gaugeName = "jvm.memory.heap.init";
        List<Map<String, Object>> gaugeResult = template.queryForList("SELECT * FROM METRIC_GAUGE WHERE NAME = ?",
                gaugeName);
        Assert.assertEquals(gaugeResult.size(), 2, "There should be two results");
    }

    @Test
    public void testSources() {
        String[] sources = metricsDataService.getAllSources();
        Assert.assertEquals(sources.length, 1, "There should be one source");
        Assert.assertEquals(sources[0], SOURCE, "The source should be " + SOURCE);
    }

    @Test
    public void testHierarchy() {
        MetricHierarchyData hierarchyData1 = metricsDataService.getHierarchy(SOURCE, "");
        MetricHierarchyData hierarchyData2 = metricsDataService.getHierarchy(SOURCE, "jvm.memory");
        MetricHierarchyData hierarchyData3 = metricsDataService.getHierarchy(SOURCE, "jvm.memory.non-heap");
        Assert.assertEquals(hierarchyData1.getMetrics().length, 0, "There should be zero metrics");
        Assert.assertEquals(hierarchyData2.getMetrics().length, 0, "There should be zero metrics");
        Assert.assertTrue(hierarchyData3.getMetrics().length > 0, "There should be multiple metrics");
        Assert.assertTrue(hierarchyData1.getChildren().length > 0, "There should be multiple sub levels");
        Assert.assertTrue(hierarchyData2.getChildren().length > 0, "There should be multiple sub levels");
        Assert.assertEquals(hierarchyData3.getChildren().length, 0, "There should be no sub levels");
    }

    @Test
    public void testLast1MinuteJMXMemoryMetrics() {
        MetricData metricData = metricsDataService.findLastMetrics(getMemoryMetrics(), SOURCE, "-1m");
        Assert.assertNotNull(metricData, "Metric Data can not be null");
    }

    @Test
    public void testLast1HourJMXMemoryMetrics() {
        MetricData metricData = metricsDataService.findLastMetrics(getMemoryMetrics(), SOURCE, "-1h");
        Assert.assertNotNull(metricData, "Metric Data can not be null");
    }

    @Test
    public void testLast1DayJMXMemoryMetrics() {
        MetricData metricData = metricsDataService.findLastMetrics(getMemoryMetrics(), SOURCE, "-1d");
        Assert.assertNotNull(metricData, "Metric Data is can not be null");
    }

    @Test
    public void testLastJMXMemoryMetrics() {
        MetricData metricData = metricsDataService.findLastMetrics(getMemoryMetrics(), SOURCE,
                String.valueOf(START_TIME));
        Assert.assertEquals(metricData.getData().length, 2, "There results count should be 2");
    }

    @Test
    public void testJMXMemoryMetrics() {
        MetricData metricData = metricsDataService.findMetricsByTimePeriod(getMemoryMetrics(), SOURCE, START_TIME,
                END_TIME);
        Assert.assertEquals(metricData.getData().length, 2, "There should be two results");
        Assert.assertEquals(metricData.getMetadata().getNames().length, 9, "There should be nine names");
        Assert.assertEquals(metricData.getMetadata().getTypes().length, 9, "There should be nine types");
        for (int i = 0; i < metricData.getData().length; i++) {
            Assert.assertEquals(metricData.getData()[i].length, 9, "The results count should be 9");
            for (int j = 0; j < metricData.getData()[i].length; j++) {
                BigDecimal value = metricData.getData()[i][j];
                Assert.assertNotNull(value, "Value is available");
            }
        }
    }

    private MetricList getMemoryMetrics() {
        List<Metric> metrics = new ArrayList<>();

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

    @Test
    public void testJMXCPULoadMetrics() {
        List<Metric> metrics = new ArrayList<>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.cpu.load.process", "Process CPU Load", MetricAttribute.VALUE,
                MetricDataFormat.P));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.cpu.load.system", "System CPU Load", MetricAttribute.VALUE,
                MetricDataFormat.P));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);

        Assert.assertEquals(metricData.getData().length, 2, "There should be two results");
        Assert.assertEquals(metricData.getMetadata().getNames().length, 3, "There should be three names");
        Assert.assertEquals(metricData.getMetadata().getTypes().length, 3, "There should be three types");
        for (int i = 0; i < metricData.getData().length; i++) {
            Assert.assertEquals(metricData.getData()[i].length, 3, "There should be three values");
            for (int j = 1; j < metricData.getData()[i].length; j++) {
                BigDecimal value = metricData.getData()[i][j];
                Assert.assertNotNull(value, "Value can not be null");
                Assert.assertTrue(value.compareTo(BigDecimal.ZERO) >= 0,
                        "Value should be greater than or equal to zero");
                Assert.assertTrue(value.compareTo(new BigDecimal("100")) <= 0,
                        "Value should be less than or equal to 100");
            }
        }
    }

    @Test
    public void testJMXLoadAverageMetrics() {
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.system.load.average", "System Load Average",
                MetricAttribute.VALUE, null));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);

        Assert.assertEquals(metricData.getData().length, 2, "There should be two results");
        Assert.assertEquals(metricData.getMetadata().getNames().length, 2, "There should be two names");
        Assert.assertEquals(metricData.getMetadata().getTypes().length, 2, "There should be two types");
        for (int i = 0; i < metricData.getData().length; i++) {
            Assert.assertEquals(metricData.getData()[i].length, 2, "There should be two values");
            for (int j = 0; j < metricData.getData()[i].length; j++) {
                BigDecimal value = metricData.getData()[i][j];
                Assert.assertNotNull(value, "Value should not be null");
            }
        }
    }

    @Test
    public void testJMXFileDescriptorMetrics() {
        List<Metric> metrics = new ArrayList<>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.file.descriptor.open.count", "Open File Descriptor Count",
                MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.os.file.descriptor.max.count", "Max File Descriptor Count",
                MetricAttribute.VALUE, null));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);
        Assert.assertEquals(metricData.getData().length, 2, "There should be two results");
        Assert.assertEquals(metricData.getMetadata().getNames().length, 3, "There should be three names");
        Assert.assertEquals(metricData.getMetadata().getTypes().length, 3, "There should be three types");
        for (int i = 0; i < metricData.getData().length; i++) {
            Assert.assertEquals(metricData.getData()[i].length, 3, "There should be three values");
            for (int j = 0; j < metricData.getData()[i].length; j++) {
                BigDecimal value = metricData.getData()[i][j];
                Assert.assertNotNull(value, "Value should not be null");
            }
        }
    }

    @Test
    public void testJMXPhysicalMemoryMetrics() {
        List<Metric> metrics = new ArrayList<>();
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
        Assert.assertEquals(metricData.getData().length, 2, "There should be two results");
        Assert.assertEquals(metricData.getMetadata().getNames().length, 6, "There should be six names");
        Assert.assertEquals(metricData.getMetadata().getTypes().length, 6, "There should be six types");
        for (int i = 0; i < metricData.getData().length; i++) {
            Assert.assertEquals(metricData.getData()[i].length, 6, "There should be six values");
            for (int j = 0; j < metricData.getData()[i].length; j++) {
                BigDecimal value = metricData.getData()[i][j];
                Assert.assertNotNull(value, "Value should not be null");
            }
        }
    }

    @Test
    public void testJMXThreadingMetrics() {
        List<Metric> metrics = new ArrayList<>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.threads.count", "Live Threads", MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.threads.daemon.count", "Daemon Threads", MetricAttribute.VALUE,
                null));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);
        Assert.assertEquals(metricData.getData().length, 2, "There should be two results");
        Assert.assertEquals(metricData.getMetadata().getNames().length, 3, "There should be three names");
        Assert.assertEquals(metricData.getMetadata().getTypes().length, 3, "There should be three types");
        for (int i = 0; i < metricData.getData().length; i++) {
            Assert.assertEquals(metricData.getData()[i].length, 3, "There should be three values");
            for (int j = 0; j < metricData.getData()[i].length; j++) {
                BigDecimal value = metricData.getData()[i][j];
                Assert.assertNotNull(value, "Value should not be null");
            }
        }
    }

    @Test
    public void testJMXClassLoadingMetrics() {
        List<Metric> metrics = new ArrayList<>();
        metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading.loaded.current", "Current Classes Loaded",
                MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading.loaded.total", "Total Classes Loaded",
                MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading.unloaded.total", "Total Classes Unloaded",
                MetricAttribute.VALUE, null));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);

        Assert.assertEquals(metricData.getData().length, 2, "There should be two results");
        Assert.assertEquals(metricData.getMetadata().getNames().length, 4, "There should be four names");
        Assert.assertEquals(metricData.getMetadata().getTypes().length, 4, "There should be four types");
        for (int i = 0; i < metricData.getData().length; i++) {
            Assert.assertEquals(metricData.getData()[i].length, 4, "There should be four values");
            for (int j = 0; j < metricData.getData()[i].length; j++) {
                BigDecimal value = metricData.getData()[i][j];
                Assert.assertNotNull(value, "Value should not be null");
            }
        }
    }

    @Test
    public void testDatabaseReadWriteAndJMXMetrics() {
        // This test will check the metric find queries
        List<Metric> metrics = new ArrayList<>();
        // Read Metrics
        metrics.add(new Metric(MetricType.TIMER, "database.read", "Minimum Database Read Time", MetricAttribute.MIN,
                null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "Mean Database Read Time",
                MetricAttribute.MEAN, null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "Maximum Database Read Time", MetricAttribute.MAX,
                null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "Standard Deviation of Database Read Time",
                MetricAttribute.STDDEV, null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "50th Percentile of Database Read Time",
                MetricAttribute.P50, null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "75th Percentile of Database Read Time",
                MetricAttribute.P75, null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "95th Percentile of Database Read Time",
                MetricAttribute.P95, null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "98th Percentile of Database Read Time",
                MetricAttribute.P98, null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "99th Percentile of Database Read Time",
                MetricAttribute.P99, null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "99.9th Percentile of Database Read Time",
                MetricAttribute.P999, null));

        metrics.add(new Metric(MetricType.TIMER, "database.read", "Database Read Mean Rate", MetricAttribute.MEAN_RATE,
                null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "Database Read Last Minute Rate",
                MetricAttribute.M1_RATE, null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "Database Read Last 5 Minutes Rate",
                MetricAttribute.M5_RATE, null));
        metrics.add(new Metric(MetricType.TIMER, "database.read", "Database Read Last 15 Minutes Rate",
                MetricAttribute.M15_RATE, null));

        // JMX Thread
        metrics.add(new Metric(MetricType.GAUGE, "jvm.threads.count", "Live Threads", MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.threads.daemon.count", "Daemon Threads", MetricAttribute.VALUE,
                null));

        // Write Metrics
        metrics.add(new Metric(MetricType.TIMER, "database.write", "Minimum Database Write Time", MetricAttribute.MIN,
                null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "Mean Database Write Time", MetricAttribute.MEAN,
                null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "Maximum Database Write Time", MetricAttribute.MAX,
                null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "Standard Deviation of Database Write Time",
                MetricAttribute.STDDEV, null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "50th Percentile of Database Write Time",
                MetricAttribute.P50, null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "75th Percentile of Database Write Time",
                MetricAttribute.P75, null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "95th Percentile of Database Write Time",
                MetricAttribute.P95, null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "98th Percentile of Database Write Time",
                MetricAttribute.P98, null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "99th Percentile of Database Write Time",
                MetricAttribute.P99, null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "99.9th Percentile of Database Write Time",
                MetricAttribute.P999, null));

        metrics.add(new Metric(MetricType.TIMER, "database.write", "Database Write Mean Rate",
                MetricAttribute.MEAN_RATE, null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "Database Write Last Minute Rate",
                MetricAttribute.M1_RATE, null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "Database Write Last 5 Minutes Rate",
                MetricAttribute.M5_RATE, null));
        metrics.add(new Metric(MetricType.TIMER, "database.write", "Database Write Last 15 Minutes Rate",
                MetricAttribute.M15_RATE, null));

        // JMX Class Loading
        metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading.loaded.current", "Current Classes Loaded",
                MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading.loaded.total", "Total Classes Loaded",
                MetricAttribute.VALUE, null));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading.unloaded.total", "Total Classes Unloaded",
                MetricAttribute.VALUE, null));

        // Duplicate Metric
        metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading.unloaded.total", "Total Classes Unloaded",
                MetricAttribute.VALUE, null));

        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));

        MetricData metricData = metricsDataService.findMetricsByTimePeriod(metricList, SOURCE, START_TIME, END_TIME);

        Assert.assertEquals(metricData.getData().length, 2, "There should be two results");
        Assert.assertEquals(metricData.getMetadata().getNames().length, 34, "There should be 34 names");
        Assert.assertEquals(metricData.getMetadata().getTypes().length, 34, "There should be 34 types");
        for (int i = 0; i < metricData.getData().length; i++) {
            Assert.assertEquals(metricData.getData()[i].length, 34, "There should be 34 values");
            for (int j = 0; j < metricData.getData()[i].length; j++) {
                BigDecimal value = metricData.getData()[i][j];
                Assert.assertNotNull(value, "Value should not be null");
            }
        }
    }
}
