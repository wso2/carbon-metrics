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
import org.wso2.carbon.metrics.core.MetricManager;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class MetricsTest {
    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        List<Option> optionList = new ArrayList<>();
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.jdbc.reporter").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.core").versionAsInProject());
        optionList.add(mavenBundle().groupId("io.dropwizard.metrics")
                .artifactId("metrics-core").versionAsInProject());
        optionList.add(mavenBundle().groupId("io.dropwizard.metrics")
                .artifactId("metrics-jvm").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.datasources")
                .artifactId("org.wso2.carbon.datasource.core").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.das.reporter").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.analytics-common")
                .artifactId("org.wso2.carbon.databridge.agent").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.analytics-common")
                .artifactId("org.wso2.carbon.databridge.commons").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.orbit.com.lmax")
                .artifactId("disruptor").versionAsInProject());
        optionList.add(mavenBundle().groupId("libthrift.wso2")
                .artifactId("libthrift").versionAsInProject());


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

    @Test(dependsOnMethods = "testMetricsCoreBundle")
    public void testMetricsJdbcReporterBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.metrics.jdbc.reporter");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test(dependsOnMethods = "testMetricsCoreBundle")
    public void testMetricsDasReporterBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.metrics.das.reporter");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test(dependsOnMethods = "testMetricsCoreBundle")
    public void testCounter() {
        Counter counter = MetricManager.counter("org.wso2.carbon.metrics.osgi.test.counter", Level.INFO);
        counter.inc();
        Assert.assertEquals(counter.getCount(), 1);
    }

    @Test(dependsOnMethods = "testMetricsCoreBundle")
    public void testMeter() {
        Meter meter = MetricManager.meter("org.wso2.carbon.metrics.osgi.test.meter", Level.INFO);
        meter.mark();
        Assert.assertEquals(meter.getCount(), 1);
    }

    @Test(dependsOnMethods = "testMetricsCoreBundle")
    public void testHistogram() {
        Histogram histogram = MetricManager.histogram("org.wso2.carbon.metrics.osgi.test.histogram", Level.INFO);
        histogram.update(1);
        Assert.assertEquals(histogram.getCount(), 1);
    }

    @Test(dependsOnMethods = "testMetricsCoreBundle")
    public void testTimer() {
        Timer timer = MetricManager.timer("org.wso2.carbon.metrics.osgi.test.timer", Level.INFO);
        timer.update(1, TimeUnit.SECONDS);
        Assert.assertEquals(timer.getCount(), 1);
    }
}
