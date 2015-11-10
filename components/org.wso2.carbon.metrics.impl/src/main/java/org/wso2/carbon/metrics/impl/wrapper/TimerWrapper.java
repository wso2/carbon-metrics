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
 *
 *
 */
package org.wso2.carbon.metrics.impl.wrapper;

import org.wso2.carbon.metrics.impl.AbstractMetric;
import org.wso2.carbon.metrics.impl.internal.MetricServiceValueHolder;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Metric;
import org.wso2.carbon.metrics.manager.MetricUpdater;
import org.wso2.carbon.metrics.manager.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Implementation class wrapping {@link com.codahale.metrics.Timer} metric
 */
public class TimerWrapper implements Timer {

    private com.codahale.metrics.Timer timer;

    public TimerWrapper(com.codahale.metrics.Timer timer) {
        this.timer = timer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Timer#update(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void update(long duration, TimeUnit unit) {
            timer.update(duration, unit);
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
     * @see org.wso2.carbon.metrics.manager.Timer#time()
     */
    @Override
    public Context start() {
            return new ContextImpl(timer.time());

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


    private static class ContextImpl implements Context {

        private com.codahale.metrics.Timer.Context context;

        private ContextImpl(com.codahale.metrics.Timer.Context context) {
            this.context = context;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.wso2.carbon.metrics.manager.Timer.Context#stop()
         */
        @Override
        public long stop() {
            return context.stop();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.wso2.carbon.metrics.manager.Timer.Context#close()
         */
        @Override
        public void close() {
            context.close();
        }

    }

}
