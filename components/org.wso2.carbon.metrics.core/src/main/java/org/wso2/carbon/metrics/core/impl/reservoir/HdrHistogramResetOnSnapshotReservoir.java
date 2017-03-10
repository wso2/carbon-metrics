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
package org.wso2.carbon.metrics.core.impl.reservoir;

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;

/**
 * <p>
 * A {@link Reservoir} implementation using {@code HdrHistogram}.
 * </p>
 * <p>
 * This implementation resets the histogram every time a snapshot is taken.
 * </p>
 */
public final class HdrHistogramResetOnSnapshotReservoir implements Reservoir {

    /**
     * The main recorder used to record all values
     */
    private final Recorder recorder;

    /**
     * This interval histogram is reused when taking the next interval histogram
     */
    private Histogram intervalHistogram;

    /**
     * Create a reservoir with a default recorder.
     */
    public HdrHistogramResetOnSnapshotReservoir() {
        this(new Recorder(2));
    }

    /**
     * Create a reservoir with a user-specified recorder.
     *
     * @param recorder {@link Recorder} to use
     */
    public HdrHistogramResetOnSnapshotReservoir(Recorder recorder) {
        this.recorder = recorder;
        intervalHistogram = recorder.getIntervalHistogram();
    }

    @Override
    public int size() {
        return getSnapshot().size();
    }

    @Override
    public void update(long value) {
        recorder.recordValue(value);
    }

    /**
     * @return A snapshot of the histogram with the values recorded since the last snapshot was taken.
     */
    @Override
    public Snapshot getSnapshot() {
        return new HdrHistogramSnapshot(getHistogram());
    }

    /**
     * @return a copy of the interval histogram taken from the recorder
     */
    private synchronized Histogram getHistogram() {
        intervalHistogram = recorder.getIntervalHistogram(intervalHistogram);
        return intervalHistogram.copy();
    }
}
