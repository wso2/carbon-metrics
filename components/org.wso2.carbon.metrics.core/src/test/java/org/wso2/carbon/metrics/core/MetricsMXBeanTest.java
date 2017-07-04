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
import org.wso2.carbon.metrics.core.config.model.CsvReporterConfig;
import org.wso2.carbon.metrics.core.jmx.MetricsMXBean;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.utils.Utils;

import java.io.File;
import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Test cases for {@link MetricsMXBean}.
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
        Assert.assertTrue(metricsMXBean.getMetricsCount() > 0);
        Assert.assertTrue(metricsMXBean.getEnabledMetricsCount() > 0);
        Assert.assertTrue(metricsMXBean.getEnabledMetricsCount() <= metricsMXBean.getMetricsCount());
        Assert.assertTrue(metricsMXBean.getMetricCollectionsCount() >= 0);
    }

    @Test
    public void testDefaultSource() {
        Assert.assertEquals(metricsMXBean.getDefaultSource(), Utils.getDefaultSource());
    }

    @Test
    public void testReporterJMXOperations() throws ReporterBuildException {
        CsvReporterConfig csvReporterConfig = new CsvReporterConfig();
        csvReporterConfig.setName("CSV");
        csvReporterConfig.setEnabled(true);
        csvReporterConfig.setLocation("target/metrics");
        csvReporterConfig.setPollingPeriod(600);
        metricManagementService.addReporter(csvReporterConfig);

        // Test start/stop reporters
        metricsMXBean.startReporters();
        Assert.assertTrue(metricsMXBean.isReporterRunning("CSV"));
        metricsMXBean.stopReporters();
        Assert.assertFalse(metricsMXBean.isReporterRunning("CSV"));
        metricsMXBean.startReporter("CSV");
        Assert.assertTrue(metricsMXBean.isReporterRunning("CSV"));

        String meterName1 = MetricService.name(this.getClass(), "test-jmx-report-meter1");
        Meter meter1 = metricService.meter(meterName1, Level.INFO);
        meter1.mark();
        Assert.assertEquals(meter1.getCount(), 1);

        metricsMXBean.report("CSV");
        Assert.assertTrue(new File("target/metrics", meterName1 + ".csv").exists(), "Meter CSV file should be created");

        String meterName2 = MetricService.name(this.getClass(), "test-jmx-report-meter2");
        Meter meter2 = metricService.meter(meterName2, Level.INFO);
        meter2.mark();
        Assert.assertEquals(meter2.getCount(), 1);

        metricsMXBean.report();
        Assert.assertTrue(new File("target/metrics", meterName2 + ".csv").exists(), "Meter CSV file should be created");

        metricsMXBean.stopReporter("CSV");
        Assert.assertFalse(metricsMXBean.isReporterRunning("CSV"));
    }

}
