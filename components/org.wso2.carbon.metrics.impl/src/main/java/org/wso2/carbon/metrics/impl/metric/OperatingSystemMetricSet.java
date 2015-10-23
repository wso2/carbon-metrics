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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

/**
 * A set of gauges for Operating System usage, including stats on load average, cpu load, file descriptors etc
 */
public class OperatingSystemMetricSet implements MetricSet {

    private static final Logger logger = LoggerFactory.getLogger(OperatingSystemMetricSet.class);

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

        Double loadAverage = mxBean.getSystemLoadAverage();
        if (loadAverage != null && Double.compare(loadAverage, 0.0d) >= 0) {
            gauges.put("system.load.average", new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return mxBean.getSystemLoadAverage();
                }
            });
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("System Load Average is not available as an Operating System Metric");
            }
        }

        Gauge<Long> openFileDescriptorCountGauge = getLongGauge("getOpenFileDescriptorCount");
        if (openFileDescriptorCountGauge != null) {
            gauges.put("file.descriptor.open.count", openFileDescriptorCountGauge);
        }

        Gauge<Long> maxFileDescriptorCountGauge = getLongGauge("getMaxFileDescriptorCount");
        if (maxFileDescriptorCountGauge != null) {
            gauges.put("file.descriptor.max.count", maxFileDescriptorCountGauge);
        }

        Gauge<Double> processCpuLoadGauge = getDoubleGauge("getProcessCpuLoad");
        if (processCpuLoadGauge != null) {
            gauges.put("cpu.load.process", processCpuLoadGauge);
        }

        Gauge<Double> systemCpuLoadGauge = getDoubleGauge("getSystemCpuLoad");
        if (systemCpuLoadGauge != null) {
            gauges.put("cpu.load.system", systemCpuLoadGauge);
        }

        Gauge<Long> freePhysicalMemorySizeGauge = getLongGauge("getFreePhysicalMemorySize");
        if (freePhysicalMemorySizeGauge != null) {
            gauges.put("physical.memory.free.size", freePhysicalMemorySizeGauge);
        }

        Gauge<Long> totalPhysicalMemorySizeGauge = getLongGauge("getTotalPhysicalMemorySize");
        if (totalPhysicalMemorySizeGauge != null) {
            gauges.put("physical.memory.total.size", totalPhysicalMemorySizeGauge);
        }

        Gauge<Long> freeSwapSpaceSizeGauge = getLongGauge("getFreeSwapSpaceSize");
        if (freeSwapSpaceSizeGauge != null) {
            gauges.put("swap.space.free.size", freeSwapSpaceSizeGauge);
        }

        Gauge<Long> totalSwapSpaceSizeGauge = getLongGauge("getTotalSwapSpaceSize");
        if (totalSwapSpaceSizeGauge != null) {
            gauges.put("swap.space.total.size", totalSwapSpaceSizeGauge);
        }

        Gauge<Long> committedVirtualMemorySizeGauge = getLongGauge("getCommittedVirtualMemorySize");
        if (committedVirtualMemorySizeGauge != null) {
            gauges.put("virtual.memory.committed.size", committedVirtualMemorySizeGauge);
        }

        return Collections.unmodifiableMap(gauges);
    }

    private Gauge<Long> getLongGauge(final String methodName) {
        Object value = null;
        try {
            value = invokeMethod(methodName);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // Ignore
            if (logger.isTraceEnabled()) {
                logger.trace(String.format("Error when invoking %s", methodName), e);
            }
        }
        if (value != null) {
            // Method is working
            return new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return invokeLong(methodName);
                }
            };
        }
        return null;
    }

    private Gauge<Double> getDoubleGauge(final String methodName) {
        Object value = null;
        try {
            value = invokeMethod(methodName);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // Ignore
            if (logger.isTraceEnabled()) {
                logger.trace(String.format("Error when invoking %s", methodName), e);
            }
        }
        if (value != null) {
            // Method is working
            return new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return invokeDouble(methodName);
                }
            };
        }
        return null;
    }

    private Object invokeMethod(String methodName) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Method method = mxBean.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(mxBean);
    }

    private long invokeLong(String methodName) {
        try {
            return (Long) invokeMethod(methodName);
        } catch (NoSuchMethodException e) {
            return 0L;
        } catch (IllegalAccessException e) {
            return 0L;
        } catch (InvocationTargetException e) {
            return 0L;
        }
    }

    private double invokeDouble(String methodName) {
        try {
            return (Double) invokeMethod(methodName);
        } catch (NoSuchMethodException e) {
            return -1.0;
        } catch (IllegalAccessException e) {
            return -1.0;
        } catch (InvocationTargetException e) {
            return -1.0;
        }
    }
}
