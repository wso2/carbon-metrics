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
package org.wso2.carbon.metrics.impl.metric;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

/**
 * A set of gauges for Operating System usage, including stats on load average, cpu load, file descriptors etc
 */
public class OperatingSystemMetricSet implements MetricSet {

    private final OperatingSystemMXBean mxBean;

    public OperatingSystemMetricSet() {
        this(ManagementFactory.getOperatingSystemMXBean());
    }

    public OperatingSystemMetricSet(OperatingSystemMXBean mxBean) {
        this.mxBean = mxBean;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();

        gauges.put("system.load.average", new Gauge<Double>() {
            @Override
            public Double getValue() {
                return mxBean.getSystemLoadAverage();
            }
        });

        gauges.put("file.descriptor.open.count", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return invokeLong("getOpenFileDescriptorCount");
            }
        });

        gauges.put("file.descriptor.max.count", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return invokeLong("getMaxFileDescriptorCount");
            }
        });

        gauges.put("cpu.load.process", new Gauge<Double>() {
            @Override
            public Double getValue() {
                return invokeDouble("getProcessCpuLoad");
            }
        });

        gauges.put("cpu.load.system", new Gauge<Double>() {
            @Override
            public Double getValue() {
                return invokeDouble("getSystemCpuLoad");
            }
        });

        gauges.put("physical.memory.free.size", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return invokeLong("getFreePhysicalMemorySize");
            }
        });

        gauges.put("physical.memory.total.size", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return invokeLong("getTotalPhysicalMemorySize");
            }
        });

        gauges.put("swap.space.free.size", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return invokeLong("getFreeSwapSpaceSize");
            }
        });

        gauges.put("swap.space.total.size", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return invokeLong("getTotalSwapSpaceSize");
            }
        });

        gauges.put("virtual.memory.committed.size", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return invokeLong("getCommittedVirtualMemorySize");
            }
        });

        return Collections.unmodifiableMap(gauges);
    }

    private long invokeLong(String name) {
        try {
            final Method method = mxBean.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            return (Long) method.invoke(mxBean);
        } catch (NoSuchMethodException e) {
            return 0L;
        } catch (IllegalAccessException e) {
            return 0L;
        } catch (InvocationTargetException e) {
            return 0L;
        }
    }

    private double invokeDouble(String name) {
        try {
            final Method method = mxBean.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            return (Double) method.invoke(mxBean);
        } catch (NoSuchMethodException e) {
            return -1.0;
        } catch (IllegalAccessException e) {
            return -1.0;
        } catch (InvocationTargetException e) {
            return -1.0;
        }
    }
}
