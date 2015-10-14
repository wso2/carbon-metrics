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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.das.reporter.DASReporter;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class DASReporterImpl extends AbstractReporter implements ScheduledReporter {

    private static final Logger logger = LoggerFactory.getLogger(DASReporterImpl.class);

    private final MetricRegistry metricRegistry;

    private final MetricFilter metricFilter;

    private final String source;

    private final String type;
    private final String receiverURL;
    private final String authURL;
    private final String username;
    private final String password;

    private DASReporter dasReporter;

    private final long pollingPeriod;

    public DASReporterImpl(MetricRegistry metricRegistry, MetricFilter metricFilter, String source, String type,
            String receiverURL, String authURL, String username, String password, long pollingPeriod) {
        super("JDBC");
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.source = source;
        this.type = type;
        this.receiverURL = receiverURL;
        this.authURL = authURL;
        this.username = username;
        this.password = password;
        this.pollingPeriod = pollingPeriod;
    }

    @Override
    public void report() {
        if (dasReporter != null) {
            dasReporter.report();
        }
    }

    @Override
    public void startReporter() {
        dasReporter = DASReporter.forRegistry(metricRegistry).filter(metricFilter).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(source, type, receiverURL, authURL, username, password);
        dasReporter.start(pollingPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void stopReporter() {
        try {
            dasReporter.stop();
            dasReporter = null;
        } catch (Throwable e) {
            logger.error("An error occurred when trying to stop the reporter", e);
        }
    }
}
