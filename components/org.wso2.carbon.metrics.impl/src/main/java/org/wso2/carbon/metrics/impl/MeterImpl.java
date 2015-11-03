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

import com.codahale.metrics.Meter;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricUpdater;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Implementation class wrapping {@link Meter} metric
 */
public class MeterImpl extends AbstractMetric implements org.wso2.carbon.metrics.manager.Meter, MetricUpdater {

    private Meter meter;
    private List<Meter> affected;

    public MeterImpl(Level level, String name, String path, String identifier, Meter meter) {
        super(level, name, path, identifier);
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
     * @see org.wso2.carbon.metrics.manager.MetricUpdater#updateAffectedMetrics()
     */
    @Override
    public void updateAffectedMetrics(String path) {
        affected.clear();
        super.setPath(path);
        SortedMap<String, Meter> availableMeters =
                ((MetricServiceImpl) ServiceReferenceHolder.getInstance().getMetricService())
                        .getMetricRegistry().getMeters();
        String[] chunks = path.split("\\.");
        StringBuilder builder = new StringBuilder();
        String name;
        for (String chunk : chunks) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(chunk);
            if (chunk.contains("[+]")) {
                name = builder.toString().replaceAll("\\[\\+\\]", "");
                String absoluteName = ((MetricServiceImpl) ServiceReferenceHolder.getInstance().getMetricService())
                        .getAbsoluteName(getIdentifier(), name);
                if (availableMeters.get(absoluteName) != null) {
                    affected.add(availableMeters.get(absoluteName));
                } else {
                    ServiceReferenceHolder.getInstance().getMetricService().meter(getLevel(), name, name, getIdentifier());
                    updateAffectedMetrics(path);
                }
            }
        }
    }

}
