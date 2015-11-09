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


/**
 * Interface for JMX Managed Metric Manager Bean
 */
public interface MetricManagerMXBean {

    /**
     * Enables the Metrics Feature
     */
    void enable();

    /**
     * Disables the Metrics Feature
     */
    void disable();

    /**
     * Get the current status of Metrics (Enabled/Disabled)
     * 
     * @return {@code true} if the Metrics feature is enabled
     */
    boolean isEnabled();

    /**
     * @return The number of metrics used
     */
    int getMetricsCount();

    /**
     * @return The Level for the given metric name
     */
    String getMetricLevel(String name, String statName);

    /**
     * Set a new level to the given metric name
     * 
     * @param name The name of the Metric
     * @param level New Level for the Metric
     */
    void setMetricLevel(String name, String statName, String level);

    /**
     * @return Get the current configured root level
     */
    String getRootLevel();

    /**
     * Set root level for Metric Service
     * 
     * @param level
     */
    void setRootLevel(String level);

    /**
     * Invoke report method of all scheduled reporters.
     */
    void report();
}
