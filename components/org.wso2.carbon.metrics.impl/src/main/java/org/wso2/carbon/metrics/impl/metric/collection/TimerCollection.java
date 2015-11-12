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

import org.wso2.carbon.metrics.manager.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Implementation class wrapping {@link List<Timer>} metrics
 */
public class TimerCollection implements Timer {

    private Timer timer;
    private List<Timer> affected;

    public TimerCollection(Timer timer, List<Timer> affectedTimers) {
        this.timer = timer;
        this.affected = new ArrayList<Timer>();
        this.affected.add(timer);
        this.affected.addAll(affectedTimers);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Timer#update(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void update(long duration, TimeUnit unit) {
        for (Timer t : this.affected) {
            t.update(duration, unit);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Timer#time(java.util.concurrent.Callable)
     */
    @Override
    public <T> T time(Callable<T> event) throws Exception {
        return timer.time(event);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Timer#start()
     */
    @Override
    public Context start() {
        return timer.start();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Timer#getCount()
     */
    @Override
    public long getCount() {
        return timer.getCount();
    }

}
