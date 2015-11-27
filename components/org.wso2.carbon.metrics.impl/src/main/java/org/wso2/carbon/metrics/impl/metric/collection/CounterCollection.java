/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.metrics.impl.metric.collection;

import org.wso2.carbon.metrics.manager.Counter;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation class wrapping {@link List<Counter>} metrics
 */
public class CounterCollection implements Counter {

    private Counter counter;
    private List<Counter> affected;

    public CounterCollection(Counter counter, List<Counter> affectedCounters) {
        this.counter = counter;
        this.affected = new ArrayList<Counter>();
        // TODO get rid of this if condition
        if (!affectedCounters.contains(counter)) {
            this.affected.add(counter);
        }
        this.affected.addAll(affectedCounters);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Counter#inc()
     */
    @Override
    public void inc() {
        for (Counter c : this.affected) {
            c.inc();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Counter#inc(long)
     */
    @Override
    public void inc(long n) {
        for (Counter c : this.affected) {
            c.inc(n);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Counter#dec()
     */
    @Override
    public void dec() {
        for (Counter c : this.affected) {
            c.dec();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Counter#dec(long)
     */
    @Override
    public void dec(long n) {
        for (Counter c : this.affected) {
            c.dec(n);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.metrics.manager.Counter#getCount()
     */
    @Override
    public long getCount() {
        return counter.getCount();
    }

}
