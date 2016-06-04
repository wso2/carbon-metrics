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

import org.wso2.carbon.metrics.core.annotation.Counted;
import org.wso2.carbon.metrics.core.annotation.Metered;
import org.wso2.carbon.metrics.core.annotation.Timed;

import java.lang.reflect.Method;

/**
 * MetricAnnotation is a utility class to get or create metrics from the Metric Annotations
 */
public final class MetricAnnotation {

    private MetricAnnotation() {
    }

    private static String buildName(String explicitName, boolean absolute, Method method) {
        if (explicitName != null && !explicitName.isEmpty()) {
            if (absolute) {
                return explicitName;
            }
            return MetricService.name(method.getDeclaringClass().getName(), method.getName(), explicitName);
        }
        return MetricService.name(method.getDeclaringClass().getName(), method.getName());
    }

    private static Level toLevel(org.wso2.carbon.metrics.core.annotation.Level level) {
        Level returnLevel = Level.INFO;
        switch (level) {
            case OFF:
                returnLevel = Level.OFF;
                break;
            case INFO:
                returnLevel = Level.INFO;
                break;
            case DEBUG:
                returnLevel = Level.DEBUG;
                break;
            case TRACE:
                returnLevel = Level.TRACE;
                break;
            case ALL:
                returnLevel = Level.ALL;
                break;
        }
        return returnLevel;
    }

    public static Counter counter(MetricService metricService, Counted annotation, Method method) {
        return metricService.counter(buildName(annotation.name(), annotation.absolute(), method),
                toLevel(annotation.level()));
    }

    public static Meter meter(MetricService metricService, Metered annotation, Method method) {
        return metricService.meter(buildName(annotation.name(), annotation.absolute(), method),
                toLevel(annotation.level()));
    }

    public static Timer timer(MetricService metricService, Timed annotation, Method method) {
        return metricService.timer(buildName(annotation.name(), annotation.absolute(), method),
                toLevel(annotation.level()));
    }

}
