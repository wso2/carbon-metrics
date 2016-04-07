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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.impl.MetricService;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Utility methods for Metrics
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static final String MBEAN_NAME = "org.wso2.carbon:type=MetricManager";

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

        return file.exists() ? Optional.of(file) : Optional.empty();
    }


    public static void registerMXBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = new ObjectName(MBEAN_NAME);
            if (mBeanServer.isRegistered(name)) {
                mBeanServer.unregisterMBean(name);
            }
            mBeanServer.registerMBean(MetricService.getInstance(), name);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("MetricManagerMXBean registered under name: %s", name));
            }
        } catch (JMException e) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("MetricManagerMXBean registration failed. Name: %s", MBEAN_NAME), e);
            }
        }
    }

    public static void unregisterMXBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = new ObjectName(MBEAN_NAME);
            if (mBeanServer.isRegistered(name)) {
                mBeanServer.unregisterMBean(name);
            }
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("MetricManagerMXBean with name '%s' was unregistered.", name));
            }
        } catch (JMException e) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("MetricManagerMXBean with name '%s' was failed to unregister", MBEAN_NAME),
                        e);
            }
        }
    }
}
