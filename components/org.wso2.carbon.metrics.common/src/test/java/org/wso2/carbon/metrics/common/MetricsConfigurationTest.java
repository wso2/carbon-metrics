/*
 * Copyright 2014-2015 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.common;

import junit.framework.TestCase;

/**
 * Test Cases for {@link MetricsConfiguration}
 */
public class MetricsConfigurationTest extends TestCase {

    private static final String ENABLED = "Enabled";
    private static final String CSV_REPORTING_LOCATION = "Reporting.CSV.Location";
    private static MetricsConfiguration configuration;

    protected void setUp() throws Exception {
        System.setProperty("carbon.home", "/wso2/carbon");
        configuration = Utils.getConfiguration();
    }

    public void testConfigLoad() {
        String enabled = configuration.getProperty(ENABLED);
        assertEquals("Enabled should be true", "true", enabled);

        String csvLocation = configuration.getProperty(CSV_REPORTING_LOCATION);
        assertEquals("/wso2/carbon/repository/logs/metrics/", csvLocation);
    }

    public void testSystemPropertiesReplacements() {
        System.setProperty("user.home", "/home/test");
        assertEquals("/home/test/file", MetricsXMLConfiguration.replaceSystemProperties("${user.home}/file"));
        assertEquals("/home/test/file/wso2/carbon",
                MetricsXMLConfiguration.replaceSystemProperties("${user.home}/file${carbon.home}"));
    }
}
