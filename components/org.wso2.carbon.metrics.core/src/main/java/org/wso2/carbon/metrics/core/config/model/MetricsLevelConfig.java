/*
 * Copyright 2017 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.core.config.model;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;
import org.wso2.carbon.metrics.core.Level;

import java.util.Map;
import java.util.TreeMap;

/**
 * Configurations for each metric level
 */
@Configuration(description = "Metrics Levels are organized from most specific to least:\n" +
        "OFF (most specific, no metrics)\n" +
        "INFO\n" +
        "DEBUG\n" +
        "TRACE (least specific, a lot of data)\n" +
        "ALL (least specific, all data)")
public class MetricsLevelConfig {

    @Element(description = "The root level configured for Metrics")
    private String rootLevel = Level.INFO.name();

    @Element(description = "Metric Levels")
    private Map<String, String> levels = new TreeMap<>();

    public MetricsLevelConfig() {
        //JVM's direct and mapped buffer pools
        levels.put("jvm.buffers", Level.OFF.name());
        //Class Loading
        levels.put("jvm.class-loading", Level.INFO.name());
        //GC
        levels.put("jvm.gc", Level.DEBUG.name());
        //Memory
        levels.put("jvm.memory", Level.INFO.name());
        levels.put("jvm.memory.heap", Level.INFO.name());
        levels.put("jvm.memory.non-heap", Level.INFO.name());
        levels.put("jvm.memory.total", Level.INFO.name());
        levels.put("jvm.memory.pools", Level.OFF.name());
        //OS. Load Average, CPU Load etc
        levels.put("jvm.os", Level.INFO.name());
        //Threads
        levels.put("jvm.threads", Level.OFF.name());
        levels.put("jvm.threads.count", Level.DEBUG.name());
        levels.put("jvm.threads.daemon.count", Level.DEBUG.name());
        levels.put("jvm.threads.blocked.count", Level.OFF.name());
        levels.put("jvm.threads.deadlock.count", Level.OFF.name());
        levels.put("jvm.threads.new.count", Level.OFF.name());
        levels.put("jvm.threads.runnable.count", Level.OFF.name());
        levels.put("jvm.threads.terminated.count", Level.OFF.name());
        levels.put("jvm.threads.timed_waiting.count", Level.OFF.name());
        levels.put("jvm.threads.waiting.count", Level.OFF.name());
    }

    public String getRootLevel() {
        return rootLevel;
    }

    public void setRootLevel(String rootLevel) {
        this.rootLevel = rootLevel;
    }

    public Map<String, String> getLevels() {
        return levels;
    }

    public void setLevels(Map<String, String> levels) {
        this.levels = levels;
    }
}
