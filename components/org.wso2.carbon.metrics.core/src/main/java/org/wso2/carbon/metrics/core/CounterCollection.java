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
package org.wso2.carbon.metrics.core;

import java.util.List;

/**
 * Implementation class wrapping a list of {@link Counter} metrics
 */
public class CounterCollection implements Counter {

    private Counter counter;
    private List<Counter> affected;

    public CounterCollection(Counter counter, List<Counter> affectedCounters) {
        this.counter = counter;
        this.affected = affectedCounters;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Counter#inc()
     */
    @Override
    public void inc() {
        counter.inc();
        for (Counter c : affected) {
            c.inc();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Counter#inc(long)
     */
    @Override
    public void inc(long n) {
        counter.inc(n);
        for (Counter c : affected) {
            c.inc(n);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Counter#dec()
     */
    @Override
    public void dec() {
        counter.dec();
        for (Counter c : affected) {
            c.dec();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Counter#dec(long)
     */
    @Override
    public void dec(long n) {
        counter.dec(n);
        for (Counter c : affected) {
            c.dec(n);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.core.Counter#getCount()
     */
    @Override
    public long getCount() {
        return counter.getCount();
    }

}
