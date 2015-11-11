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

package org.wso2.carbon.metrics.manager;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by grainier on 11/11/15.
 */
public interface MetricHierarchy<T> extends Iterable<T> {
    int getLevel();
    ConcurrentMap<String, Metric> getMetrics();
}
