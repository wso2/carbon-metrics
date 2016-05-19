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
package org.wso2.carbon.metrics.core.jmx;

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
     * @param name The name of the Metric
     * @return The Level for the given metric name
     */
    String getLevel(String name);

    /**
     * Set a new level to the given metric name
     *
     * @param name  The name of the Metric
     * @param level New Level for the Metric
     */
    void setLevel(String name, String level);

    /**
     * @return Get the current configured root level
     */
    String getRootLevel();

    /**
     * Set root level for Metric Service
     *
     * @param level The new level to be used as the root level
     */
    void setRootLevel(String level);

    /**
     * Invoke report method of all scheduled reporters.
     */
    void report();


    /**
     * Start the reporter with given name
     *
     * @param name The name of the reporter
     */
    void startReporter(String name);

    /**
     * Stop the reporter with given name
     *
     * @param name The name of the reporter
     */
    void stopReporter(String name);

    /**
     * Check whether the reporter with given name is running
     *
     * @param name The name of the reporter
     * @return {@code true} if the reporter is started, otherwise {@code false}
     */
    boolean isReporterRunning(String name);

    /**
     * Start all reporters
     */
    void startReporters();

    /**
     * Stop all reporters
     */
    void stopReporters();
}
