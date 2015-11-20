/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.metrics.view.ui;

import org.wso2.carbon.metrics.data.service.stub.MetricMeta;

public class MetricMetaWrapper {
    private String name;
    private String type;
    private String displayName;

    public MetricMetaWrapper(MetricMeta metricMeta) {
        super();
        this.name = metricMeta.getName();
        this.type = metricMeta.getType();
        this.displayName = this.name.substring(this.name.lastIndexOf('.') + 1);
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getType() {
        return this.type;
    }
}
