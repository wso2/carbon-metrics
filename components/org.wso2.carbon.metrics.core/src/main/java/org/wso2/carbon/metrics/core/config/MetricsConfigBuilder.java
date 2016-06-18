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

import org.wso2.carbon.metrics.core.config.model.MetricsConfig;
import org.wso2.carbon.metrics.core.internal.Utils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.parser.ParserException;

import java.util.Optional;

/**
 * Build {@link MetricsConfig} from the YAML file
 */
public class MetricsConfigBuilder {

    private MetricsConfigBuilder() {
    }

    public static MetricsConfig build() {
        MetricsConfig metricsConfig;
        Optional<String> metricsConfigFileContent = Utils.readFile("metrics.conf", "metrics.yml");
        if (metricsConfigFileContent.isPresent()) {
            try {
                Yaml yaml = new Yaml();
                metricsConfig = yaml.loadAs(metricsConfigFileContent.get(), MetricsConfig.class);
            } catch (ParserException e) {
                throw new RuntimeException("Failed to populate Metrics Configuration", e);
            }
        } else {
            metricsConfig = new MetricsConfig();
        }
        return metricsConfig;
    }

}
