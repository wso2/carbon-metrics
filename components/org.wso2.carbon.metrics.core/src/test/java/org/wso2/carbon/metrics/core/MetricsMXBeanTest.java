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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.config.MetricsConfigBuilder;
import org.wso2.carbon.metrics.core.config.model.MetricsConfig;
import org.wso2.carbon.metrics.core.internal.Utils;
import org.wso2.carbon.metrics.core.jmx.MetricsMXBean;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;

import java.util.List;
import java.util.Map;
import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Test cases for {@link MetricsMXBean}
 */
public class MetricsMXBeanTest extends BaseReporterTest {

    private static final Logger logger = LoggerFactory.getLogger(MetricManagementServiceTest.class);

    private static final String MBEAN_NAME = "org.wso2.carbon:type=MetricsTest";

    private MetricsMXBean metricsMXBean;

    @BeforeClass
    private void createMXBeanProxy() {
        ObjectName n;
        try {
            n = new ObjectName(MBEAN_NAME);
            metricsMXBean = JMX.newMXBeanProxy(mBeanServer, n, MetricsMXBean.class);
        } catch (MalformedObjectNameException e) {
            Assert.fail(e.getMessage());
        }
    }

    @BeforeMethod
    private void setRootLevel() {
        if (logger.isInfoEnabled()) {
            logger.info("Resetting Root Level to {}", Level.INFO);
        }
        metricsMXBean.setRootLevel(Level.INFO.name());
    }

    @Test
    public void testReporterJMXOperations() throws ReporterBuildException {
        template.execute("DELETE FROM METRIC_METER;");
        // reload with custom jdbc config
        System.setProperty("metrics.conf", "src/test/resources/conf/metrics-jdbc.yml");
        System.setProperty("metrics.datasource.conf", "src/test/resources/conf/metrics-datasource.properties");
        MetricsConfig metricsConfig = MetricsConfigBuilder.build();
        metricManagementService.addReporter(metricsConfig.getReporting().getJdbc().iterator().next());
        // Test start/stop reporters
        metricsMXBean.startReporters();
        Assert.assertTrue(metricsMXBean.isReporterRunning("JDBC"));
        metricsMXBean.stopReporters();
        Assert.assertFalse(metricsMXBean.isReporterRunning("JDBC"));
        metricsMXBean.startReporter("JDBC");
        Assert.assertTrue(metricsMXBean.isReporterRunning("JDBC"));
        String meterName = MetricService.name(this.getClass(), "test-jmx-report-meter");
        Meter meter = metricService.meter(meterName, Level.INFO);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricsMXBean.report("JDBC");
        List<Map<String, Object>> meterResult =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ?", meterName);
        Assert.assertEquals(meterResult.size(), 1);
        Assert.assertEquals(meterResult.get(0).get("NAME"), meterName, "Meter should be available");
        Assert.assertEquals(meterResult.get(0).get("COUNT"), 1L, "Meter count should be one");

        meter.mark();
        metricsMXBean.report();
        List<Map<String, Object>> meterResult2 =
                template.queryForList("SELECT * FROM METRIC_METER WHERE NAME = ? ORDER BY TIMESTAMP DESC", meterName);
        Assert.assertEquals(meterResult2.size(), 2);
        Assert.assertEquals(meterResult2.get(0).get("NAME"), meterName, "Meter should be available");
        Assert.assertEquals(meterResult2.get(0).get("COUNT"), 2L, "Meter count should be two");
        Assert.assertEquals(meterResult2.get(1).get("COUNT"), 1L, "Meter count should be one");

        metricsMXBean.stopReporter("JDBC");
        Assert.assertFalse(metricsMXBean.isReporterRunning("JDBC"));
    }

    @Test
    public void testEnableDisable() {
        Assert.assertTrue(metricsMXBean.isEnabled(), "Metric Service should be enabled");
        Meter meter = metricService.meter(MetricService.name(this.getClass(), "test-enabled"), Level.INFO);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricsMXBean.disable();
        Assert.assertFalse(metricsMXBean.isEnabled(), "Metric Service should be disabled");

        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricsMXBean.enable();
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);
    }

    @Test
    public void testMetricSetLevel() {
        String name = MetricService.name(this.getClass(), "test-metric-level");
        Meter meter = metricService.meter(name, Level.INFO);
        Assert.assertNull(metricsMXBean.getMetricLevel(name), "There should be no configured level");

        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricsMXBean.setMetricLevel(name, Level.INFO.name());
        Assert.assertEquals(metricsMXBean.getMetricLevel(name), Level.INFO.name(), "Configured level should be INFO");
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);

        metricsMXBean.setMetricLevel(name, Level.OFF.name());
        Assert.assertEquals(metricsMXBean.getMetricLevel(name), Level.OFF.name(), "Configured level should be OFF");
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);
    }

    @Test
    public void testMetricServiceLevels() {
        Meter meter = metricService.meter(MetricService.name(this.getClass(), "test-levels"), Level.INFO);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);

        metricsMXBean.setRootLevel(Level.TRACE.name());
        Assert.assertEquals(metricsMXBean.getRootLevel(), Level.TRACE.name());
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);

        metricsMXBean.setRootLevel(Level.OFF.name());
        Assert.assertEquals(metricsMXBean.getRootLevel(), Level.OFF.name());
        meter.mark();
        Assert.assertEquals(meter.getCount(), 2);
    }

    @Test
    public void testMetricsCount() {
        Assert.assertTrue(metricsMXBean.getMetricsCount() > 0, "Metrics count should be greater than zero");
    }

    @Test
    public void testDefaultSource() {
        Assert.assertEquals(metricsMXBean.getDefaultSource(), Utils.getDefaultSource());
    }

}
