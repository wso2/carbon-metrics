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

import com.codahale.metrics.Counter;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricUpdater;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Implementation class wrapping {@link Counter} metric
 */
public class CounterImpl extends AbstractMetric implements org.wso2.carbon.metrics.manager.Counter, MetricUpdater {

    private Counter counter;
    private List<Counter> affected;

    public CounterImpl(Level level, String name, String path, String identifier, Counter counter) {
        super(level, name, path, identifier);
        this.counter = counter;
        this.affected = getAffectedMetrics();
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
            for (Counter c : this.affected) {
                c.inc();
            }
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
            for (Counter c : this.affected) {
                c.inc(n);
            }
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
            for (Counter c : this.affected) {
                c.dec();
            }
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
            for (Counter c : this.affected) {
                c.dec(n);
            }
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
     * @see org.wso2.carbon.metrics.manager.MetricUpdater#getAffectedMetrics()
     */
    @Override
    public List<Counter> getAffectedMetrics() {
        SortedMap<String, Counter> availableCounters =
                ((MetricServiceImpl) ServiceReferenceHolder.getInstance().getMetricService())
                        .getMetricRegistry().getCounters();
        List<Counter> affectedMetrics = new ArrayList<Counter>();
        String[] chunks = getPath().split("\\.");
        StringBuilder builder = new StringBuilder();
        String name;
        for (String chunk : chunks) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(chunk);
            if (chunk.contains("[+]")) {
                name = builder.toString().replaceAll("\\[\\+\\]", "");
                name = ((MetricServiceImpl) ServiceReferenceHolder.getInstance().getMetricService())
                        .getAbsoluteName(getIdentifier(), name);
                if (availableCounters.get(name) != null) {
                    affectedMetrics.add(availableCounters.get(name));
                } else {
                    ServiceReferenceHolder.getInstance().getMetricService().counter(getLevel(), name, name, getIdentifier());
                    return getAffectedMetrics();
                }
            }
        }
        return affectedMetrics;
    }

}
