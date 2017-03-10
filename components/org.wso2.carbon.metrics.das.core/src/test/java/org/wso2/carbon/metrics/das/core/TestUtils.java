/*
 * Copyright 2017 WSO2 Inc. (http://wso2.org)
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

import org.wso2.carbon.kernel.configprovider.ConfigFileReader;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.kernel.configprovider.YAMLBasedConfigFileReader;
import org.wso2.carbon.kernel.internal.configprovider.ConfigProviderImpl;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Test Utilities
 */
public class TestUtils {

    public static ConfigProvider getConfigProvider(String file) {
        // Clear deploymentConfigs
        try {
            Field field = ConfigProviderImpl.class.getDeclaredField("deploymentConfigs");
            field.setAccessible(true);
            field.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Ignore
        }
        System.setProperty("carbon.home", "src" + File.separator + "test" + File.separator + "resources");
        ConfigFileReader configFileReader = new YAMLBasedConfigFileReader(file);
        return new ConfigProviderImpl(configFileReader);
    }
}
