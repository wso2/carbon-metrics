/*
 * Copyright 2014 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.core.impl;

import org.wso2.carbon.metrics.core.Gauge;
import org.wso2.carbon.metrics.core.Level;

/**
 * Implementation of {@link Gauge} metric
 *
 * @param <T> the type of the gauge value
 */
public class GaugeImpl<T> extends AbstractMetric implements com.codahale.metrics.Gauge<T> {

    private final Gauge<T> gauge;

    public GaugeImpl(String name, Level level, Gauge<T> gauge) {
        super(name, level);
        this.gauge = gauge;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.codahale.metrics.Gauge#getValue()
     */
    @Override
    public T getValue() {
        return gauge.getValue();
    }

}
