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
package org.wso2.carbon.metrics.sample.service.internal;

import org.wso2.carbon.metrics.core.Counter;
import org.wso2.carbon.metrics.core.Histogram;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.carbon.metrics.sample.service.RandomNumberService;

import java.util.Random;

import static org.wso2.carbon.metrics.core.MetricService.name;

/**
 * An implementation of {@link RandomNumberService} to get some random numbers.
 */
public class RandomNumberServiceImpl implements RandomNumberService {

    private final Random random = new Random();

    private final MetricService metricService;

    private final Counter counter;

    private final Meter meter;

    private final Timer timer;

    private final Histogram histogram;

    private int randomNumberOrigin;

    private int randomNumberBound;

    public RandomNumberServiceImpl() {
        initialize();
        metricService = DataHolder.getInstance().getMetricService();
        // Create Gauges
        metricService.gauge(name(RandomNumberService.class, "random", "number", "origin"), Level.INFO,
                () -> randomNumberOrigin);
        metricService.gauge(name(RandomNumberService.class, "random", "number", "bound"), Level.INFO,
                () -> randomNumberBound);
        // Create Counter
        counter = metricService.counter(name(RandomNumberService.class, "sleep", "concurrent", "count"), Level.INFO);
        // Create Meter
        meter = metricService.meter(name(RandomNumberService.class, "sleep", "invocations"), Level.INFO);
        // Create Timer
        timer = metricService.timer(name(RandomNumberService.class, "random", "numbers", "generate", "time"),
                Level.INFO);
        // Create Histogram
        histogram = metricService.histogram(name(RandomNumberService.class, "random", "numbers", "size"), Level.INFO);
    }

    private void initialize() {
        randomNumberOrigin = random.nextInt(1000001);
        randomNumberBound = randomNumberOrigin + random.nextInt(1000001);
    }

    @Override
    public void setRandomNumberOriginAndBound() {
        counter.inc();
        meter.mark();
        try {
            initialize();
            Thread.sleep(random.nextInt(20001));
        } catch (InterruptedException e) {
            // Ignore
        } finally {
            counter.dec();
        }
    }

    @Override
    public int[] getRandomNumbers() {
        Timer.Context context = timer.start();
        try {
            int[] numbers = random.ints(random.nextInt(1000001), randomNumberOrigin, randomNumberBound).toArray();
            histogram.update(numbers.length);
            return numbers;
        } finally {
            context.stop();
        }
    }

}
