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
package org.wso2.carbon.metrics.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.reporter.ListeningReporter;
import org.wso2.carbon.metrics.core.reporter.Reporter;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.metrics.core.reporter.ScheduledReporter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main implementation class for {@link MetricManagementService}.
 */
public class MetricManagementServiceImpl implements MetricManagementService {

    private static final Logger logger = LoggerFactory.getLogger(MetricManagementServiceImpl.class);
    private final MetricManager metricManager;
    private final Map<String, Reporter> reporterMap = new ConcurrentHashMap<>();

    public MetricManagementServiceImpl(MetricManager metricManager) {
        this.metricManager = metricManager;
        metricManager.addEnabledStatusChangeListener(enabled -> {
            if (enabled) {
                startReporters();
            } else {
                stopReporters();
            }
        });
        metricManager.addRootLevelChangeListener((oldLevel, newLevel) -> {
            restartListeningReporters();
        });
        metricManager.addMetricLevelChangeListener((metric, oldLevel, newLevel) -> {
            restartListeningReporters();
        });
    }

    @Override
    public Level getRootLevel() {
        return metricManager.getRootLevel();
    }

    @Override
    public void setRootLevel(Level level) {
        metricManager.setRootLevel(level);
    }

    @Override
    public boolean isEnabled() {
        return metricManager.isEnabled();
    }

    @Override
    public void enable() {
        metricManager.enable();
    }

    @Override
    public void disable() {
        metricManager.disable();
    }

    @Override
    public int getMetricsCount() {
        return metricManager.getMetricsCount();
    }

    @Override
    public void setMetricLevel(String name, Level level) {
        metricManager.setMetricLevel(name, level);
    }

    @Override
    public Level getMetricLevel(String name) {
        return metricManager.getMetricLevel(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ReporterBuilder> void addReporter(T reporterBuilder)
            throws ReporterBuildException {
        Optional<? extends Reporter> reporter = reporterBuilder.build(metricManager.getMetricRegistry(),
                metricManager.getEnabledMetricFilter());
        reporter.ifPresent(r -> {
            Reporter previousReporter = reporterMap.put(r.getName(), r);
            if (previousReporter != null) {
                previousReporter.stop();
            }
        });
    }

    @Override
    public boolean removeReporter(String name) {
        Reporter previousReporter = reporterMap.remove(name);
        if (previousReporter != null) {
            previousReporter.stop();
            return true;
        }
        return false;
    }

    @Override
    public void report() {
        reporterMap.values().stream().filter(reporter -> reporter instanceof ScheduledReporter)
                .forEach(reporter -> ((ScheduledReporter) reporter).report());
    }

    @Override
    public void report(String name) {
        reporterMap.values().stream().filter(reporter -> name.equals(reporter.getName()))
                .filter(reporter -> reporter instanceof ScheduledReporter)
                .forEach(reporter -> ((ScheduledReporter) reporter).report());
    }

    private Reporter getReporter(String name) {
        Reporter reporter = reporterMap.get(name);
        if (reporter == null) {
            throw new IllegalArgumentException("Invalid Reporter Name");
        }
        return reporter;
    }

    @Override
    public void startReporter(String name) {
        getReporter(name).start();
    }

    @Override
    public void stopReporter(String name) {
        getReporter(name).stop();
    }

    @Override
    public boolean isReporterRunning(String name) {
        return getReporter(name).isRunning();
    }

    @Override
    public void startReporters() {
        reporterMap.values().forEach(reporter -> {
            try {
                reporter.start();
            } catch (Throwable e) {
                logger.error("Error when starting the reporter", e);
            }
        });
    }

    @Override
    public void stopReporters() {
        reporterMap.values().forEach(reporter -> {
            try {
                reporter.stop();
            } catch (Throwable e) {
                logger.error("Error when stopping the reporter", e);
            }
        });
    }

    private void restartListeningReporters() {
        reporterMap.values().stream()
                .filter(reporter -> reporter instanceof ListeningReporter)
                .filter(Reporter::isRunning)
                .forEach(reporter -> {
                    ListeningReporter listeningReporter = (ListeningReporter) reporter;
                    listeningReporter.stop();
                    listeningReporter.start();
                });
    }

}
