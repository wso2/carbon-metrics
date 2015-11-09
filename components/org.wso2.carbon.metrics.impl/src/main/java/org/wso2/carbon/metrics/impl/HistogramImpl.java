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
import org.wso2.carbon.metrics.manager.Histogram;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Metric;
import org.wso2.carbon.metrics.manager.MetricUpdater;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation class wrapping {@link com.codahale.metrics.Histogram} metric
 */
public class HistogramImpl extends AbstractMetric implements Histogram, MetricUpdater {

    private com.codahale.metrics.Histogram histogram;
    private List<Histogram> affected;

    public HistogramImpl(Level level, String name, String path, String statName, com.codahale.metrics.Histogram histogram) {
        super(level, name, path, statName);
        this.histogram = histogram;
        this.affected = new ArrayList<Histogram>();
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
     * @see org.wso2.carbon.metrics.manager.Histogram#updateAll(int)
     */
    @Override
    public void updateAll(int value) {
        if (isEnabled()) {
            histogram.update(value);
            for (Histogram h : this.affected) {
                h.update(value);
            }
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
     * @see org.wso2.carbon.metrics.manager.Histogram#updateAll(long)
     */
    @Override
    public void updateAll(long value) {
        if (isEnabled()) {
            histogram.update(value);
            for (Histogram h : this.affected) {
                h.update(value);
            }
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

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricUpdater#updateAffectedMetrics(String)
     */
    @Override
    public void updateAffectedMetrics(String path) {
        affected.clear();
        super.setPath(path);
        List<Metric> affectedMetrics = MetricServiceValueHolder.getMetricServiceInstance().getAffectedMetrics(getLevel(), getName(), path, getStatName());
        for (Metric metric : affectedMetrics) {
            affected.add((Histogram) metric);
        }
    }
}
