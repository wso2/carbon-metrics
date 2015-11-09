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
public class TimerImpl extends AbstractMetric implements Timer, MetricUpdater {

    private com.codahale.metrics.Timer timer;
    private List<Timer> affected;

    public TimerImpl(Level level, String name, String path, String statName, com.codahale.metrics.Timer timer) {
        super(level, name, path, statName);
        this.timer = timer;
        this.affected = new ArrayList<Timer>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Timer#update(long, java.util.concurrent.TimeUnit)
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
     * @see org.wso2.carbon.metrics.manager.Timer#updateAll(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void updateAll(long duration, TimeUnit unit) {
        if (isEnabled()) {
            timer.update(duration, unit);
            for (Timer t : this.affected) {
                t.update(duration, unit);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Timer#time(java.util.concurrent.Callable)
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
     * @see org.wso2.carbon.metrics.manager.Timer#time()
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
     * @see org.wso2.carbon.metrics.manager.Timer#getCount()
     */
    @Override
    public long getCount() {
        return timer.getCount();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricUpdater#updateAffectedMetrics(String)
     */
    @Override
    public void updateAffectedMetrics(String path) {
        affected.clear();
        super.setPath(path);
        List<Metric> affectedMetrics = MetricServiceValueHolder.getMetricServiceInstance().getAffectedMetrics(getLevel(), getName(), path, getStatName());
        for (Metric metric : affectedMetrics) {
            affected.add((Timer) metric);
        }
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

    private static class DummyContextImpl implements Context {

        @Override
        public long stop() {
            return 0;
        }

        @Override
        public void close() {
        }

    }

}
