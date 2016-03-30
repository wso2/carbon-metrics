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
package org.wso2.carbon.metrics.core.metric;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of gauges for JVM classloader usage.
 */
public class ClassLoadingGaugeSet implements MetricSet {

    private final ClassLoadingMXBean mxBean;

    public ClassLoadingGaugeSet() {
        this(ManagementFactory.getClassLoadingMXBean());
    }

    public ClassLoadingGaugeSet(ClassLoadingMXBean mxBean) {
        this.mxBean = mxBean;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();

        gauges.put("loaded.total", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getTotalLoadedClassCount();
            }
        });

        gauges.put("loaded.current", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return mxBean.getLoadedClassCount();
            }
        });

        gauges.put("unloaded.total", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getUnloadedClassCount();
            }
        });

        return gauges;
    }
}
