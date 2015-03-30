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

import java.net.URL;

import org.wso2.carbon.metrics.common.MetricsConfigException;
import org.wso2.carbon.metrics.common.MetricsConfiguration;

/**
 * Utility class for Tests
 */
public class Utils {

    public static MetricsConfiguration getConfiguration() throws MetricsConfigException {
        return getConfiguration("/metrics.xml");
    }

    private static MetricsConfiguration getConfiguration(String resource) throws MetricsConfigException {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration();
        URL file = Utils.class.getResource(resource);
        String filePath = file.getPath();
        metricsConfiguration.load(filePath);
        return metricsConfiguration;
    }
}
