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
import org.wso2.carbon.metrics.core.config.model.MetricsConfig;
import org.wso2.carbon.metrics.core.utils.Utils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by isuru on 3/23/16.
 */
public class MetricsConfigBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MetricsConfigBuilder.class);

    private MetricsConfigBuilder() {
    }

    public static MetricsConfig build() {
        MetricsConfig metricsConfig;
        Optional<File> metricsConfigFile = Utils.getConfigFile("metrics.conf", "metrics.yml");
        if (metricsConfigFile.isPresent()) {
            File file = metricsConfigFile.get();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Loading Metrics Configuration from %s", file.getAbsolutePath()));
            }
            try (Stream<String> stream = Files.lines(file.toPath())) {
                String fileContent = stream.map(s -> org.wso2.carbon.kernel.utils.Utils.substituteVariables(s))
                        .collect(Collectors.joining(System.lineSeparator()));
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                metricsConfig = yaml.loadAs(fileContent, MetricsConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed populate MetricsConfig from " + file.getAbsolutePath(), e);
            }
        } else {
            metricsConfig = new MetricsConfig();
        }

        return metricsConfig;
    }

}
