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
import org.wso2.carbon.metrics.manager.Counter;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Metric;
import org.wso2.carbon.metrics.manager.MetricUpdater;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation class wrapping {@link com.codahale.metrics.Counter} metric
 */
public class CounterWrapper implements Counter{

    private com.codahale.metrics.Counter counter;

    public CounterWrapper(com.codahale.metrics.Counter counter) {
        this.counter = counter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.Counter#inc()
     */
    @Override
    public void inc() {
            counter.inc();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.Counter#inc(long)
     */
    @Override
    public void inc(long n) {
            counter.inc(n);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.Counter#dec()
     */
    @Override
    public void dec() {
            counter.dec();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.Counter#dec(long)
     */
    @Override
    public void dec(long n) {
            counter.dec(n);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.Counter#getCount()
     */
    @Override
    public long getCount() {
        return counter.getCount();
    }
}
