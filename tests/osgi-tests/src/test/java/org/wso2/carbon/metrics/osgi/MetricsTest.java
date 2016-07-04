/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.metrics.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.metrics.core.Counter;
import org.wso2.carbon.metrics.core.Histogram;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.carbon.metrics.core.jmx.MetricsMXBean;
import org.wso2.carbon.metrics.sample.service.RandomNumberService;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class MetricsTest {

    private static final String MBEAN_NAME = "org.wso2.carbon:type=Metrics";

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private MetricService metricService;

    @Inject
    private MetricManagementService metricManagementService;

    @Inject
    private RandomNumberService randomNumberService;

    @Configuration
    public Option[] createConfiguration() {
        List<Option> optionList = new ArrayList<>();
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.jdbc.reporter").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.core").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.jdbc.core").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.das.core").versionAsInProject());
        optionList.add(mavenBundle().groupId("io.dropwizard.metrics")
                .artifactId("metrics-core").versionAsInProject());
        optionList.add(mavenBundle().groupId("io.dropwizard.metrics")
                .artifactId("metrics-jvm").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.jndi")
                .artifactId("org.wso2.carbon.jndi").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.datasources")
                .artifactId("org.wso2.carbon.datasource.core").versionAsInProject());
        optionList.add(mavenBundle().groupId("commons-io.wso2")
                .artifactId("commons-io").versionAsInProject());
        optionList.add(mavenBundle().groupId("com.zaxxer")
                .artifactId("HikariCP").versionAsInProject());
        optionList.add(mavenBundle().groupId("com.h2database")
                .artifactId("h2").versionAsInProject());

        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.das.reporter").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.analytics-common")
                .artifactId("org.wso2.carbon.databridge.agent").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.analytics-common")
                .artifactId("org.wso2.carbon.databridge.commons").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.orbit.com.lmax")
                .artifactId("disruptor").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.analytics-common")
                .artifactId("org.wso2.carbon.databridge.commons.thrift").versionAsInProject());
        optionList.add(mavenBundle().groupId("libthrift.wso2")
                .artifactId("libthrift").versionAsInProject());
        optionList.add(mavenBundle().groupId("commons-pool.wso2")
                .artifactId("commons-pool").versionAsInProject());

        // Sample bundles
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.sample.service").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.sample.consumer").versionAsInProject());

        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path carbonHome = Paths.get(currentDir, "target", "carbon-home");

        CarbonSysPropConfiguration sysPropConfiguration = new CarbonSysPropConfiguration();
        sysPropConfiguration.setCarbonHome(carbonHome.toString());
        sysPropConfiguration.setServerKey("carbon-metrics");
        sysPropConfiguration.setServerName("WSO2 Carbon Metrics Server");
        sysPropConfiguration.setServerVersion("1.0.0");

        optionList = OSGiTestConfigurationUtils.getConfiguration(optionList, sysPropConfiguration);

        return optionList.toArray(new Option[optionList.size()]);
    }

    private Bundle getBundle(String name) {
        Bundle bundle = null;
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getSymbolicName().equals(name)) {
                bundle = b;
                break;
            }
        }
        Assert.assertNotNull(bundle, "Bundle should be available. Name: " + name);
        return bundle;
    }

    @Test
    public void testMetricsCoreBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.metrics.core");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test
    public void testMetricsJdbcReporterBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.metrics.jdbc.reporter");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test
    public void testMetricsJdbcCoreBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.metrics.jdbc.core");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test
    public void testMetricsDasReporterBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.metrics.das.reporter");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test
    public void testMetricsDasCoreBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.metrics.das.core");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test
    public void testMetricsSampleServiceBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.metrics.sample.service");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test
    public void testMetricsSampleConsumerBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.metrics.sample.consumer");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test
    public void testCounter() {
        Counter counter = metricService.counter("org.wso2.carbon.metrics.osgi.test.counter", Level.INFO);
        counter.inc();
        Assert.assertEquals(counter.getCount(), 1);
    }

    @Test
    public void testMeter() {
        Meter meter = metricService.meter("org.wso2.carbon.metrics.osgi.test.meter", Level.INFO);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);
    }

    @Test
    public void testHistogram() {
        Histogram histogram = metricService.histogram("org.wso2.carbon.metrics.osgi.test.histogram", Level.INFO);
        histogram.update(1);
        Assert.assertEquals(histogram.getCount(), 1);
    }

    @Test
    public void testTimer() {
        Timer timer = metricService.timer("org.wso2.carbon.metrics.osgi.test.timer", Level.INFO);
        timer.update(1, TimeUnit.SECONDS);
        Assert.assertEquals(timer.getCount(), 1);
    }

    @Test
    public void testMBean() {
        MetricsMXBean metricsMXBean = null;
        try {
            ObjectName n = new ObjectName(MBEAN_NAME);
            metricsMXBean = JMX.newMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), n, MetricsMXBean.class);
        } catch (MalformedObjectNameException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertNotNull(metricsMXBean);
        Assert.assertTrue(metricsMXBean.isEnabled());
        // Check whether the reporters are started at the startup
        Assert.assertTrue(metricsMXBean.isReporterRunning("JMX"));
        Assert.assertTrue(metricsMXBean.isReporterRunning("JDBC"));
        Assert.assertTrue(metricsMXBean.getMetricsCount() > 0);
        Assert.assertEquals(metricsMXBean.getRootLevel(), Level.INFO.name());
        Assert.assertEquals(metricsMXBean.getDefaultSource(), "carbon-metrics");
    }

    @Test(dependsOnMethods = "testMBean")
    public void testEnableDisable() {
        // This method depends on the "testMBean" as the reporters will start if we disable and enable the Metrics
        Assert.assertTrue(metricManagementService.isEnabled(), "Metric Service should be enabled");
        Counter counter = metricService.counter(MetricService.name(this.getClass(), "test-enabled"), Level.INFO);
        counter.inc(10);
        Assert.assertEquals(counter.getCount(), 10);

        metricManagementService.disable();
        Assert.assertFalse(metricManagementService.isEnabled(), "Metric Service should be disabled");

        counter.inc(10);
        Assert.assertEquals(counter.getCount(), 10);

        metricManagementService.enable();
        counter.inc(90);
        Assert.assertEquals(counter.getCount(), 100);
    }

    @Test
    public void testMetricSetLevel() {
        String name = MetricService.name(this.getClass(), "test-metric-level");
        Counter counter = metricService.counter(name, Level.INFO);
        Assert.assertNull(metricManagementService.getMetricLevel(name), "There should be no configured level");

        counter.inc(10);
        Assert.assertEquals(counter.getCount(), 10);

        metricManagementService.setMetricLevel(name, Level.INFO);
        Assert.assertEquals(metricManagementService.getMetricLevel(name), Level.INFO,
                "Configured level should be INFO");
        counter.inc(10);
        Assert.assertEquals(counter.getCount(), 20);

        metricManagementService.setMetricLevel(name, Level.OFF);
        Assert.assertEquals(metricManagementService.getMetricLevel(name), Level.OFF, "Configured level should be OFF");
        counter.inc(10);
        Assert.assertEquals(counter.getCount(), 20);
    }

    @Test
    public void testMetricServiceLevels() {
        Counter counter = metricService.counter(MetricService.name(this.getClass(), "test-levels"), Level.INFO);
        counter.inc(10);
        Assert.assertEquals(counter.getCount(), 10);

        metricManagementService.setRootLevel(Level.TRACE);
        Assert.assertEquals(metricManagementService.getRootLevel(), Level.TRACE);
        counter.inc(10);
        Assert.assertEquals(counter.getCount(), 20);

        metricManagementService.setRootLevel(Level.OFF);
        Assert.assertEquals(metricManagementService.getRootLevel(), Level.OFF);
        counter.inc(10);
        Assert.assertEquals(counter.getCount(), 20);

        //  Set Root Level back to INFO, otherwise other tests will fail
        metricManagementService.setRootLevel(Level.INFO);
        Assert.assertEquals(metricManagementService.getRootLevel(), Level.INFO);
        counter.inc(10);
        Assert.assertEquals(counter.getCount(), 30);
    }

    @Test
    public void testSampleService() {
        Assert.assertTrue(randomNumberService.getRandomNumbers().length > 0);
    }

}
