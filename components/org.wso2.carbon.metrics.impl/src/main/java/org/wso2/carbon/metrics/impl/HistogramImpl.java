/*
 * Copyright 2014-2015 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.impl;

import org.wso2.carbon.metrics.manager.Histogram;
import org.wso2.carbon.metrics.manager.Level;

/**
 * Implementation class wrapping {@link com.codahale.metrics.Histogram} metric
 */
public class HistogramImpl extends AbstractMetric implements Histogram {

    private com.codahale.metrics.Histogram histogram;

    public HistogramImpl(String name, Level level, com.codahale.metrics.Histogram histogram) {
        super(name, level);
        this.histogram = histogram;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Histogram#update(int)
     */
    @Override
    public void update(int value) {
        if (isEnabled()) {
            histogram.update(value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Histogram#update(long)
     */
    @Override
    public void update(long value) {
        if (isEnabled()) {
            histogram.update(value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Histogram#getCount()
     */
    @Override
    public long getCount() {
        return histogram.getCount();
    }
}
