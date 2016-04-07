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
package org.wso2.carbon.metrics.core.reporter.impl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.wso2.carbon.metrics.core.reporter.ScheduledReporter;

import java.util.concurrent.TimeUnit;

/**
 * A scheduled reporter for logging Metrics values to a SLF4J {@link Logger}
 */
public class Slf4jReporter extends AbstractReporter implements ScheduledReporter {

    private static final Logger logger = LoggerFactory.getLogger(Slf4jReporter.class);

    private final MetricRegistry metricRegistry;

    private final MetricFilter metricFilter;

    private final String loggerName;

    private final String markerName;

    private final long pollingPeriod;

    private com.codahale.metrics.Slf4jReporter slf4jReporter;

    public Slf4jReporter(MetricRegistry metricRegistry, MetricFilter metricFilter, String loggerName, String markerName,
                         long pollingPeriod) {
        super("SLF4J");
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.loggerName = loggerName;
        this.markerName = markerName;
        this.pollingPeriod = pollingPeriod;
    }

    @Override
    public void report() {
        if (slf4jReporter != null) {
            slf4jReporter.report();
        }
    }

    @Override
    public void startReporter() {
        final com.codahale.metrics.Slf4jReporter.Builder builder = com.codahale.metrics.Slf4jReporter
                .forRegistry(metricRegistry).filter(metricFilter).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS).outputTo(LoggerFactory.getLogger(loggerName));
        if (markerName != null) {
            builder.markWith(MarkerFactory.getMarker(markerName));
        }
        slf4jReporter = builder.build();
        slf4jReporter.start(pollingPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void stopReporter() {
        try {
            slf4jReporter.stop();
            slf4jReporter = null;
        } catch (Throwable e) {
            logger.error("An error occurred when trying to stop the reporter", e);
        }
    }
}
