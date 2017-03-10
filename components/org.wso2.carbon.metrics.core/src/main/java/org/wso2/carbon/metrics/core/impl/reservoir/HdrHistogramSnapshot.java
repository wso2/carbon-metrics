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

import com.codahale.metrics.Snapshot;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramIterationValue;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * A statistical snapshot of a {@link HdrHistogramSnapshot}.
 */
public final class HdrHistogramSnapshot extends Snapshot {

    private final Histogram histogram;

    public HdrHistogramSnapshot(Histogram histogram) {
        this.histogram = histogram;
    }

    @Override
    public double getValue(double quantile) {
        return histogram.getValueAtPercentile(quantile * 100.0);
    }

    @Override
    public long[] getValues() {
        long[] values = new long[(int) histogram.getTotalCount()];
        int i = 0;

        for (HistogramIterationValue value : histogram.recordedValues()) {
            long v = value.getValueIteratedTo();
            for (int j = 0; j < value.getCountAddedInThisIterationStep(); j++) {
                values[i] = v;
                i++;
            }
        }

        return values;
    }

    @Override
    public int size() {
        return (int) histogram.getTotalCount();
    }

    @Override
    public long getMax() {
        return histogram.getMaxValue();
    }

    @Override
    public double getMean() {
        return histogram.getMean();
    }

    @Override
    public long getMin() {
        return histogram.getMinValue();
    }

    @Override
    public double getStdDev() {
        return histogram.getStdDeviation();
    }

    @Override
    public void dump(OutputStream output) {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            for (HistogramIterationValue value : histogram.recordedValues()) {
                long v = value.getValueIteratedTo();
                for (int j = 0; j < value.getCountAddedInThisIterationStep(); j++) {
                    out.printf("%d%n", v);
                }
            }
        }
    }
}
