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
import org.wso2.carbon.metrics.core.reporter.ScheduledReporter;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A scheduled reporter for writing metrics data to a CSV file
 */
public class CsvReporter extends AbstractReporter implements ScheduledReporter {

    private final MetricRegistry metricRegistry;

    private final MetricFilter metricFilter;

    private final File directory;

    private final long pollingPeriod;

    private com.codahale.metrics.CsvReporter csvReporter;

    public CsvReporter(String name, MetricRegistry metricRegistry, MetricFilter metricFilter, File directory,
                       long pollingPeriod) {
        super(name);
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.directory = directory;
        this.pollingPeriod = pollingPeriod;
    }

    @Override
    public void report() {
        if (csvReporter != null) {
            csvReporter.report();
        }
    }

    @Override
    public void startReporter() {
        csvReporter = com.codahale.metrics.CsvReporter.forRegistry(metricRegistry).formatFor(Locale.US).
                filter(metricFilter).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(directory);
        csvReporter.start(pollingPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void stopReporter() {
        if (csvReporter != null) {
            csvReporter.stop();
            csvReporter = null;
        }
    }

}
