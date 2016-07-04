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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.task.ScheduledTask;

import java.util.concurrent.TimeUnit;

/**
 * Test {@link ScheduledTask}
 */
public class ScheduledTaskTest {

    private long counter;

    private class TestTask extends ScheduledTask {

        protected TestTask(String name) {
            super(name);
        }

        @Override
        public void run() {
            counter++;
        }
    }

    private TestTask testTask;

    @BeforeClass
    private void start() {
        testTask = new TestTask("Test");
        testTask.start(1, TimeUnit.NANOSECONDS);
    }

    @AfterClass
    private void stop() {
        testTask.close();
    }

    @Test
    public void testScheduledTask() throws InterruptedException {
        Thread.sleep(5);
        Assert.assertTrue(counter > 0);
    }
}
