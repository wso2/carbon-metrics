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
package org.wso2.carbon.metrics.view.ui;

import java.math.BigDecimal;

import org.wso2.carbon.metrics.data.service.stub.MetricData;

/**
 * Wrapper class for MetricData
 */
public class MetricDataWrapper {

    private final BigDecimal[][] data;

    private final MetadataWrapper metadata;

    public MetricDataWrapper(MetricData metricData) {
        super();
        this.data = new BigDecimal[metricData.getData().length][];
        for (int i = 0; i < metricData.getData().length; i++) {
            data[i] = metricData.getData()[i].getArray();
        }
        metadata = new MetadataWrapper(metricData.getMetadata());
    }

    public BigDecimal[][] getData() {
        return data;
    }

    public MetadataWrapper getMetadata() {
        return metadata;
    }

}
