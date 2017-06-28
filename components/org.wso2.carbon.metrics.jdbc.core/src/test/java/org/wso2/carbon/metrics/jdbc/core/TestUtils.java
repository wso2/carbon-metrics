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
package org.wso2.carbon.metrics.jdbc.core;


import org.easymock.EasyMock;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.config.provider.ConfigProviderImpl;
import org.wso2.carbon.config.reader.ConfigFileReader;
import org.wso2.carbon.config.reader.YAMLBasedConfigFileReader;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.secvault.exception.SecureVaultException;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test Utilities.
 */
public class TestUtils {

    public static ConfigProvider getConfigProvider(String file) throws ConfigurationException {
        SecureVault secureVault = EasyMock.mock(SecureVault.class);
        try {
            EasyMock.expect(secureVault.resolve(EasyMock.anyString())).andReturn("n3wP4s5w0r4"
                    .toCharArray()).anyTimes();
        } catch (SecureVaultException e) {
            throw new ConfigurationException("Error resolving secure vault", e);
        }
        EasyMock.replay(secureVault);
        // Clear deploymentConfigs
        try {
            Field field = ConfigProviderImpl.class.getDeclaredField("deploymentConfigs");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // Ignore
        }
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test");
        System.setProperty("carbon.home", carbonHome.toString());
        String filePath = carbonHome.toAbsolutePath() + File.separator + "resources" +  File.separator + "conf" + File
                .separator + file;
        Path configurationFilePath = Paths.get(URI.create("file:" + filePath));
        ConfigFileReader configFileReader = new YAMLBasedConfigFileReader(configurationFilePath);
        return new ConfigProviderImpl(configFileReader, secureVault);
    }
}
