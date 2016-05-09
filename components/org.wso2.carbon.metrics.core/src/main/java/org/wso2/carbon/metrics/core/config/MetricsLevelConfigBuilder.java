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
package org.wso2.carbon.metrics.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.config.model.MetricsLevelConfig;
import org.wso2.carbon.metrics.core.internal.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;

/**
 * Build Metrics Level Configuration from a properties file
 */
public class MetricsLevelConfigBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MetricsLevelConfigBuilder.class);

    private static final String METRICS_ROOT_LEVEL = "metrics.rootLevel";

    private static final String METRIC_LEVEL_PREFIX = "metric.level.";

    private MetricsLevelConfigBuilder() {
    }

    public static MetricsLevelConfig build() {
        MetricsLevelConfig metricsLevelConfig = new MetricsLevelConfig();
        Optional<File> metricsLevelConfigFile = Utils.getConfigFile("metrics.level.conf", "metrics.properties");
        if (metricsLevelConfigFile.isPresent()) {
            File file = metricsLevelConfigFile.get();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Loading Metrics Level Configuration from %s", file.getAbsolutePath()));
            }
            try (FileInputStream in = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(in);

                metricsLevelConfig.setRootLevel(Level.toLevel(properties.getProperty(METRICS_ROOT_LEVEL,
                        Level.OFF.name()).trim(), Level.OFF));
                Enumeration<?> enumeration = properties.propertyNames();
                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    if (key.startsWith(METRIC_LEVEL_PREFIX)) {
                        String metricName = key.substring(METRIC_LEVEL_PREFIX.length());
                        String value = properties.getProperty(key);
                        if (value != null) {
                            metricsLevelConfig.setLevel(metricName, Level.toLevel(value.trim(), Level.OFF));
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load Metrics Level Configuration from "
                        + file.getAbsolutePath(), e);
            }
        }

        return metricsLevelConfig;
    }

}
