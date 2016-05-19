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

import java.util.concurrent.TimeUnit;

/**
 * A scheduled reporter for Data Analytics Server (DAS)
 */
public class DasReporter extends AbstractReporter implements ScheduledReporter {

    private final MetricRegistry metricRegistry;

    private final MetricFilter metricFilter;

    private final String source;

    private final String type;
    private final String receiverURL;
    private final String authURL;
    private final String username;
    private final String password;
    private final String dataAgentConfigPath;

    private org.wso2.carbon.metrics.das.reporter.DasReporter dasReporter;

    private final long pollingPeriod;

    public DasReporter(String name, MetricRegistry metricRegistry, MetricFilter metricFilter, String source,
                       String type, String receiverURL, String authURL, String username, String password,
                       String dataAgentConfigPath, long pollingPeriod) {
        super(name);
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.source = source;
        this.type = type;
        this.receiverURL = receiverURL;
        this.authURL = authURL;
        this.username = username;
        this.password = password;
        this.dataAgentConfigPath = dataAgentConfigPath;
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
        dasReporter = org.wso2.carbon.metrics.das.reporter.DasReporter.forRegistry(metricRegistry).filter(metricFilter)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(source, type, receiverURL, authURL, username, password, dataAgentConfigPath);
        dasReporter.start(pollingPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void stopReporter() {
        if (dasReporter != null) {
            dasReporter.stop();
            dasReporter = null;
        }
    }
}
