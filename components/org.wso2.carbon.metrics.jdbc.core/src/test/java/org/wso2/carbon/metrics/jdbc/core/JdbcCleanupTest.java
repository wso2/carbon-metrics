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
package org.wso2.carbon.metrics.jdbc.core;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.jdbc.core.reporter.impl.ScheduledJdbcMetricsCleanupTask;
import org.wso2.carbon.metrics.jdbc.reporter.JdbcReporter;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ScheduledJdbcMetricsCleanupTask}.
 */
public class JdbcCleanupTest extends BaseReporterTest {

    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Clock clock = mock(Clock.class);

    private JdbcReporter reporter;

    private static final String SOURCE = JdbcCleanupTest.class.getSimpleName();

    private static final int DAYS = 7;

    // Timestamp in database is in milliseconds. There are 86,400,000 milliseconds for a day (24 hours).
    // Adding one more second to satisfy the condition in cleanup task
    private static final int SUBTRACT_MILLIS = (DAYS * 86_400_000) + 1000;

    @BeforeMethod
    private void setUp() {
        when(clock.getTime()).thenReturn(System.currentTimeMillis());

        this.reporter = JdbcReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS).convertTimestampTo(TimeUnit.MILLISECONDS).withClock(clock)
                .filter(MetricFilter.ALL).build(SOURCE, dataSource);

        template.execute("DELETE FROM METRIC_GAUGE;");
        template.execute("DELETE FROM METRIC_TIMER;");
        template.execute("DELETE FROM METRIC_METER;");
        template.execute("DELETE FROM METRIC_HISTOGRAM;");
        template.execute("DELETE FROM METRIC_COUNTER;");
    }

    private void cleanValues(String tableName) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ");
        queryBuilder.append(tableName);
        String query = queryBuilder.toString();
        List<Map<String, Object>> result = template.queryForList(query);
        Assert.assertEquals(result.size(), 2);
        try (ScheduledJdbcMetricsCleanupTask cleanupTask = new ScheduledJdbcMetricsCleanupTask(dataSource, DAYS)) {
            cleanupTask.run();
        }
        result = template.queryForList(query);
        Assert.assertEquals(result.size(), 1);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void cleansGaugeValues() {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(1);

        reporter.report(map("gauge", gauge), map(), map(), map(), map());

        when(clock.getTime()).thenReturn(System.currentTimeMillis() - SUBTRACT_MILLIS);

        reporter.report(map("gauge", gauge), map(), map(), map(), map());

        cleanValues("METRIC_GAUGE");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void cleansCounterValues() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        reporter.report(map(), map("test.counter", counter), map(), map(), map());

        when(clock.getTime()).thenReturn(System.currentTimeMillis() - SUBTRACT_MILLIS);

        reporter.report(map(), map("test.counter", counter), map(), map(), map());

        cleanValues("METRIC_COUNTER");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void cleansHistogramValues() throws Exception {
        final Histogram histogram = mock(Histogram.class);
        when(histogram.getCount()).thenReturn(1L);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(2L);
        when(snapshot.getMean()).thenReturn(3.0);
        when(snapshot.getMin()).thenReturn(4L);
        when(snapshot.getStdDev()).thenReturn(5.0);
        when(snapshot.getMedian()).thenReturn(6.0);
        when(snapshot.get75thPercentile()).thenReturn(7.0);
        when(snapshot.get95thPercentile()).thenReturn(8.0);
        when(snapshot.get98thPercentile()).thenReturn(9.0);
        when(snapshot.get99thPercentile()).thenReturn(10.0);
        when(snapshot.get999thPercentile()).thenReturn(11.0);

        when(histogram.getSnapshot()).thenReturn(snapshot);

        reporter.report(map(), map(), map("test.histogram", histogram), map(), map());

        when(clock.getTime()).thenReturn(System.currentTimeMillis() - SUBTRACT_MILLIS);

        reporter.report(map(), map(), map("test.histogram", histogram), map(), map());

        cleanValues("METRIC_HISTOGRAM");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void cleansMeterValues() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);

        reporter.report(map(), map(), map(), map("test.meter", meter), map());

        when(clock.getTime()).thenReturn(System.currentTimeMillis() - SUBTRACT_MILLIS);

        reporter.report(this.map(), map(), map(), map("test.meter", meter), map());

        cleanValues("METRIC_METER");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsTimerValues() throws Exception {
        final Timer timer = mock(Timer.class);
        when(timer.getCount()).thenReturn(1L);
        when(timer.getMeanRate()).thenReturn(2.0);
        when(timer.getOneMinuteRate()).thenReturn(3.0);
        when(timer.getFiveMinuteRate()).thenReturn(4.0);
        when(timer.getFifteenMinuteRate()).thenReturn(5.0);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
        when(snapshot.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
        when(snapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
        when(snapshot.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));
        when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(1000));

        when(timer.getSnapshot()).thenReturn(snapshot);

        reporter.report(map(), map(), map(), map(),
                map("test.timer", timer));

        when(clock.getTime()).thenReturn(System.currentTimeMillis() - SUBTRACT_MILLIS);

        reporter.report(map(), map(), map(), map(),
                map("test.timer", timer));

        cleanValues("METRIC_TIMER");
    }

    private <T> SortedMap<String, T> map() {
        return new TreeMap<>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<>();
        map.put(name, metric);
        return map;
    }

}
