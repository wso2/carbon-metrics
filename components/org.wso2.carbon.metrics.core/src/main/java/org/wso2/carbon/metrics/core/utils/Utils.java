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
package org.wso2.carbon.metrics.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utility methods for Metrics
 */
public class Utils {

    private static volatile String defaultSource;

    private static volatile boolean carbonEnvironment;

    /**
     * A utility method to provide a default source value
     *
     * @return The default source, if it is available, otherwise return the hostname. If the hostname is also not
     * available, it will return "Carbon".
     */
    public static String getDefaultSource() {
        if (defaultSource == null) {
            // Use host name if available
            String hostname = null;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                // Ignore exception
            }
            if (hostname == null || hostname.trim().length() == 0) {
                defaultSource = "Carbon";
            } else {
                defaultSource = hostname;
            }
        }
        return defaultSource;
    }

    /**
     * Set the default source for the reporters
     *
     * @param defaultSource The value to be used as the default source for the reporters
     */
    public static void setDefaultSource(String defaultSource) {
        Utils.defaultSource = defaultSource;
    }

    /**
     * Check whether the Metrics is running in the Carbon Environment (OSGi)
     *
     * @return {@code true} if the Metrics is running in the Carbon Environment
     */
    public static boolean isCarbonEnvironment() {
        return carbonEnvironment;
    }

    /**
     * Set the flag used to check whether the Metrics is running in Carbon Environment (OSGi)
     *
     * @param carbonEnvironment Set {@code true} if the Metrics is running in the Carbon Environment
     */
    public static void setCarbonEnvironment(boolean carbonEnvironment) {
        Utils.carbonEnvironment = carbonEnvironment;
    }

}

