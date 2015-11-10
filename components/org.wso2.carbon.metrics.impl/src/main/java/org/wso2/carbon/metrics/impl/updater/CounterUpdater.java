/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.wso2.carbon.metrics.impl.updater;

/**
 * An incrementing and decrementing counter metric.
 */
public interface CounterUpdater extends MetricUpdater {

    /**
     * Increment the counter by one.
     */
    void incSelf();

    /**
     * Increment the counter by {@code n}.
     *
     * @param n the amount by which the counter will be increased
     */
    void incSelf(long n);

    /**
     * Decrement the counter by one.
     */
    void decSelf();

    /**
     * Decrement the counter by {@code n}.
     *
     * @param n the amount by which the counter will be decreased
     */
    void decSelf(long n);
}
