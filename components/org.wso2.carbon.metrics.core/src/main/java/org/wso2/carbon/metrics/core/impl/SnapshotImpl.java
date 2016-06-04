/*
 * Copyright 2016 WSO2 Inc. (http://wso2.org)
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

import org.wso2.carbon.metrics.core.Snapshot;

/**
 * Implementation class wrapping {@link com.codahale.metrics.Snapshot} for sampling metrics
 */
public class SnapshotImpl implements Snapshot {

    private final com.codahale.metrics.Snapshot snapshot;

    public SnapshotImpl(com.codahale.metrics.Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public double getStdDev() {
        return snapshot.getStdDev();
    }

    @Override
    public double getValue(double quantile) {
        return snapshot.getValue(quantile);
    }

    @Override
    public long[] getValues() {
        return snapshot.getValues();
    }

    @Override
    public int size() {
        return snapshot.size();
    }

    @Override
    public double getMedian() {
        return snapshot.getMedian();
    }

    @Override
    public double get75thPercentile() {
        return snapshot.get75thPercentile();
    }

    @Override
    public double get95thPercentile() {
        return snapshot.get95thPercentile();
    }

    @Override
    public double get98thPercentile() {
        return snapshot.get98thPercentile();
    }

    @Override
    public double get99thPercentile() {
        return snapshot.get99thPercentile();
    }

    @Override
    public double get999thPercentile() {
        return snapshot.get999thPercentile();
    }

    @Override
    public long getMax() {
        return snapshot.getMax();
    }

    @Override
    public double getMean() {
        return snapshot.getMean();
    }

    @Override
    public long getMin() {
        return snapshot.getMin();
    }
}
