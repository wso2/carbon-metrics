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
package org.wso2.carbon.metrics.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Count
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Counted {
    /**
     * @return The name of the counter.
     */
    String name() default "";

    /**
     * @return The {@link Level} for the metric
     */
    Level level() default Level.INFO;

    /**
     * @return If {@code true}, use the given name as an absolute name. If {@code false}, use the given name relative to
     * the annotated class
     */
    boolean absolute() default false;

    /**
     * @return If {@code false} (default), the counter is decremented when the annotated method returns, counting
     * current invocations of the annotated method. If {@code true}, the counter increases monotonically, counting total
     * invocations of the annotated method.
     */
    boolean monotonic() default false;
}
