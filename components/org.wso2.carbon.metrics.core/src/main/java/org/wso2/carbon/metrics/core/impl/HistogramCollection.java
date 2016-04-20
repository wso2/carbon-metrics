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
package org.wso2.carbon.metrics.core.impl;

import org.wso2.carbon.metrics.core.Histogram;

import java.util.List;


/**
 * Implementation class wrapping a list of {@link Histogram} metrics
 */
public class HistogramCollection implements Histogram {

    private Histogram histogram;
    private List<Histogram> affected;

    public HistogramCollection(Histogram histogram, List<Histogram> affectedHistograms) {
        this.histogram = histogram;
        this.affected = affectedHistograms;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Histogram#update(int)
     */
    @Override
    public void update(int value) {
        histogram.update(value);
        for (Histogram h : affected) {
            h.update(value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Histogram#update(long)
     */
    @Override
    public void update(long value) {
        histogram.update(value);
        for (Histogram h : affected) {
            h.update(value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Histogram#getCount()
     */
    @Override
    public long getCount() {
        return histogram.getCount();
    }
}
