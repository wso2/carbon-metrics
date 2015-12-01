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
package org.wso2.carbon.metrics.impl;

import com.codahale.metrics.*;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.wso2.carbon.metrics.impl.task.ScheduledJDBCMetricsCleanupTask;
import org.wso2.carbon.metrics.jdbc.reporter.JDBCReporter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ScheduledJDBCMetricsCleanupTask}
 */
public class JDBCCleanupTest {

    private static final String SOURCE = JDBCCleanupTest.class.getSimpleName();
    private static DataSource dataSource;
    private static JdbcTemplate template;
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Clock clock = mock(Clock.class);
    private final int DAYS = 7;
    // Timestamp in database is in seconds. There are 86400 seconds for a day (24 hours).
    // Adding one more second to satisfy the condition in cleanup task
    private final int SUBSTRACT_MILLIS = (DAYS * 86400 * 1000) + 1000;
    private JDBCReporter reporter;

    @BeforeClass
    public static void setupDatasource() throws ScriptException, SQLException {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test-cleanup;DB_CLOSE_DELAY=-1", "sa", "");
        template = new JdbcTemplate(dataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("dbscripts/h2.sql"));
        populator.populate(dataSource.getConnection());
    }

    @Before
    public void setUp() throws Exception {
        when(clock.getTime()).thenReturn(System.currentTimeMillis());

        this.reporter = JDBCReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.NANOSECONDS).withClock(clock).filter(MetricFilter.ALL)
                .build(SOURCE, dataSource);

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
        assertEquals(2, result.size());
        @SuppressWarnings("resource")
        ScheduledJDBCMetricsCleanupTask cleanupTask = new ScheduledJDBCMetricsCleanupTask(dataSource, DAYS);
        cleanupTask.run();
        result = template.queryForList(query);
        assertEquals(1, result.size());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void cleansGaugeValues() {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(1);

        reporter.report(map("gauge", gauge), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
                this.<Timer>map());

        when(clock.getTime()).thenReturn(System.currentTimeMillis() - SUBSTRACT_MILLIS);

        reporter.report(map("gauge", gauge), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
                this.<Timer>map());

        cleanValues("METRIC_GAUGE");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void cleansCounterValues() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        reporter.report(this.<Gauge>map(), map("test.counter", counter), this.<Histogram>map(), this.<Meter>map(),
                this.<Timer>map());

        when(clock.getTime()).thenReturn(System.currentTimeMillis() - SUBSTRACT_MILLIS);

        reporter.report(this.<Gauge>map(), map("test.counter", counter), this.<Histogram>map(), this.<Meter>map(),
                this.<Timer>map());

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

        reporter.report(this.<Gauge>map(), this.<Counter>map(), map("test.histogram", histogram), this.<Meter>map(),
                this.<Timer>map());

        when(clock.getTime()).thenReturn(System.currentTimeMillis() - SUBSTRACT_MILLIS);

        reporter.report(this.<Gauge>map(), this.<Counter>map(), map("test.histogram", histogram), this.<Meter>map(),
                this.<Timer>map());

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

        reporter.report(this.<Gauge>map(), this.<Counter>map(), this.<Histogram>map(), map("test.meter", meter),
                this.<Timer>map());

        when(clock.getTime()).thenReturn(System.currentTimeMillis() - SUBSTRACT_MILLIS);

        reporter.report(this.<Gauge>map(), this.<Counter>map(), this.<Histogram>map(), map("test.meter", meter),
                this.<Timer>map());

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

        reporter.report(this.<Gauge>map(), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
                map("test.timer", timer));

        when(clock.getTime()).thenReturn(System.currentTimeMillis() - SUBSTRACT_MILLIS);

        reporter.report(this.<Gauge>map(), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
                map("test.timer", timer));

        cleanValues("METRIC_TIMER");
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
