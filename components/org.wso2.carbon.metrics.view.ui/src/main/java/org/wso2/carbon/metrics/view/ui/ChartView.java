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
package org.wso2.carbon.metrics.view.ui;

/**
 * Chart view for a set of charts
 */
public class ChartView {

    private final boolean visible;
    private final String[] charts;

    public ChartView(final boolean visible, final String[] charts) {
        this.visible = visible;
        this.charts = charts;
    }

    public boolean isVisible() {
        return visible;
    }

    public String[] getCharts() {
        return charts;
    }
}
