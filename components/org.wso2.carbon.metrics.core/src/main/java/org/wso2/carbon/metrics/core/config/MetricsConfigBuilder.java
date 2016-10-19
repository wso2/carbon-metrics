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

import org.wso2.carbon.metrics.core.utils.Utils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Build Metrics Configuration from the YAML file
 */
public final class MetricsConfigBuilder {

    public static <T> T build(Class<T> clazz, Supplier<T> defaultConfig) {
        Optional<String> metricsConfigFileContent = Utils.readFile("metrics.conf", "metrics.yml");
        if (metricsConfigFileContent.isPresent()) {
            try {
                Representer representer = new Representer();
                representer.getPropertyUtils().setSkipMissingProperties(true);
                Yaml yaml = new Yaml(new CustomClassLoaderConstructor(clazz, clazz.getClassLoader()), representer);
                return yaml.loadAs(metricsConfigFileContent.get(), clazz);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to populate Metrics Configuration", e);
            }
        } else {
            return defaultConfig.get();
        }
    }

}
