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
package org.wso2.carbon.metrics.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A utility class to provide a default source value
 */
public final class DefaultSourceValueProvider {

    private DefaultSourceValueProvider() {
    }

    public static String getValue() {
        String source;
        // Use host name if available
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // Ignore exception
        }
        if (hostname == null || hostname.trim().length() == 0) {
            source = "Carbon";
        } else {
            source = hostname;
        }
        return source;
    }

}
