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
package org.wso2.carbon.metrics.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * A TestNG listener to log all methods.
 * The Metric Reporters will output a lot of data and the logs in this listener will help to identify the test method.
 */
public class TestMethodListener implements IInvokedMethodListener {

    private static final Logger logger = LoggerFactory.getLogger(TestMethodListener.class);

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            logger.info("Test Method: {}", method.toString());
        } else if (method.isConfigurationMethod()) {
            logger.info("Configuration Method: {}", method.toString());
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    }

}
