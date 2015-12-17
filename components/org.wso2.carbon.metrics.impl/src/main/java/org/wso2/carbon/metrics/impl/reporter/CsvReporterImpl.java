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
package org.wso2.carbon.metrics.impl.reporter;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class CsvReporterImpl extends AbstractReporter implements ScheduledReporter {

    private static final Logger logger = LoggerFactory.getLogger(CsvReporterImpl.class);

    private final MetricRegistry metricRegistry;

    private final MetricFilter metricFilter;

    private final File directory;

    private final long pollingPeriod;

    private CsvReporter csvReporter;

    public CsvReporterImpl(MetricRegistry metricRegistry, MetricFilter metricFilter, File directory,
            long pollingPeriod) {
        super("CSV");
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
        csvReporter = CsvReporter.forRegistry(metricRegistry).formatFor(Locale.US).filter(metricFilter)
                .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build(directory);
        csvReporter.start(pollingPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void stopReporter() {
        try {
            csvReporter.stop();
            csvReporter = null;
        } catch (Throwable e) {
            logger.error("An error occurred when trying to stop the reporter", e);
        }
    }

}
