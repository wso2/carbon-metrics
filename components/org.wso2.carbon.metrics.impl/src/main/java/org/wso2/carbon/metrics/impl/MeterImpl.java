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
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.Metric;
import org.wso2.carbon.metrics.manager.MetricUpdater;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation class wrapping {@link com.codahale.metrics.Meter} metric
 */
public class MeterImpl extends AbstractMetric implements Meter, MetricUpdater {

    private com.codahale.metrics.Meter meter;
    private List<Meter> affected;

    public MeterImpl(Level level, String name, String path, String statType, com.codahale.metrics.Meter meter) {
        super(level, name, path, statType);
        this.meter = meter;
        this.affected = new ArrayList<Meter>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.Meter#mark()
     */
    @Override
    public void mark() {
        if (isEnabled()) {
            meter.mark();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Meter#markAll()
     */
    @Override
    public void markAll() {
        if (isEnabled()) {
            meter.mark();
            for (Meter m : this.affected) {
                m.mark();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.Meter#mark(long)
     */
    @Override
    public void mark(long n) {
        if (isEnabled()) {
            meter.mark(n);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Meter#markAll(long)
     */
    @Override
    public void markAll(long n) {
        if (isEnabled()) {
            meter.mark(n);
            for (Meter m : this.affected) {
                m.mark(n);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.Meter#getCount()
     */
    @Override
    public long getCount() {
        return meter.getCount();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricUpdater#updateAffectedMetrics(String)
     */
    @Override
    public void updateAffectedMetrics(String path) {
        affected.clear();
        super.setPath(path);
        List<Metric> affectedMetrics = MetricServiceValueHolder.getMetricServiceInstance().getAffectedMetrics(getLevel(), getName(), path, getStatType());
        for (Metric metric : affectedMetrics) {
            affected.add((Meter) metric);
        }
    }

}
