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

package org.wso2.carbon.metrics.impl.util;

public class MetricsConstants {
    private MetricsConstants() {
        // Prevents instantiation.
    }

    public static final String SYSTEM_PROPERTY_METRICS_ENABLED = "metrics.enabled";
    public static final String SYSTEM_PROPERTY_METRICS_ROOT_LEVEL = "metrics.rootLevel";
    public static final String ROOT_METRIC_NAME = "";
    public static final String METRIC_AGGREGATE_ANNOTATION = "[+]";
    public static final String METRIC_AGGREGATE_ANNOTATION_REGEX = "\\[\\+\\]";
    public static final String METRIC_PATH_DELIMITER = ".";
}