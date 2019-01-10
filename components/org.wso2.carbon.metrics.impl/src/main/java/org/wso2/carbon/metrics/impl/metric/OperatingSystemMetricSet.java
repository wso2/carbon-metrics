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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.sun.management.UnixOperatingSystemMXBean;

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

        double loadAverage = mxBean.getSystemLoadAverage();
        if (Double.compare(loadAverage, 0.0d) >= 0) {
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
        
        Gauge<Long> openFileDescriptorCountGauge;
        Gauge<Long> maxFileDescriptorCountGauge;
        // openFileDescriptorCountGauge and maxFileDescriptorCountGauge are only supported in UNIX OS
        try {
            openFileDescriptorCountGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return ((UnixOperatingSystemMXBean) mxBean).getOpenFileDescriptorCount();
                }
            };
            maxFileDescriptorCountGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return ((UnixOperatingSystemMXBean) mxBean).getMaxFileDescriptorCount();
                }
            };

        } catch (ClassCastException e) {
            // Since these two properties are supported only on unix os, set the value to 0 for other OS
            openFileDescriptorCountGauge = new EmptyGaugeLong();
            maxFileDescriptorCountGauge = new EmptyGaugeLong();

        }

        gauges.put("file.descriptor.open.count", openFileDescriptorCountGauge);
        if (logger.isDebugEnabled()) {
            logger.debug("file.descriptor.open.count : " + openFileDescriptorCountGauge.getValue());
        }

        gauges.put("file.descriptor.max.count", maxFileDescriptorCountGauge);
        if (logger.isDebugEnabled()) {
            logger.debug("file.descriptor.max.count : " + maxFileDescriptorCountGauge.getValue());
        }

        Gauge<Double> processCpuLoadGauge = new Gauge<Double>() {
            @Override
            public Double getValue() {
                return ((com.sun.management.OperatingSystemMXBean) mxBean).getProcessCpuLoad();
            }
        };

        gauges.put("cpu.load.process", processCpuLoadGauge);
        if (logger.isDebugEnabled()) {
            logger.debug("cpu.load.process : " + processCpuLoadGauge.getValue());
        }

        Gauge<Double> systemCpuLoadGauge = new Gauge<Double>() {
            @Override
            public Double getValue() {
                return ((com.sun.management.OperatingSystemMXBean) mxBean).getSystemCpuLoad();
            }
        };

        gauges.put("cpu.load.system", systemCpuLoadGauge);
        if (logger.isDebugEnabled()) {
            logger.debug("cpu.load.system : " + systemCpuLoadGauge.getValue());
        }

        Gauge<Long> freePhysicalMemorySizeGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return ((com.sun.management.OperatingSystemMXBean) mxBean).getFreePhysicalMemorySize();
            }
        };

        gauges.put("physical.memory.free.size", freePhysicalMemorySizeGauge);
        if (logger.isDebugEnabled()) {
            logger.debug("physical.memory.free.size : " + freePhysicalMemorySizeGauge.getValue());
        }

        Gauge<Long> totalPhysicalMemorySizeGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return ((com.sun.management.OperatingSystemMXBean) mxBean).getTotalPhysicalMemorySize();
            }
        };

        gauges.put("physical.memory.total.size", totalPhysicalMemorySizeGauge);
        if (logger.isDebugEnabled()) {
            logger.debug("physical.memory.total.size : " + totalPhysicalMemorySizeGauge.getValue());
        }

        Gauge<Long> freeSwapSpaceSizeGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return ((com.sun.management.OperatingSystemMXBean) mxBean).getFreeSwapSpaceSize();
            }
        };

        gauges.put("swap.space.free.size", freeSwapSpaceSizeGauge);
        if (logger.isDebugEnabled()) {
            logger.debug("swap.space.free.size : " + freeSwapSpaceSizeGauge.getValue());
        }

        Gauge<Long> totalSwapSpaceSizeGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return ((com.sun.management.OperatingSystemMXBean) mxBean).getTotalSwapSpaceSize();
            }
        };

        gauges.put("swap.space.total.size", totalSwapSpaceSizeGauge);
        if (logger.isDebugEnabled()) {
            logger.debug("swap.space.total.size : " + totalSwapSpaceSizeGauge.getValue());
        }

        Gauge<Long> committedVirtualMemorySizeGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return ((com.sun.management.OperatingSystemMXBean) mxBean).getCommittedVirtualMemorySize();
            }
        };

        gauges.put("virtual.memory.committed.size", committedVirtualMemorySizeGauge);
        if (logger.isDebugEnabled()) {
            logger.debug("virtual.memory.committed.size : " + committedVirtualMemorySizeGauge.getValue());
        }

        return Collections.unmodifiableMap(gauges);
    }

    static class EmptyGaugeLong implements Gauge{
        @Override
        public Long getValue() {
            return 0L;
        }     
    }
}
