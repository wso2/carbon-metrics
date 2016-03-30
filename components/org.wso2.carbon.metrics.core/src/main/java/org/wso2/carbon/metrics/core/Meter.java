/*
 * Copyright 2014 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.core;

/**
 * A metric to measure the the rate of events over time
 */
public interface Meter extends Metric {

    /**
     * Mark the occurrence of an event.
     */
    void mark();

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    void mark(long n);

    /**
     * Returns the number of events which have been marked.
     *
     * @return the number of events which have been marked
     */
    long getCount();

    /**
     * Returns the mean rate at which events have occurred since the meter was created.
     *
     * @return the mean rate at which events have occurred since the meter was created
     */
    double getMeanRate();

    /**
     * Returns the one-minute exponentially-weighted moving average rate at which events have occurred since the meter
     * was created. This rate has the same exponential decay factor as the one-minute load average in the {@code top}
     * Unix command.
     *
     * @return the one-minute exponentially-weighted moving average rate at which events have occurred since the meter
     * was created
     */
    double getOneMinuteRate();

    /**
     * Returns the five-minute exponentially-weighted moving average rate at which events have occurred since the meter
     * was created. This rate has the same exponential decay factor as the five-minute load average in the {@code top}
     * Unix command.
     *
     * @return the five-minute exponentially-weighted moving average rate at which events have occurred since the meter
     * was created
     */
    double getFiveMinuteRate();

    /**
     * Returns the fifteen-minute exponentially-weighted moving average rate at which events have occurred since the
     * meter was created. This rate has the same exponential decay factor as the fifteen-minute load average in the
     * {@code top} Unix command.
     *
     * @return the fifteen-minute exponentially-weighted moving average rate at which events have occurred since the
     * meter was created
     */
    double getFifteenMinuteRate();
}
