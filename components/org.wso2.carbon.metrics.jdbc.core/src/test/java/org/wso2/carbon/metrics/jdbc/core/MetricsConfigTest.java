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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.config.MetricsConfigBuilder;
import org.wso2.carbon.metrics.jdbc.core.config.model.DataSourceConfig;
import org.wso2.carbon.metrics.jdbc.core.config.model.JdbcReporterConfig;
import org.wso2.carbon.metrics.jdbc.core.config.model.MetricsConfig;

import java.io.File;

/**
 * Test Cases for {@link MetricsConfig}
 */
public class MetricsConfigTest {

    private static MetricsConfig metricsConfig;

    protected static final String RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";

    @BeforeClass
    private void load() {
        metricsConfig = MetricsConfigBuilder.build(MetricsConfig.class, MetricsConfig::new);
    }

    @Test
    public void testJdbcReporterConfigLoad() {
        JdbcReporterConfig config = metricsConfig.getReporting().getJdbc().iterator().next();
        Assert.assertEquals(config.getName(), "JDBC");
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getPollingPeriod(), 600L);
        Assert.assertEquals(config.getSource(), "Carbon-jdbc");
    }

    @Test
    public void testDataSourceConfigLoad() {
        DataSourceConfig config = metricsConfig.getDataSource().iterator().next();
        Assert.assertEquals(config.isLookupDataSource(), true);
        Assert.assertEquals(config.getDataSourceName(), "jdbc/WSO2MetricsDB");
        Assert.assertEquals(config.getScheduledCleanup().isEnabled(), true);
        Assert.assertEquals(config.getScheduledCleanup().getDaysToKeep(), 2);
        Assert.assertEquals(config.getScheduledCleanup().getScheduledCleanupPeriod(), 10000L);

        JdbcReporterConfig jdbcReporterConfig = metricsConfig.getReporting().getJdbc().iterator().next();
        Assert.assertEquals(jdbcReporterConfig.getDataSource(), config);
    }

}
