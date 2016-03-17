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

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;

/**
 * Implementation class wrapping {@link com.codahale.metrics.Meter} metric
 */
public class MeterImpl extends AbstractMetric implements Meter {

    private com.codahale.metrics.Meter meter;

    public MeterImpl(String name, Level level, com.codahale.metrics.Meter meter) {
        super(name, level);
        this.meter = meter;
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
     * @see org.wso2.carbon.metrics.manager.Meter#getCount()
     */
    @Override
    public long getCount() {
        return meter.getCount();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Meter#getMeanRate()
     */
    @Override
    public double getMeanRate() {
        return meter.getMeanRate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Meter#getOneMinuteRate()
     */
    @Override
    public double getOneMinuteRate() {
        return meter.getOneMinuteRate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Meter#getFiveMinuteRate()
     */
    @Override
    public double getFiveMinuteRate() {
        return meter.getFiveMinuteRate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Meter#getFifteenMinuteRate()
     */
    @Override
    public double getFifteenMinuteRate() {
        return meter.getFifteenMinuteRate();
    }
}
