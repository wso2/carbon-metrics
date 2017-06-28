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
package org.wso2.carbon.metrics.core;

import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;

/**
 * The APIs to manage metrics feature enabled status, root level, metric levels and reporters.
 */
public interface MetricManagementService {

    /**
     * Enables the Metrics Feature.
     */
    void enable();

    /**
     * Disables the Metrics Feature.
     */
    void disable();

    /**
     * Get the current enabled status of Metrics.
     *
     * @return {@code true} if the Metrics feature is enabled
     */
    boolean isEnabled();

    /**
     * Get the root level configured in the service.
     *
     * @return The Root {@link Level}
     */
    Level getRootLevel();

    /**
     * Set a new root level to the Metric Service.
     *
     * @param level New Root {@link Level}
     */
    void setRootLevel(Level level);

    /**
     * Return the number of metrics used.
     *
     * @return The metrics count
     */
    long getMetricsCount();

    /**
     * Return the number of enabled metrics used.
     *
     * @return The enabled metrics count
     */
    long getEnabledMetricsCount();

    /**
     * Return the number of metric collections used.
     *
     * @return The metric collections count
     */
    long getMetricCollectionsCount();

    /**
     * Set a new level to the given metric.
     *
     * @param name  The name of the Metric
     * @param level New {@link Level} for the Metric
     */
    void setMetricLevel(String name, Level level);

    /**
     * Get the {@link Level} for a given metric.
     *
     * @param name The name of the Metric
     * @return {@link Level} for the given metric
     */
    Level getMetricLevel(String name);

    /**
     * Add a new reporter.
     *
     * @param reporterBuilder The {@link ReporterBuilder} to create a new reporter.
     * @param <T>             The type of the {@link ReporterBuilder}
     * @throws ReporterBuildException when the reporter build fails
     */
    <T extends ReporterBuilder> void addReporter(T reporterBuilder) throws ReporterBuildException;

    /**
     * Remove the reporter with given name.
     *
     * @param name The name of the reporter
     * @return {@code true} if the reporter is removed successfully, otherwise {@code false}
     */
    boolean removeReporter(String name);

    /**
     * Invoke report method of all scheduled reporters.
     */
    void report();

    /**
     * Invoke report method the scheduled reporter with given name.
     *
     * @param name The name of the reporter
     */
    void report(String name);

    /**
     * Start the reporter with the given name.
     *
     * @param name The name of the reporter
     */
    void startReporter(String name);

    /**
     * Stop the reporter with the given name.
     *
     * @param name The name of the reporter
     */
    void stopReporter(String name);

    /**
     * Check whether the reporter with the given name is running.
     *
     * @param name The name of the reporter
     * @return {@code true} if the reporter is running
     */
    boolean isReporterRunning(String name);

    /**
     * Start all reporters.
     */
    void startReporters();

    /**
     * Stop all reporters.
     */
    void stopReporters();
}
