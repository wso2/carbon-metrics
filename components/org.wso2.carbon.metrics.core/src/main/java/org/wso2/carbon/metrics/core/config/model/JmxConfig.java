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

/**
 * JMX Configuration for Metrics
 */
public class JmxConfig {

    private boolean registerMBean = true;

    private String name = "org.wso2.carbon:type=Metrics";

    public boolean isRegisterMBean() {
        return registerMBean;
    }

    public void setRegisterMBean(boolean registerMBean) {
        this.registerMBean = registerMBean;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
