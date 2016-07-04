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
package org.wso2.carbon.metrics.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility methods for Metrics
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static volatile String defaultSource;

    private static volatile boolean carbonEnvironment;

    /**
     * A utility method to provide a default source value
     *
     * @return The default source, if it is available, otherwise return the hostname. If the hostname is also not
     * available, it will return "Carbon".
     */
    public static String getDefaultSource() {
        if (defaultSource == null) {
            // Use host name if available
            String hostname = null;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                // Ignore exception
            }
            if (hostname == null || hostname.trim().length() == 0) {
                defaultSource = "Carbon";
            } else {
                defaultSource = hostname;
            }
        }
        return defaultSource;
    }

    /**
     * Set the default source for the reporters
     *
     * @param defaultSource The value to be used as the default source for the reporters
     */
    public static void setDefaultSource(String defaultSource) {
        Utils.defaultSource = defaultSource;
    }

    /**
     * Check whether the Metrics is running in the Carbon Environment (OSGi)
     *
     * @return {@code true} if the Metrics is running in the Carbon Environment
     */
    public static boolean isCarbonEnvironment() {
        return carbonEnvironment;
    }

    /**
     * Set the flag used to check whether the Metrics is running in Carbon Environment (OSGi)
     *
     * @param carbonEnvironment Set {@code true} if the Metrics is running in the Carbon Environment
     */
    public static void setCarbonEnvironment(boolean carbonEnvironment) {
        Utils.carbonEnvironment = carbonEnvironment;
    }

    /**
     * Get the configuration file. The optional will have the file only if it exists and if it is a valid file.
     *
     * @param key      The system property key
     * @param fileName The file name
     * @return An optional {@link File}
     */
    public static Optional<File> getConfigFile(final String key, final String fileName) {
        // Check System Property first. If not found, just find the file in the root directory.
        File file = new File(System.getProperty(key, fileName));
        if (!file.exists() && carbonEnvironment) {
            // Try to find the config file in the Carbon Conf Directory
            file = new File(org.wso2.carbon.kernel.utils.Utils.getCarbonConfigHome().resolve(fileName).toString());
        }

        if (file.exists() && file.isFile()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Configuration file found at {}", file.getAbsolutePath());
            }
            return Optional.of(file);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Load properties. The optional will have the  {@link Properties} loaded from the given file when the file exists
     * in the system or when it is found in the classpath.
     *
     * @param key      The system property key
     * @param fileName The file name
     * @return An optional {@link Properties}
     */
    public static Optional<Properties> loadProperties(final String key, final String fileName) {
        Optional<File> configFile = getConfigFile(key, fileName);
        try (final InputStream in = configFile.isPresent() ? new FileInputStream(configFile.get()) :
                Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                return Optional.of(properties);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read properties from the file: " + fileName, e);
        }
    }

    /**
     * Read file content to a String. The optional will have the file content from the given file in a {@link String}
     * when the file exists in the system or when it is found in the classpath.
     *
     * @param key      The system property key
     * @param fileName The file name
     * @return An optional {@link String}
     */
    public static Optional<String> readFile(final String key, final String fileName) {
        Optional<File> configFile = getConfigFile(key, fileName);
        try (final InputStream in = configFile.isPresent() ? new FileInputStream(configFile.get()) :
                Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (in != null) {
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                     Stream<String> stream = buffer.lines()) {
                    String fileContent = stream.map(org.wso2.carbon.kernel.utils.Utils::substituteVariables)
                            .collect(Collectors.joining(System.lineSeparator()));
                    return Optional.of(fileContent);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read lines from the file: " + fileName, e);
        }

        return Optional.empty();
    }

}

