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
package org.wso2.carbon.metrics.impl.task;

import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The abstract base class for scheduled tasks
 */
public abstract class ScheduledTask implements Closeable, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTask.class);

    /**
     * A simple named thread factory.
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private NamedThreadFactory(String name) {
            final SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = name + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    private static final AtomicInteger FACTORY_ID = new AtomicInteger();

    private final ScheduledExecutorService executor;

    /**
     * Creates a new {@link ScheduledTask} instance.
     *
     * @param name the task's name
     */
    protected ScheduledTask(String name) {
        this(Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name + '-'
                + FACTORY_ID.incrementAndGet())));
    }

    /**
     * Creates a new {@link ScheduledTask} instance.
     *
     * @param executor the executor to use while scheduling tasks.
     */
    protected ScheduledTask(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Starts the tasks with the given period.
     *
     * @param period the amount of time between each run
     * @param unit the unit for {@code period}
     */
    public void start(long period, TimeUnit unit) {
        executor.scheduleAtFixedRate(this, period, period, unit);
    }

    /**
     * Stops the task and shuts down its thread of execution. Uses the shutdown pattern from
     * http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
     */
    public void stop() {
        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    LOGGER.error(getClass().getSimpleName() + ": ScheduledExecutorService did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stops the task and shuts down its thread of execution.
     */
    @Override
    public void close() {
        stop();
    }
}
