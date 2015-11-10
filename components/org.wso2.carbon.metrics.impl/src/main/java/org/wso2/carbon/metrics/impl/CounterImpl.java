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

import org.wso2.carbon.metrics.impl.internal.MetricServiceValueHolder;
import org.wso2.carbon.metrics.impl.updater.CounterUpdater;
import org.wso2.carbon.metrics.manager.Counter;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Metric;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation class wrapping {@link com.codahale.metrics.Counter} metric
 */
public class CounterImpl extends AbstractMetric implements Counter, CounterUpdater {

    private com.codahale.metrics.Counter counter;
    private List<CounterUpdater> affected;

    public CounterImpl(Level level, String name, String path, String statName, com.codahale.metrics.Counter counter) {
        super(level, name, path, statName);
        this.counter = counter;
        this.affected = new ArrayList<CounterUpdater>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Counter#inc()
     */
    @Override
    public void inc() {
        if (isEnabled()) {
            counter.inc();
            for (CounterUpdater c : this.affected) {
                c.incSelf();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.impl.updater.CounterUpdater#incSelf()
     */
    @Override
    public void incSelf() {
        if (isEnabled()) {
            counter.inc();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Counter#inc(long)
     */
    @Override
    public void inc(long n) {
        if (isEnabled()) {
            counter.inc(n);
            for (CounterUpdater c : this.affected) {
                c.incSelf(n);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.impl.updater.CounterUpdater#incSelf(long)
     */
    @Override
    public void incSelf(long n) {
        if (isEnabled()) {
            counter.inc(n);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Counter#dec()
     */
    @Override
    public void dec() {
        if (isEnabled()) {
            counter.dec();
            for (CounterUpdater c : this.affected) {
                c.decSelf();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.impl.updater.CounterUpdater#decSelf()
     */
    @Override
    public void decSelf() {
        if (isEnabled()) {
            counter.dec();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Counter#dec(long)
     */
    @Override
    public void dec(long n) {
        if (isEnabled()) {
            counter.dec(n);
            for (CounterUpdater c : this.affected) {
                c.decSelf(n);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.impl.updater.CounterUpdater#decSelf(long)
     */
    @Override
    public void decSelf(long n) {
        if (isEnabled()) {
            counter.dec(n);
        }
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

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.impl.updater.MetricUpdater#updateAffectedMetrics(String)
     */
    @Override
    public void updateAffectedMetrics(String path) {
        affected.clear();
        super.setPath(path);
        List<Metric> affectedMetrics = MetricServiceValueHolder.getMetricServiceInstance().getAffectedMetrics(getLevel(), getName(), path, getStatName());
        for (Metric metric : affectedMetrics) {
            affected.add((CounterUpdater) metric);
        }
    }

}
