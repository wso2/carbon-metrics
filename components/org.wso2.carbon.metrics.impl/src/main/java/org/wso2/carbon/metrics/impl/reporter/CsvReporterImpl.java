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

import com.codahale.metrics.CsvReporter;

public class CsvReporterImpl extends AbstractReporter {

    private final CsvReporter csvReporter;

    private final long pollingPeriod;

    public CsvReporterImpl(CsvReporter csvReporter, long pollingPeriod) {
        super("CSV");
        this.csvReporter = csvReporter;
        this.pollingPeriod = pollingPeriod;
    }

    @Override
    public void report() {
        csvReporter.report();
    }

    @Override
    public void startReporter() {
        csvReporter.start(pollingPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void stopReporter() {
        csvReporter.stop();
    }

}
