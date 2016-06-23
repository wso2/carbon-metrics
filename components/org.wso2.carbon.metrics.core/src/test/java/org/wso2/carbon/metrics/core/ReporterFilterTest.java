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
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Test Cases for Reporter Filters
 */
public class ReporterFilterTest {

    private static final Logger logger = LoggerFactory.getLogger(ReporterFilterTest.class);

    private static final String RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";

    private Metrics metrics;

    private MetricManagementService metricManagementService;

    private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    @BeforeSuite
    protected void init() {
        System.setProperty("metrics.conf", RESOURCES_DIR + File.separator + "conf" + File.separator
                + "metrics-filter.yml");
        System.setProperty("metrics.level.conf", RESOURCES_DIR + File.separator + "conf" + File.separator
                + "metrics.properties");
        // Initialize the Metrics
        metrics = new Metrics.Builder().build();
        metricManagementService = metrics.getMetricManagementService();
    }

    @Test
    public void testJMX() {
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX"));
        Assert.assertEquals(findObjects("org.wso2.carbon.metrics.filter.test").size(),
                metricManagementService.getEnabledMetricsCount());
    }

    @Test
    public void testJMX1() {
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX1"));
        Assert.assertEquals(findObjects("org.wso2.carbon.metrics.filter.test1").size(), 6);
    }

    @Test
    public void testJMX2() {
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX2"));
        Assert.assertEquals(findObjects("org.wso2.carbon.metrics.filter.test2").size(), 9);
    }

    @Test
    public void testJMX3() {
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX3"));
        Assert.assertEquals(findObjects("org.wso2.carbon.metrics.filter.test3").size(), 9);
    }

    @Test
    public void testJMX4() {
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX4"));
        Assert.assertEquals(findObjects("org.wso2.carbon.metrics.filter.test4").size(), 2);
    }

    @Test
    public void testJMX5() {
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX5"));
        Assert.assertEquals(findObjects("org.wso2.carbon.metrics.filter.test5").size(),
                metricManagementService.getEnabledMetricsCount() - 2);
    }

    @Test
    public void testJMX6() {
        Assert.assertTrue(metricManagementService.isReporterRunning("JMX6"));
        Assert.assertEquals(findObjects("org.wso2.carbon.metrics.filter.test6").size(), 2);
    }

    private Set<ObjectName> findObjects(String domain) {
        try {
            Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName(domain + ":name=*"), null);
            logger.info("Found {} objects for domain {}", objectNames.size(), domain);
            objectNames.forEach(objectName -> logger.info(objectName.getCanonicalName()));
            return objectNames;
        } catch (MalformedObjectNameException e) {
            Assert.fail(e.getMessage());
        }
        return Collections.emptySet();
    }

}
