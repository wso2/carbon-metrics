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
package org.wso2.carbon.metrics.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.manager.Level;

/**
 * Configurations for each metric level
 */
public class MetricsLevelConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MetricsLevelConfiguration.class);

    /**
     * The root level configured for Metrics collection
     */
    private Level rootLevel = Level.OFF;

    private final Map<String, Level> levelMap = new HashMap<String, Level>();

    private static final String METRICS_ROOT_LEVEL = "metrics.rootLevel";

    private static final String METRIC_LEVEL_PREFIX = "metric.level.";

    public MetricsLevelConfiguration() {
    }

    public void load(String filePath) throws MetricsLevelConfigException {
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Loading Metrics Level Configuration from %s", filePath));
            }
            in = new FileInputStream(filePath);
            properties.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            throw new MetricsLevelConfigException("File not found: " + filePath, e);
        } catch (IOException e) {
            throw new MetricsLevelConfigException("I/O error while reading the configuration file: " + filePath, e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        setLevels(properties);
    }

    private void setLevels(Properties properties) {
        rootLevel = Level.toLevel(properties.getProperty(METRICS_ROOT_LEVEL, Level.OFF.name()).trim(), Level.OFF);
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            if (key.startsWith(METRIC_LEVEL_PREFIX)) {
                String metricName = key.substring(METRIC_LEVEL_PREFIX.length());
                String value = properties.getProperty(key);
                if (value != null) {
                    levelMap.put(metricName, Level.toLevel(value.trim(), Level.OFF));
                }
            }
        }
    }

    public Level getRootLevel() {
        return rootLevel;
    }

    public void setRootLevel(Level rootLevel) {
        this.rootLevel = rootLevel;
    }

    public Level getLevel(String metricName) {
        return levelMap.get(metricName);
    }
}
