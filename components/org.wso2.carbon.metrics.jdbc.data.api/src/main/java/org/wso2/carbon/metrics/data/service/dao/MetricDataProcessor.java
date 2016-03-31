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
package org.wso2.carbon.metrics.data.service.dao;

import org.wso2.carbon.metrics.data.common.MetricAttribute;
import org.wso2.carbon.metrics.data.common.MetricType;

import java.math.BigDecimal;

/**
 * Implementing class should process metric data and return the final result.
 *
 * @param <T> The type of the result
 */
public interface MetricDataProcessor<T> {

    void process(String source, long timestamp, MetricType metricType, String name, MetricAttribute attribute,
                 BigDecimal value);

    T getResult();

}
