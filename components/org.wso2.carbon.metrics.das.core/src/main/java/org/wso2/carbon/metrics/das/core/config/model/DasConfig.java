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
package org.wso2.carbon.metrics.das.core.config.model;

import org.wso2.carbon.config.annotation.Element;

/**
 * Configuration for connecting with Data Analytics Server (DAS).
 */
public class DasConfig {

    @Element(description = "The type used with Data Publisher")
    private String type = "thrift";

    @Element(description = "Data Receiver URL used by the Data Publisher")
    private String receiverURL = "tcp://localhost:7611";

    @Element(description = "Authentication URL for the Data Publisher. eg: ssl://localhost:7711")
    private String authURL;

    private String username = "admin";

    private String password = "admin";

    @Element(description = "The path for Data Bridge Agent configuration")
    private String dataAgentConfigPath = "${carbon.home}/conf/data-bridge/data-agent-config.xml";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReceiverURL() {
        return receiverURL;
    }

    public void setReceiverURL(String receiverURL) {
        this.receiverURL = receiverURL;
    }

    public String getAuthURL() {
        return authURL;
    }

    public void setAuthURL(String authURL) {
        this.authURL = authURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDataAgentConfigPath() {
        return dataAgentConfigPath;
    }

    public void setDataAgentConfigPath(String dataAgentConfigPath) {
        this.dataAgentConfigPath = dataAgentConfigPath;
    }


}
