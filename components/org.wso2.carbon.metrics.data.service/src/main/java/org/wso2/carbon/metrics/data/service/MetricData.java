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

import java.math.BigDecimal;

/**
 * Description about MetricData
 */
public class MetricData {

    private Metadata metadata;

    private BigDecimal[][] data;

    public MetricData() {
    }

    public MetricData(Metadata metadata, BigDecimal[][] data) {
        super();
        this.metadata = metadata;
        this.data = data;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public BigDecimal[][] getData() {
        return data;
    }

    public void setData(BigDecimal[][] data) {
        this.data = data;
    }

}
