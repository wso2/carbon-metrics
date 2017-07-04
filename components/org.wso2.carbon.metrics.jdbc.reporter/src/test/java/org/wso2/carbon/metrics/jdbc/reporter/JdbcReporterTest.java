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

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JdbcReporter}.
 */
public class JdbcReporterTest {

    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Clock clock = mock(Clock.class);

    private static DataSource dataSource;

    private JdbcReporter reporter;

    private static final String SOURCE = JdbcReporterTest.class.getSimpleName();

    private static JdbcTemplate template;

    private static TransactionTemplate transactionTemplate;

    @BeforeSuite
    private static void init() throws Exception {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
        template = new JdbcTemplate(dataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("dbscripts/h2.sql"));
        populator.populate(dataSource.getConnection());
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(dataSourceTransactionManager);
    }

    @BeforeMethod
    private void setUp() throws Exception {
        when(clock.getTime()).thenReturn(19910191000L);

        this.reporter = JdbcReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.NANOSECONDS).withClock(clock).filter(MetricFilter.ALL)
                .build(SOURCE, dataSource);

        transactionTemplate.execute(status -> {
            template.execute("DELETE FROM METRIC_GAUGE;");
            template.execute("DELETE FROM METRIC_TIMER;");
            template.execute("DELETE FROM METRIC_METER;");
            template.execute("DELETE FROM METRIC_HISTOGRAM;");
            template.execute("DELETE FROM METRIC_COUNTER;");
            return null;
        });
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsGaugeValues() {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(1);

        long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

        reporter.report(map("gauge", gauge), this.map(), this.map(), this.map(), this.map());

        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_GAUGE");
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).get("NAME"), "gauge");
        Assert.assertEquals(result.get(0).get("VALUE"), "1");
        Assert.assertEquals(result.get(0).get("SOURCE"), SOURCE);
        Assert.assertEquals(result.get(0).get("TIMESTAMP"), timestamp);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsCounterValues() throws Exception {
        long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        reporter.report(map(), map("test.counter", counter), map(), map(), map());

        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_COUNTER");
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).get("NAME"), "test.counter");
        Assert.assertEquals(result.get(0).get("COUNT"), 100L);
        Assert.assertEquals(result.get(0).get("SOURCE"), SOURCE);
        Assert.assertEquals(result.get(0).get("TIMESTAMP"), timestamp);
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

        reporter.report(map(), map(), map("test.histogram", histogram), map(), map());

        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_HISTOGRAM");
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).get("NAME"), "test.histogram");
        Assert.assertEquals(result.get(0).get("COUNT"), 1L);
        Assert.assertEquals(result.get(0).get("MAX"), 2.0);
        Assert.assertEquals(result.get(0).get("MEAN"), 3.0);
        Assert.assertEquals(result.get(0).get("MIN"), 4.0);
        Assert.assertEquals(result.get(0).get("STDDEV"), 5.0);
        Assert.assertEquals(result.get(0).get("P50"), 6.0);
        Assert.assertEquals(result.get(0).get("P75"), 7.0);
        Assert.assertEquals(result.get(0).get("P95"), 8.0);
        Assert.assertEquals(result.get(0).get("P98"), 9.0);
        Assert.assertEquals(result.get(0).get("P99"), 10.0);
        Assert.assertEquals(result.get(0).get("P999"), 11.0);
        Assert.assertEquals(result.get(0).get("SOURCE"), SOURCE);
        Assert.assertEquals(result.get(0).get("TIMESTAMP"), timestamp);
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

        reporter.report(map(), map(), map(), map("test.meter", meter), map());

        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_METER");
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).get("NAME"), "test.meter");
        Assert.assertEquals(result.get(0).get("COUNT"), 1L);
        Assert.assertEquals(result.get(0).get("MEAN_RATE"), 2.0);
        Assert.assertEquals(result.get(0).get("M1_RATE"), 3.0);
        Assert.assertEquals(result.get(0).get("M5_RATE"), 4.0);
        Assert.assertEquals(result.get(0).get("M15_RATE"), 5.0);
        Assert.assertEquals(result.get(0).get("SOURCE"), SOURCE);
        Assert.assertEquals(result.get(0).get("TIMESTAMP"), timestamp);
        Assert.assertEquals(result.get(0).get("RATE_UNIT"), "events/second");
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

        reporter.report(map(), map(), map(), map(), map("test.timer", timer));

        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_TIMER");
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).get("NAME"), "test.timer");
        Assert.assertEquals(result.get(0).get("COUNT"), 1L);
        Assert.assertEquals(result.get(0).get("MAX"), (double) TimeUnit.MILLISECONDS.toNanos(100));
        Assert.assertEquals(result.get(0).get("MEAN"), (double) TimeUnit.MILLISECONDS.toNanos(200));
        Assert.assertEquals(result.get(0).get("MIN"), (double) TimeUnit.MILLISECONDS.toNanos(300));
        Assert.assertEquals(result.get(0).get("STDDEV"), (double) TimeUnit.MILLISECONDS.toNanos(400));
        Assert.assertEquals(result.get(0).get("MEAN_RATE"), 2.0);
        Assert.assertEquals(result.get(0).get("M1_RATE"), 3.0);
        Assert.assertEquals(result.get(0).get("M5_RATE"), 4.0);
        Assert.assertEquals(result.get(0).get("M15_RATE"), 5.0);
        Assert.assertEquals(result.get(0).get("P50"), (double) TimeUnit.MILLISECONDS.toNanos(500));
        Assert.assertEquals(result.get(0).get("P75"), (double) TimeUnit.MILLISECONDS.toNanos(600));
        Assert.assertEquals(result.get(0).get("P95"), (double) TimeUnit.MILLISECONDS.toNanos(700));
        Assert.assertEquals(result.get(0).get("P98"), (double) TimeUnit.MILLISECONDS.toNanos(800));
        Assert.assertEquals(result.get(0).get("P99"), (double) TimeUnit.MILLISECONDS.toNanos(900));
        Assert.assertEquals(result.get(0).get("P999"), (double) TimeUnit.MILLISECONDS.toNanos(1000));
        Assert.assertEquals(result.get(0).get("SOURCE"), SOURCE);
        Assert.assertEquals(result.get(0).get("TIMESTAMP"), timestamp);
        Assert.assertEquals(result.get(0).get("RATE_UNIT"), "calls/second");
        Assert.assertEquals(result.get(0).get("DURATION_UNIT"), "nanoseconds");
    }

    @Test
    public void reportsSeconds() {
        long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());
        Assert.assertEquals(reportGauge(TimeUnit.SECONDS), timestamp);
    }

    @Test
    public void reportsMilliseconds() {
        long timestamp = clock.getTime();
        Assert.assertEquals(reportGauge(TimeUnit.MILLISECONDS), timestamp);
    }

    @Test
    public void reportsNanoseconds() {
        long timestamp = TimeUnit.NANOSECONDS.convert(clock.getTime(), TimeUnit.MILLISECONDS);
        Assert.assertEquals(reportGauge(TimeUnit.NANOSECONDS), timestamp);
    }

    @SuppressWarnings("rawtypes")
    private long reportGauge(TimeUnit timestampUnit) {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(1);

        JdbcReporter reporter = JdbcReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.NANOSECONDS).convertTimestampTo(timestampUnit).withClock(clock)
                .filter(MetricFilter.ALL).build(SOURCE, dataSource);

        reporter.report(map("gauge", gauge), map(), map(), map(), map());
        List<Map<String, Object>> result = template.queryForList("SELECT * FROM METRIC_GAUGE");
        Assert.assertEquals(result.size(), 1);
        return (Long) result.get(0).get("TIMESTAMP");
    }

    private <T> SortedMap<String, T> map() {
        return new TreeMap<>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<>();
        map.put(name, metric);
        return map;
    }

    @Test
    public void testJdbcReporterBuilderValidations() {
        build(null, null);
        build("", null);
        build(SOURCE, null);
    }

    private void build(String source, DataSource dataSource) {
        try {
            JdbcReporter.forRegistry(registry).build(source, dataSource);
            Assert.fail("The JdbcReporter Builder should fail");
        } catch (IllegalArgumentException e) {
        }
    }
}
