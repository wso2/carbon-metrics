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

import java.util.Random;

import org.wso2.carbon.metrics.common.MetricsConfiguration;
import org.wso2.carbon.metrics.manager.Histogram;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

import junit.framework.TestCase;

/**
 * Test Cases for {@link Histogram}
 */
public class HistogramTest extends TestCase {

    private MetricService metricService;

    private Random randomGenerator = new Random();

    protected void setUp() throws Exception {
        super.setUp();
        MetricsConfiguration configuration = Utils.getConfiguration();
        MetricsLevelConfiguration levelConfiguration = Utils.getLevelConfiguration();
        metricService = new MetricServiceImpl.Builder().configure(configuration).build(levelConfiguration);
        ServiceReferenceHolder.getInstance().setMetricService(metricService);
    }

    public void testInitialCount() {
        Histogram histogram =
                MetricManager.histogram(MetricManager.name(this.getClass(), "test-initial-count"), Level.INFO);
        assertEquals("Initial count should be zero", 0, histogram.getCount());
    }

    public void testParentCount() {
        Histogram main = MetricManager.histogram("org.wso2.main.test-histogram", Level.INFO);
        Histogram sub = MetricManager.histogram("org.wso2.main[+].sub.test-histogram", Level.INFO, Level.INFO);
        sub.update(randomGenerator.nextInt());
        main.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, sub.getCount());
        assertEquals("Count should be two", 2, main.getCount());
    }

    public void testSameMetric() {
        Histogram histogram =
                MetricManager.histogram(MetricManager.name(this.getClass(), "test-same-histogram"), Level.INFO);
        histogram.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, histogram.getCount());

        Histogram histogram2 =
                MetricManager.histogram(MetricManager.name(this.getClass(), "test-same-histogram"), Level.INFO);
        assertEquals("Count should be one", 1, histogram2.getCount());
    }

    public void testSameMetricWithParent() {
        Histogram main = MetricManager.histogram("org.wso2.main.test-histogram", Level.INFO);
        Histogram sub = MetricManager.histogram("org.wso2.main[+].sub.test-histogram", Level.INFO, Level.INFO);

        Histogram main2 = MetricManager.histogram("org.wso2.main.test-histogram", Level.INFO);
        Histogram sub2 = MetricManager.histogram("org.wso2.main[+].sub.test-histogram", Level.INFO, Level.INFO);

        sub.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, sub.getCount());
        assertEquals("Count should be one", 1, sub2.getCount());
        assertEquals("Count should be one", 1, main.getCount());
        assertEquals("Count should be one", 1, main2.getCount());

        main.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, sub.getCount());
        assertEquals("Count should be one", 1, sub2.getCount());
        assertEquals("Count should be two", 2, main.getCount());
        assertEquals("Count should be two", 2, main2.getCount());
    }

    public void testMetricWithNonExistingParents() {
        Histogram sub2 = MetricManager.histogram("org.wso2.main[+].sub1[+].sub2.test-histogram", Level.INFO, Level.INFO,
                Level.INFO);
        Histogram sub1 = MetricManager.histogram("org.wso2.main[+].sub1.test-histogram", Level.INFO, Level.INFO);
        Histogram main = MetricManager.histogram("org.wso2.main.test-histogram", Level.INFO);

        sub2.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, sub2.getCount());
        assertEquals("Count should be one", 1, sub1.getCount());
        assertEquals("Count should be one", 1, main.getCount());

        sub1.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, sub2.getCount());
        assertEquals("Count should be two", 2, sub1.getCount());
        assertEquals("Count should be two", 2, main.getCount());

        main.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, sub2.getCount());
        assertEquals("Count should be two", 2, sub1.getCount());
        assertEquals("Count should be three", 3, main.getCount());
    }

    public void testUpdateInt() {
        Histogram histogram =
                MetricManager.histogram(MetricManager.name(this.getClass(), "test-histogram-update-int"), Level.INFO);
        histogram.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, histogram.getCount());

        metricService.setRootLevel(Level.OFF);
        histogram.update(randomGenerator.nextInt());
        assertEquals("Count should be one", 1, histogram.getCount());
    }

    public void testUpdateLong() {
        Histogram histogram =
                MetricManager.histogram(MetricManager.name(this.getClass(), "test-histogram-update-long"), Level.INFO);

        histogram.update(randomGenerator.nextLong());
        assertEquals("Count should be one", 1, histogram.getCount());

        metricService.setRootLevel(Level.OFF);
        histogram.update(randomGenerator.nextLong());
        assertEquals("Count should be one", 1, histogram.getCount());
    }

}
