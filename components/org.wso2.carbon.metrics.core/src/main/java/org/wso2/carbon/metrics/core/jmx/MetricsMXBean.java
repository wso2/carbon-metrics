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
public interface MetricsMXBean {

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
     * Return the number of metrics used
     *
     * @return The metrics count
     */
    long getMetricsCount();

    /**
     * Return the number of enabled metrics used
     *
     * @return The enabled metrics count
     */
    long getEnabledMetricsCount();

    /**
     * Return the number of metric collections used
     *
     * @return The metric collections count
     */
    long getMetricCollectionsCount();

    /**
     * @param name The name of the Metric
     * @return The Level for the given metric name
     */
    String getMetricLevel(String name);

    /**
     * Set a new level to the given metric name
     *
     * @param name  The name of the Metric
     * @param level New Level for the Metric
     */
    void setMetricLevel(String name, String level);

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
     * Invoke report method of the scheduled reporter with given name
     *
     * @param name The name of the reporter
     */
    void report(String name);

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

    /**
     * Get the default source used for the reporters
     *
     * @return The default source used for the reporters
     */
    String getDefaultSource();
}
