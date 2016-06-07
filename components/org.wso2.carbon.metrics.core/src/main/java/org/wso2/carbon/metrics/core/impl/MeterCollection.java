/*
 * Copyright 2015 WSO2 Inc. (http://wso2.org)
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

import org.wso2.carbon.metrics.core.Meter;

import java.util.List;

/**
 * Implementation class wrapping a list of {@link Meter} metrics
 */
public class MeterCollection implements Meter {

    private Meter meter;
    private List<Meter> affected;

    public MeterCollection(Meter meter, List<Meter> affectedMeters) {
        this.meter = meter;
        this.affected = affectedMeters;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Meter#mark()
     */
    @Override
    public void mark() {
        meter.mark();
        for (Meter m : affected) {
            m.mark();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Meter#mark(long)
     */
    @Override
    public void mark(long n) {
        meter.mark(n);
        for (Meter m : affected) {
            m.mark(n);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Meter#getCount()
     */
    @Override
    public long getCount() {
        return meter.getCount();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Meter#getMeanRate()
     */
    @Override
    public double getMeanRate() {
        return meter.getMeanRate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Meter#getOneMinuteRate()
     */
    @Override
    public double getOneMinuteRate() {
        return meter.getOneMinuteRate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Meter#getFiveMinuteRate()
     */
    @Override
    public double getFiveMinuteRate() {
        return meter.getFiveMinuteRate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Meter#getFifteenMinuteRate()
     */
    @Override
    public double getFifteenMinuteRate() {
        return meter.getFifteenMinuteRate();
    }
}
