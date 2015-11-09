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
package org.wso2.carbon.metrics.data.common;

/**
 * Metric
 */
public class Metric {

    /**
     * Metric Type
     */
    private String type;

    /**
     * Metric Name
     */
    private String name;

    /**
     * Metric StatName
     */
    private String statName;

    /**
     * Metric Display Name
     */
    private String displayName;

    /**
     * Metric Attribute
     */
    private String attr;

    /**
     * Metric Data Format
     */
    private String format;

    public Metric() {
    }

    public Metric(MetricType type, String name, String statName, String displayName, MetricAttribute attr, MetricDataFormat format) {
        super();
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (statName == null) {
            throw new IllegalArgumentException("StatName cannot be null");
        }
        if (displayName == null) {
            throw new IllegalArgumentException("Display Name cannot be null");
        }
        if (attr == null) {
            throw new IllegalArgumentException("Attribute cannot be null");
        }
        this.type = type.name();
        this.name = name;
        this.statName = statName;
        this.displayName = displayName;
        this.attr = attr.name();
        if (format != null) {
            this.format = format.name();
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatName() {
        return statName;
    }

    public void setStatName(String statName) {
        this.statName = statName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
