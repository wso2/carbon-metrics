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
 *
 *
 */
package org.wso2.carbon.metrics.impl.wrapper;

import org.wso2.carbon.metrics.impl.AbstractMetric;
import org.wso2.carbon.metrics.impl.internal.MetricServiceValueHolder;
import org.wso2.carbon.metrics.manager.Histogram;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Metric;
import org.wso2.carbon.metrics.manager.MetricUpdater;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation class wrapping {@link com.codahale.metrics.Histogram} metric
 */
public class HistogramWrapper implements Histogram {

    private com.codahale.metrics.Histogram histogram;

    public HistogramWrapper(com.codahale.metrics.Histogram histogram) {
        this.histogram = histogram;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Histogram#update(int)
     */
    @Override
    public void update(int value) {
        histogram.update(value);
    }



    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Histogram#update(long)
     */
    @Override
    public void update(long value) {

            histogram.update(value);

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
