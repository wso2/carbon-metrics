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
package org.wso2.carbon.metrics.manager.jmx;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricService;

/**
 * Implementation for Metric Manager JMX Bean
 */
public class MetricManagerMXBeanImpl implements MetricManagerMXBean {

    private final MetricService metricService;

    public MetricManagerMXBeanImpl(MetricService metricService) {
        super();
        this.metricService = metricService;
    }

    @Override
    public void enable() {
        metricService.enable();
    }

    @Override
    public void disable() {
        metricService.disable();
    }

    @Override
    public boolean isEnabled() {
        return metricService.isEnabled();
    }

    @Override
    public int getMetricsCount() {
        return metricService.getMetricsCount();
    }

    @Override
    public String getMetricLevel(String name) {
        Level level = metricService.getMetricLevel(name);
        return level != null ? level.name() : null;
    }

    @Override
    public void setMetricLevel(String name, String level) {
        metricService.setMetricLevel(name, Level.valueOf(level));
    }

    @Override
    public String getRootLevel() {
        return metricService.getRootLevel().name();
    }

    @Override
    public void setRootLevel(String level) {
        metricService.setRootLevel(Level.valueOf(level));
    }
}
