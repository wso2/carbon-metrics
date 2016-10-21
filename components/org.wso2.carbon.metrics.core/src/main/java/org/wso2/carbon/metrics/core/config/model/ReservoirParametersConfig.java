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
package org.wso2.carbon.metrics.core.config.model;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for keeping parameters for different reservoir implementations
 */
public class ReservoirParametersConfig {

    /**
     * The number of measurements to store in {@code SlidingWindowReservoir}
     * or number of samples to keep in {@code UniformReservoir}
     */
    private int size = 1028;

    /**
     * The window of time in {@code SlidingTimeWindowReservoir}
     */
    private long window = 1;

    /**
     * The unit of {@code window} in {@code SlidingTimeWindowReservoir}
     */
    private TimeUnit windowUnit = TimeUnit.HOURS;

    /**
     * The precision to use in the {@code Recorder} to be used in {@code HdrHistogramReservoir} and
     * {@code HdrHistogramResetOnSnapshotReservoir}
     */
    private int numberOfSignificantValueDigits = 2;

    /**
     * Reset the {@code HdrHistogram} when taking a snapshot
     */
    private boolean resetOnSnapshot;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getWindow() {
        return window;
    }

    public void setWindow(long window) {
        this.window = window;
    }

    public TimeUnit getWindowUnit() {
        return windowUnit;
    }

    public void setWindowUnit(TimeUnit windowUnit) {
        this.windowUnit = windowUnit;
    }

    public int getNumberOfSignificantValueDigits() {
        return numberOfSignificantValueDigits;
    }

    public void setNumberOfSignificantValueDigits(int numberOfSignificantValueDigits) {
        this.numberOfSignificantValueDigits = numberOfSignificantValueDigits;
    }

    public boolean isResetOnSnapshot() {
        return resetOnSnapshot;
    }

    public void setResetOnSnapshot(boolean resetOnSnapshot) {
        this.resetOnSnapshot = resetOnSnapshot;
    }
}
