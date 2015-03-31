/*
 * Copyright 2015 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.data.service;

import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Test Metrics Data Service
 */
public class MetricsDataServiceTest extends TestCase {

    private MetricsDataService metricsDataService;

    private static JdbcTemplate template;

    public static Test suite() {
        return new TestSetup(new TestSuite(MetricsDataServiceTest.class)) {

            protected void setUp() throws Exception {
                DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
                template = new JdbcTemplate(dataSource);
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("dbscripts/h2.sql"));
                populator.populate(dataSource.getConnection());

                // Create initial context
                System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
                System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
                InitialContext ic = new InitialContext();
                ic.createSubcontext("jdbc");
                ic.bind("jdbc/WSO2MetricsDB", dataSource);
            }

            protected void tearDown() throws Exception {
                InitialContext ic = new InitialContext();
                ic.unbind("jdbc/WSO2MetricsDB");
                ic.unbind("jdbc");
            }
        };
    }

    protected void setUp() throws Exception {
        super.setUp();

        metricsDataService = new MetricsDataService();
        metricsDataService.init(Utils.getConfiguration());
    }

    public void testAllData() {
        List<Map<String, Object>> gaugeResult = template.queryForList("SELECT * FROM METRIC_GAUGE");
        assertEquals("There are 56 results", 56, gaugeResult.size());
    }

    public void testSpecificData() {
        String gaugeName = "jvm.memory.heap.init";
        List<Map<String, Object>> gaugeResult = template.queryForList("SELECT * FROM METRIC_GAUGE WHERE NAME = ?",
                gaugeName);
        assertEquals("There are two results", 2, gaugeResult.size());
    }

    public void testSearchJVMMetrics() {
        MetricData metricData = metricsDataService.searchJMXMemory(1427714860L, 1427714920L);
        assertEquals("There are two results", 2, metricData.getData().length);
        for (int i = 0; i < metricData.getData().length; i++) {
            assertEquals("There are nine values", 9, metricData.getData()[i].length);
        }
    }

}
