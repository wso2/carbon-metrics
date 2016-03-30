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
package org.wso2.carbon.metrics.core.reporter.impl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.reporter.ScheduledReporter;

import java.util.concurrent.TimeUnit;

/**
 * A reporter which outputs measurements to console
 */
public class ConsoleReporter extends AbstractReporter implements ScheduledReporter {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleReporter.class);

    private final MetricRegistry metricRegistry;

    private final MetricFilter metricFilter;

    private final long pollingPeriod;

    private com.codahale.metrics.ConsoleReporter consoleReporter;

    public ConsoleReporter(MetricRegistry metricRegistry, MetricFilter metricFilter, long pollingPeriod) {
        super("Console");
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.pollingPeriod = pollingPeriod;
    }

    @Override
    public void report() {
        if (consoleReporter != null) {
            consoleReporter.report();
        }
    }

    @Override
    public void startReporter() {
        consoleReporter = com.codahale.metrics.ConsoleReporter.forRegistry(metricRegistry).filter(metricFilter)
                .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
        consoleReporter.start(pollingPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void stopReporter() {
        try {
            consoleReporter.stop();
            consoleReporter = null;
        } catch (Throwable e) {
            logger.error("An error occurred when trying to stop the reporter", e);
        }
    }

}
