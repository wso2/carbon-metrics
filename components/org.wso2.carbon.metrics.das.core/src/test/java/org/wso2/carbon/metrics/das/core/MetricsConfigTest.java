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
package org.wso2.carbon.metrics.das.core;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.config.MetricsConfigBuilder;
import org.wso2.carbon.metrics.das.core.config.model.DasConfig;
import org.wso2.carbon.metrics.das.core.config.model.DasReporterConfig;
import org.wso2.carbon.metrics.das.core.config.model.MetricsConfig;

/**
 * Test Cases for {@link MetricsConfig}
 */
public class MetricsConfigTest {

    private static MetricsConfig metricsConfig;

    @BeforeClass
    private void load() {
        metricsConfig = MetricsConfigBuilder.build(MetricsConfig.class, MetricsConfig::new);
    }

    @Test
    public void testDasReporterConfigLoad() {
        DasReporterConfig config = metricsConfig.getReporting().getDas().iterator().next();
        Assert.assertEquals(config.getName(), "DAS");
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getPollingPeriod(), 600L);
        Assert.assertEquals(config.getSource(), "Carbon-das");
    }

    @Test
    public void testDasConfigLoad() {
        DasConfig config = metricsConfig.getDas().iterator().next();
        Assert.assertEquals(config.getReceiverURL(), "tcp://localhost:51840");
        Assert.assertNull(config.getAuthURL());
        Assert.assertEquals(config.getType(), "thrift");
        Assert.assertEquals(config.getUsername(), "admin");
        Assert.assertEquals(config.getPassword(), "admin");
        Assert.assertEquals(config.getDataAgentConfigPath(), "data-agent-config.xml");

        DasReporterConfig dasReporterConfig = metricsConfig.getReporting().getDas().iterator().next();
        Assert.assertEquals(dasReporterConfig.getDas(), config);
    }
}
