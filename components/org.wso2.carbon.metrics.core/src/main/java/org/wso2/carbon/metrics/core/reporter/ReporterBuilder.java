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
package org.wso2.carbon.metrics.core.reporter;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

import java.util.Optional;

/**
 * Build a reporter for Metrics.
 *
 * @param <T> The {@link Reporter} type
 */
public interface ReporterBuilder<T extends Reporter> {

    /**
     * Build a {@link Reporter}
     *
     * @param metricRegistry The Metric Registry used by the Metric Service
     * @param metricFilter   The Metric filter for filtering enabled metrics
     * @return An {@link Optional} {@link Reporter}.
     * @throws ReporterBuildException Throws when required parameters are not found or when the reporter is failed to
     *                                build.
     */
    Optional<T> build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterBuildException;

}
