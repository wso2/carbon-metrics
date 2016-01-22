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

public class MetricHierarchyData {
    private String path;
    private String[] children;
    private MetricMeta[] metrics;

    public MetricHierarchyData() {
    }

    public MetricHierarchyData(String path, String[] children, MetricMeta[] metrics) {
        this.path = path;
        this.children = children;
        this.metrics = metrics;
    }

    public String getPath() {
        return path;
    }

    public String[] getChildren() {
        return children;
    }

    public MetricMeta[] getMetrics() {
        return metrics;
    }
}
