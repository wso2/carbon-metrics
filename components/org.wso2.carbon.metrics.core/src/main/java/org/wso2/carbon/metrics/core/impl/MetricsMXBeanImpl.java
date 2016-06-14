/*
 * Copyright 2016 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.core.impl;

import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.internal.Utils;
import org.wso2.carbon.metrics.core.jmx.MetricsMXBean;

/**
 * MBean for {@link MetricManagementService}
 */
public class MetricsMXBeanImpl implements MetricsMXBean {

    private final MetricManagementService metricManagementService;

    public MetricsMXBeanImpl(MetricManagementService metricManagementService) {
        this.metricManagementService = metricManagementService;
    }

    @Override
    public void enable() {
        metricManagementService.enable();
    }

    @Override
    public void disable() {
        metricManagementService.disable();
    }

    @Override
    public boolean isEnabled() {
        return metricManagementService.isEnabled();
    }

    @Override
    public int getMetricsCount() {
        return metricManagementService.getMetricsCount();
    }

    public String getRootLevel() {
        return metricManagementService.getRootLevel().name();
    }

    public void setRootLevel(String level) {
        metricManagementService.setRootLevel(Level.valueOf(level));
    }

    @Override
    public String getMetricLevel(String name) {
        Level level = metricManagementService.getMetricLevel(name);
        return level != null ? level.name() : null;
    }

    @Override
    public void setMetricLevel(String name, String level) {
        metricManagementService.setMetricLevel(name, Level.valueOf(level));
    }

    @Override
    public void report() {
        metricManagementService.report();
    }

    @Override
    public void report(String name) {
        metricManagementService.report(name);
    }

    @Override
    public void startReporter(String name) {
        metricManagementService.startReporter(name);
    }

    @Override
    public void stopReporter(String name) {
        metricManagementService.stopReporter(name);
    }

    @Override
    public boolean isReporterRunning(String name) {
        return metricManagementService.isReporterRunning(name);
    }

    @Override
    public void startReporters() {
        metricManagementService.startReporters();
    }

    @Override
    public void stopReporters() {
        metricManagementService.stopReporters();
    }

    @Override
    public String getDefaultSource() {
        return Utils.getDefaultSource();
    }
}
