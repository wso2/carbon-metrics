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
package org.wso2.carbon.metrics.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.Gauge;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Metrics;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.metrics.jdbc.core.config.model.DataSourceConfig;
import org.wso2.carbon.metrics.jdbc.core.config.model.JdbcReporterConfig;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Test Cases for JDBC Reporter
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
        String name = "JDBC-DISABLED";
        JdbcReporterConfig jdbcReporterConfig = new JdbcReporterConfig();
        jdbcReporterConfig.setEnabled(false);
        jdbcReporterConfig.setName(name);
        metricManagementService.addReporter(jdbcReporterConfig);

        metricManagementService.stopReporter(name);
    }

    @Test
    public void testJDBCReporter() {
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
    }

    @Test
    public void testJDBCReporterCachedGauge() {
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
    }

    @Test
    public void testJDBCReporterRestart() {
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
    }

    @Test
    public void testJDBCReporterCustomDatasource() {
        // reload with custom jdbc config
        System.setProperty("metrics.conf", RESOURCES_DIR + File.separator + "metrics-jdbc.yml");
        System.setProperty("metrics.datasource.conf", RESOURCES_DIR + File.separator + "datasource.properties");
        Metrics metrics = new Metrics();
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

        meter.mark();
        metricManagementService.report();
        List<Map<String, Object>> meterResult2 =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ? ORDER BY TIMESTAMP DESC", meterName);
        Assert.assertEquals(meterResult2.size(), 2);
        Assert.assertEquals(meterResult2.get(0).get("NAME"), meterName, "Meter should be available");
        Assert.assertEquals(meterResult2.get(0).get("COUNT"), 2L, "Meter count should be two");
        Assert.assertEquals(meterResult2.get(1).get("COUNT"), 1L, "Meter count should be one");

        metricManagementService.stopReporter("JDBC");
        Assert.assertFalse(metricManagementService.isReporterRunning("JDBC"));
        metrics.deactivate();
    }

}
