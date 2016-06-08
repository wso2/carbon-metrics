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
package org.wso2.carbon.metrics.sample.consumer.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.task.ScheduledTask;
import org.wso2.carbon.metrics.sample.service.RandomNumberService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Service component to refer the sample service
 */
@Component(
        name = "org.wso2.carbon.metrics.sample.consumer.internal.ServiceComponent",
        immediate = true
)
public class ServiceComponent {

    private static final Logger logger = LoggerFactory.getLogger(ServiceComponent.class);

    private RandomNumberService randomNumberService;

    private ExecutorService executorService;

    private ScheduledTask getRandomNumbersTask;

    private ScheduledTask setRandomNumberOriginAndBoundTask;

    /**
     * This is the activation method of ServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     */
    @Activate
    protected void activate(BundleContext bundleContext) {
        logger.info("Service Component is activated");

        executorService = Executors.newCachedThreadPool();

        getRandomNumbersTask = new ScheduledTask("Get-Random-Numbers") {

            private Future<?> future;

            @Override
            public void run() {
                if (future != null && !future.isDone() && logger.isDebugEnabled()) {
                    logger.debug("Previous task is not done");
                }
                future = executorService.submit(() -> randomNumberService.getRandomNumbers());
            }
        };
        getRandomNumbersTask.start(1, TimeUnit.SECONDS);

        setRandomNumberOriginAndBoundTask = new ScheduledTask("Set-Random-Number-Origin-And-Bound") {

            private Future<?> future;

            @Override
            public void run() {
                if (future != null && !future.isDone() && logger.isDebugEnabled()) {
                    logger.debug("Previous task is not done");
                }
                future = executorService.submit(() -> randomNumberService.setRandomNumberOriginAndBound());
            }
        };
        setRandomNumberOriginAndBoundTask.start(5, TimeUnit.SECONDS);
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are not satisfied during runtime.
     */
    @Deactivate
    protected void deactivate() {
        logger.info("Service Component is deactivated");
        getRandomNumbersTask.stop();
        setRandomNumberOriginAndBoundTask.stop();
        executorService.shutdownNow();
    }

    /**
     * This bind method will be called when {@link RandomNumberService} is registered.
     *
     * @param randomNumberService The {@link RandomNumberService} instance registered as an OSGi service
     */
    @Reference(
            name = "random.number.service",
            service = RandomNumberService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRandomNumberService"
    )
    protected void setRandomNumberService(RandomNumberService randomNumberService) {
        this.randomNumberService = randomNumberService;
    }

    /**
     * This is the unbind method which gets called at the un-registration of {@link RandomNumberService}
     *
     * @param randomNumberService The {@link RandomNumberService} instance registered as an OSGi service
     */
    protected void unsetRandomNumberService(RandomNumberService randomNumberService) {
    }

}
