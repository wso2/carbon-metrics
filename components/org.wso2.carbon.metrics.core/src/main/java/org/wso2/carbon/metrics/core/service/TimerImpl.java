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
package org.wso2.carbon.metrics.core.service;

import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Snapshot;
import org.wso2.carbon.metrics.core.Timer;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Implementation class wrapping {@link com.codahale.metrics.Timer} metric
 */
public class TimerImpl extends AbstractMetric implements Timer {

    private final com.codahale.metrics.Timer timer;

    public TimerImpl(String name, Level level, com.codahale.metrics.Timer timer) {
        super(name, level);
        this.timer = timer;
    }

    private static class ContextImpl implements Context {

        private com.codahale.metrics.Timer.Context context;

        private ContextImpl(com.codahale.metrics.Timer.Context context) {
            this.context = context;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.wso2.carbon.metrics.core.Timer.Context#stop()
         */
        @Override
        public long stop() {
            return context.stop();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.wso2.carbon.metrics.core.Timer.Context#close()
         */
        @Override
        public void close() {
            context.close();
        }

    }

    private static class DummyContextImpl implements Context {

        @Override
        public long stop() {
            return 0;
        }

        @Override
        public void close() {
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Timer#update(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void update(long duration, TimeUnit unit) {
        if (isEnabled()) {
            timer.update(duration, unit);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Timer#time(java.util.concurrent.Callable)
     */
    @Override
    public <T> T time(Callable<T> event) throws Exception {
        if (isEnabled()) {
            return timer.time(event);
        }
        // TODO Should we throw an exception?
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Timer#time()
     */
    @Override
    public Context start() {
        if (isEnabled()) {
            return new ContextImpl(timer.time());
        }
        return new DummyContextImpl();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.core.Timer#getCount()
     */
    @Override
    public long getCount() {
        return timer.getCount();
    }

    @Override
    public double getFifteenMinuteRate() {
        return timer.getFifteenMinuteRate();
    }

    @Override
    public double getFiveMinuteRate() {
        return timer.getFiveMinuteRate();
    }

    @Override
    public double getMeanRate() {
        return timer.getMeanRate();
    }

    @Override
    public double getOneMinuteRate() {
        return timer.getOneMinuteRate();
    }

    @Override
    public Snapshot getSnapshot() {
        return new SnapshotImpl(timer.getSnapshot());
    }

}
