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

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Utility methods for Metrics
 */
public class Utils {

    private Utils() {
    }

    /**
     * A utility method to provide a default source value
     *
     * @return The host name, if it is available, otherwise "Carbon"
     */
    public static String getDefaultSource() {
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

    public static Optional<File> getConfigFile(final String key, final String fileName) {
        // Check System Property first. If not found, just find the file in the root directory.
        File file = new File(System.getProperty(key, fileName));
        if (!file.exists()) {
            // Try to find the config file in the Carbon Conf Directory
            file = new File(org.wso2.carbon.kernel.utils.Utils.getCarbonConfigHome().resolve(fileName).toString());
        }

        return file.exists() && file.isFile() ? Optional.of(file) : Optional.empty();
    }


}
