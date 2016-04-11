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

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.wso2.carbon.metrics.core.impl.MetricService;
import org.wso2.carbon.metrics.core.utils.Utils;
import org.wso2.carbon.metrics.das.reporter.TestEventServer;

import java.io.File;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Base Class for all Metrics Core Test Cases
 */
public abstract class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    protected static MetricService metricService;

    protected static Random random = new Random();

    protected static DataSource dataSource;

    protected static JdbcTemplate template;

    protected static final String RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";

    protected static final String TEST_RESOURCES_DIR = "target" + File.separator + "test-resources";

    protected static final TestEventServer TEST_EVENT_SERVER = new TestEventServer(TEST_RESOURCES_DIR);

    @Parameters("server-port")
    @BeforeSuite
    protected static void init(String serverPort) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Initializing the data source and populating data");
        }
        // Setup datasource
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
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

        // Set setup system property to cover database creator logic
        System.setProperty("setup", "");

        System.setProperty("metrics.target", "target");
        System.setProperty("metrics.enabled", "true");
        System.setProperty("metrics.rootLevel", "INFO");
        System.setProperty("metrics.conf", RESOURCES_DIR + File.separator + "conf" + File.separator + "metrics.yml");
        System.setProperty("metrics.level.conf", RESOURCES_DIR + File.separator + "conf" + File.separator
                + "metrics.properties");
        System.setProperty("metrics.dataagent.conf", TEST_RESOURCES_DIR + File.separator + "data-agent-config.xml");

        if (logger.isInfoEnabled()) {
            logger.info("Creating the DAS Test Receiver");
        }
        TEST_EVENT_SERVER.start("localhost", Integer.parseInt(serverPort));

        if (logger.isInfoEnabled()) {
            logger.info("Creating the MetricService and registering the MX Bean");
        }
        metricService = MetricService.getInstance();

        // Register the MX Bean
        Utils.registerMXBean();
    }

    @AfterSuite
    protected static void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Unbinding jdbc/WSO2MetricsDB");
        }
        InitialContext ic = new InitialContext();
        ic.unbind("jdbc/WSO2MetricsDB");
        ic.unbind("jdbc");
        if (logger.isInfoEnabled()) {
            logger.info("Stopping reporters and unregistering the MX Bean");
        }
        // Disable to stop reporters
        metricService.disable();
        // Unregister the MX Bean
        Utils.unregisterMXBean();
        // Stop DAS Receiver Test Server
        TEST_EVENT_SERVER.stop();
    }

    @BeforeMethod
    protected static void resetRootLevel() {
        if (logger.isInfoEnabled()) {
            logger.info("Resetting Root Level to {}", Level.ALL);
        }
        metricService.setRootLevel(Level.ALL);
    }
}
