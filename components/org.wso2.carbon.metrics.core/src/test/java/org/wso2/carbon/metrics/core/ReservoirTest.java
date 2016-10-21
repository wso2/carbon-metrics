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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.impl.reservoir.ReservoirType;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Testing different reservoir implementations
 */
public class ReservoirTest {

    private static final Logger logger = LoggerFactory.getLogger(ReservoirTest.class);

    private static final String RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";

    private Metrics metrics;

    private MetricService metricService;

    private MetricManagementService metricManagementService;

    private ReservoirType reservoirType;

    @Parameters("metrics-conf")
    @BeforeClass()
    protected void init(String file) {
        Pattern pattern = Pattern.compile("metrics-([a-z\\-]*)\\d?\\.yml");
        Matcher matcher = pattern.matcher(file);
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(ReservoirType.valueOf(matcher.group(1).toUpperCase().replaceAll("-", "_")));
        String configFile = RESOURCES_DIR + File.separator + "reservoirs" + File.separator + file;
        Assert.assertTrue(new File(configFile).exists());
        System.setProperty("metrics.conf", configFile);
        if (logger.isInfoEnabled()) {
            logger.info("Creating Metrics with Configuration File: {}", configFile);
        }
        metrics = new Metrics();
        metrics.activate();
        metricService = metrics.getMetricService();
        metricManagementService = metrics.getMetricManagementService();
    }

    @AfterClass
    protected void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Deactivating Metrics");
        }
        metrics.deactivate();
    }

    @Test
    public void testHistogram() {
        Histogram histogram = metricService.histogram(MetricService.name(this.getClass(), "histogram"), Level.INFO);
        IntStream.rangeClosed(1, 100).forEach(histogram::update);
        Assert.assertEquals(histogram.getCount(), 100);
        metricManagementService.report();
    }

    @Test
    public void testTimer() {
        Timer timer = metricService.timer(MetricService.name(this.getClass(), "timer"), Level.INFO);
        IntStream.rangeClosed(1, 100).forEach(i -> timer.update(i, TimeUnit.MILLISECONDS));
        Assert.assertEquals(timer.getCount(), 100);
        metricManagementService.report();
    }

}
