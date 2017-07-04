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
package org.wso2.carbon.metrics.core.config.model;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.metrics.core.impl.reservoir.ReservoirType;

/**
 * Configuration for Reservoir
 */
@Configuration(description = "Reservoir Configuration")
public class ReservoirConfig {

    @Element(description = "Reservoir Type used for Histogram and Timer\n" +
            "Available types are EXPONENTIALLY_DECAYING, UNIFORM, SLIDING_WINDOW, SLIDING_TIME_WINDOW & HDR_HISTOGRAM")
    private ReservoirType type = ReservoirType.EXPONENTIALLY_DECAYING;

    private ReservoirParametersConfig parameters = new ReservoirParametersConfig();

    public ReservoirType getType() {
        return type;
    }

    public void setType(ReservoirType type) {
        this.type = type;
    }

    public ReservoirParametersConfig getParameters() {
        return parameters;
    }

    public void setParameters(ReservoirParametersConfig parameters) {
        this.parameters = parameters;
    }
}
