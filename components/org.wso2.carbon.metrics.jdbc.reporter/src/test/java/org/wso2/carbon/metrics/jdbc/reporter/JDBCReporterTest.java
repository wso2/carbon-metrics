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
package org.wso2.carbon.metrics.jdbc.reporter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

/**
 * Tests for {@link JDBCReporter}
 */
public class JDBCReporterTest {

    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Clock clock = mock(Clock.class);

    private static DataSource dataSource;

    private JDBCReporter reporter;

    private static final String SOURCE = JDBCReporterTest.class.getSimpleName();

    private static JdbcTemplate template;

    @BeforeClass
    public static void setupDatasource() throws Exception {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
        template = new JdbcTemplate(dataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("dbscripts/h2.sql"));
        populator.populate(dataSource.getConnection());
    }

    @Before
    public void setUp() throws Exception {
        when(clock.getTime()).thenReturn(19910191000L);

        this.reporter = JDBCReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.NANOSECONDS).withClock(clock).filter(MetricFilter.ALL)
                .build(SOURCE, dataSource);

        template.execute("DELETE FROM METRIC_GAUGE;");
        template.execute("DELETE FROM METRIC_TIMER;");
        template.execute("DELETE FROM METRIC_METER;");
        template.execute("DELETE FROM METRIC_HISTOGRAM;");
        template.execute("DELETE FROM METRIC_COUNTER;");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsGaugeValues() {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(1);

        long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

        reporter.report(map("gauge", gauge), this.<Counter> map(), this.<Histogram> map(), this.<Meter> map(),
                this.<Timer> map());

        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_GAUGE");
        assertEquals(1, result.size());
        assertEquals("gauge", result.get(0).get("NAME"));
        assertEquals("1", result.get(0).get("VALUE"));
        assertEquals(SOURCE, result.get(0).get("SOURCE"));
        assertEquals(timestamp, result.get(0).get("TIMESTAMP"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsCounterValues() throws Exception {
        long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        reporter.report(this.<Gauge> map(), map("test.counter", counter), this.<Histogram> map(), this.<Meter> map(),
                this.<Timer> map());

        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_COUNTER");
        assertEquals(1, result.size());
        assertEquals("test.counter", result.get(0).get("NAME"));
        assertEquals(100L, result.get(0).get("COUNT"));
        assertEquals(SOURCE, result.get(0).get("SOURCE"));
        assertEquals(timestamp, result.get(0).get("TIMESTAMP"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsHistogramValues() throws Exception {
        final long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

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

        reporter.report(this.<Gauge> map(), this.<Counter> map(), map("test.histogram", histogram), this.<Meter> map(),
                this.<Timer> map());

        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_HISTOGRAM");
        assertEquals(1, result.size());
        assertEquals("test.histogram", result.get(0).get("NAME"));
        assertEquals(1L, result.get(0).get("COUNT"));
        assertEquals(2.0, result.get(0).get("MAX"));
        assertEquals(3.0, result.get(0).get("MEAN"));
        assertEquals(4.0, result.get(0).get("MIN"));
        assertEquals(5.0, result.get(0).get("STDDEV"));
        assertEquals(6.0, result.get(0).get("P50"));
        assertEquals(7.0, result.get(0).get("P75"));
        assertEquals(8.0, result.get(0).get("P95"));
        assertEquals(9.0, result.get(0).get("P98"));
        assertEquals(10.0, result.get(0).get("P99"));
        assertEquals(11.0, result.get(0).get("P999"));
        assertEquals(SOURCE, result.get(0).get("SOURCE"));
        assertEquals(timestamp, result.get(0).get("TIMESTAMP"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsMeterValues() throws Exception {
        long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);

        reporter.report(this.<Gauge> map(), this.<Counter> map(), this.<Histogram> map(), map("test.meter", meter),
                this.<Timer> map());

        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_METER");
        assertEquals(1, result.size());
        assertEquals("test.meter", result.get(0).get("NAME"));
        assertEquals(1L, result.get(0).get("COUNT"));
        assertEquals(2.0, result.get(0).get("MEAN_RATE"));
        assertEquals(3.0, result.get(0).get("M1_RATE"));
        assertEquals(4.0, result.get(0).get("M5_RATE"));
        assertEquals(5.0, result.get(0).get("M15_RATE"));
        assertEquals(SOURCE, result.get(0).get("SOURCE"));
        assertEquals(timestamp, result.get(0).get("TIMESTAMP"));
        assertEquals("events/second", result.get(0).get("RATE_UNIT"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsTimerValues() throws Exception {
        long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

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

        reporter.report(this.<Gauge> map(), this.<Counter> map(), this.<Histogram> map(), this.<Meter> map(),
                map("test.another.timer", timer));

        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_TIMER");
        assertEquals(1, result.size());
        assertEquals("test.another.timer", result.get(0).get("NAME"));
        assertEquals(1L, result.get(0).get("COUNT"));
        assertEquals((double) TimeUnit.MILLISECONDS.toNanos(100), result.get(0).get("MAX"));
        assertEquals((double) TimeUnit.MILLISECONDS.toNanos(200), result.get(0).get("MEAN"));
        assertEquals((double) TimeUnit.MILLISECONDS.toNanos(300), result.get(0).get("MIN"));
        assertEquals((double) TimeUnit.MILLISECONDS.toNanos(400), result.get(0).get("STDDEV"));
        assertEquals(2.0, result.get(0).get("MEAN_RATE"));
        assertEquals(3.0, result.get(0).get("M1_RATE"));
        assertEquals(4.0, result.get(0).get("M5_RATE"));
        assertEquals(5.0, result.get(0).get("M15_RATE"));
        assertEquals((double) TimeUnit.MILLISECONDS.toNanos(500), result.get(0).get("P50"));
        assertEquals((double) TimeUnit.MILLISECONDS.toNanos(600), result.get(0).get("P75"));
        assertEquals((double) TimeUnit.MILLISECONDS.toNanos(700), result.get(0).get("P95"));
        assertEquals((double) TimeUnit.MILLISECONDS.toNanos(800), result.get(0).get("P98"));
        assertEquals((double) TimeUnit.MILLISECONDS.toNanos(900), result.get(0).get("P99"));
        assertEquals((double) TimeUnit.MILLISECONDS.toNanos(1000), result.get(0).get("P999"));
        assertEquals(SOURCE, result.get(0).get("SOURCE"));
        assertEquals(timestamp, result.get(0).get("TIMESTAMP"));
        assertEquals("calls/second", result.get(0).get("RATE_UNIT"));
        assertEquals("nanoseconds", result.get(0).get("DURATION_UNIT"));
    }

    @Test
    public void reportsSeconds() {
        long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());
        assertEquals(timestamp, reportGauge(TimeUnit.SECONDS));
    }

    @Test
    public void reportsMilliseconds() {
        long timestamp = clock.getTime();
        assertEquals(timestamp, reportGauge(TimeUnit.MILLISECONDS));
    }

    @Test
    public void reportsNanoseconds() {
        long timestamp = TimeUnit.NANOSECONDS.convert(clock.getTime(), TimeUnit.MILLISECONDS);
        assertEquals(timestamp, reportGauge(TimeUnit.NANOSECONDS));
    }

    @SuppressWarnings("rawtypes")
    private long reportGauge(TimeUnit timestampUnit) {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(1);

        JDBCReporter reporter = JDBCReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.NANOSECONDS).convertTimestampTo(timestampUnit).withClock(clock)
                .filter(MetricFilter.ALL).build(SOURCE, dataSource);

        reporter.report(map("gauge", gauge), this.<Counter> map(), this.<Histogram> map(), this.<Meter> map(),
                this.<Timer> map());
        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_GAUGE");
        assertEquals(1, result.size());
        return (Long) result.get(0).get("TIMESTAMP");
    }

    private <T> SortedMap<String, T> map() {
        return new TreeMap<String, T>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<String, T>();
        map.put(name, metric);
        return map;
    }

}
