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
package org.wso2.carbon.metrics.reporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

/**
 * A reporter which stores the measurements for each metric in a database using JDBC API.
 */
public class JDBCReporter extends ScheduledReporter {

    /**
     * Returns a new {@link Builder} for {@link JDBCReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link JDBCReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link JDBCReporter} instances. Defaults to converting rates to events/second, converting durations
     * to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private Clock clock;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.clock = Clock.defaultClock();
            this.filter = MetricFilter.ALL;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Use the given {@link Clock} instance for the time.
         *
         * @param clock a {@link Clock} instance
         * @return {@code this}
         */
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@link JDBCReporter} with the given properties to report metrics to a database
         *
         * @param source A value to identify the source of each metrics in database
         * @param dataSource The {@link DataSource}, which will be used to store the data from each metric
         * @return a {@link JDBCReporter}
         */
        public JDBCReporter build(String source, DataSource dataSource) {
            return new JDBCReporter(registry, source, dataSource, rateUnit, durationUnit, clock, filter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCReporter.class);

    private final Clock clock;
    private final String source;
    private final DataSource dataSource;

    private static final String INSERT_GAUGE_QUERY = "INSERT INTO METRIC_GAUGE (SOURCE, TIMESTAMP, NAME, VALUE) VALUES (?,?,?,?);";
    private static final String INSERT_COUNTER_QUERY = "INSERT INTO METRIC_COUNTER (SOURCE, TIMESTAMP, NAME, COUNT) VALUES (?,?,?,?);";
    private static final String INSERT_METER_QUERY = "INSERT INTO METRIC_METER (SOURCE,TIMESTAMP,NAME,COUNT,MEAN_RATE,M1_RATE,M5_RATE,M15_RATE,RATE_UNIT) VALUES (?,?,?,?,?,?,?,?,?);";
    private static final String INSERT_HISTOGRAM_QUERY = "INSERT INTO METRIC_HISTOGRAM (SOURCE,TIMESTAMP,NAME,COUNT,MAX,MEAN,MIN,STDDEV,P50,P75,P95,P98,P99,P999) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
    private static final String INSERT_TIMER_QUERY = "INSERT INTO METRIC_TIMER (SOURCE,TIMESTAMP,NAME,COUNT,MAX,MEAN,MIN,STDDEV,P50,P75,P95,P98,P99,P999,MEAN_RATE,M1_RATE,M5_RATE,M15_RATE,RATE_UNIT,DURATION_UNIT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

    private JDBCReporter(MetricRegistry registry, String source, DataSource dataSource, TimeUnit rateUnit,
            TimeUnit durationUnit, Clock clock, MetricFilter filter) {
        super(registry, "jdbc-reporter", filter, rateUnit, durationUnit);
        this.source = source;
        this.dataSource = dataSource;
        this.clock = clock;
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException("Data source cannot be null");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        final long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

        reportGauges(timestamp, gauges);
        reportCounters(timestamp, counters);
        reportHistograms(timestamp, histograms);
        reportMeters(timestamp, meters);
        reportTimers(timestamp, timers);
    }

    @SuppressWarnings("rawtypes")
    private void reportGauges(final long timestamp, final SortedMap<String, Gauge> gauges) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(INSERT_GAUGE_QUERY);

            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                String name = entry.getKey();
                Gauge gauge = entry.getValue();

                ps.setString(1, source);
                ps.setLong(2, timestamp);
                ps.setString(3, name);
                ps.setObject(4, gauge.getValue());
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            connection.close();
            ps = null;
            connection = null;
        } catch (SQLException e) {
            LOGGER.error("Error when reporting gauges", e);
        } finally {
            closeQuietly(connection, ps);
        }
    }

    private void reportCounters(final long timestamp, final SortedMap<String, Counter> counters) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(INSERT_COUNTER_QUERY);

            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                String name = entry.getKey();
                Counter counter = entry.getValue();

                ps.setString(1, source);
                ps.setLong(2, timestamp);
                ps.setString(3, name);
                ps.setLong(4, counter.getCount());
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            connection.close();
            ps = null;
            connection = null;
        } catch (SQLException e) {
            LOGGER.error("Error when reporting counters", e);
        } finally {
            closeQuietly(connection, ps);
        }
    }

    private void reportHistograms(final long timestamp, final SortedMap<String, Histogram> histograms) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(INSERT_HISTOGRAM_QUERY);

            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                String name = entry.getKey();
                Histogram histogram = entry.getValue();
                final Snapshot snapshot = histogram.getSnapshot();

                ps.setString(1, source);
                ps.setLong(2, timestamp);
                ps.setString(3, name);
                ps.setLong(4, histogram.getCount());
                ps.setDouble(5, snapshot.getMax());
                ps.setDouble(6, snapshot.getMean());
                ps.setDouble(7, snapshot.getMin());
                ps.setDouble(8, snapshot.getStdDev());
                ps.setDouble(9, snapshot.getMedian());
                ps.setDouble(10, snapshot.get75thPercentile());
                ps.setDouble(11, snapshot.get95thPercentile());
                ps.setDouble(12, snapshot.get98thPercentile());
                ps.setDouble(13, snapshot.get99thPercentile());
                ps.setDouble(14, snapshot.get999thPercentile());
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            connection.close();
            ps = null;
            connection = null;
        } catch (SQLException e) {
            LOGGER.error("Error when reporting histograms", e);
        } finally {
            closeQuietly(connection, ps);
        }
    }

    private void reportMeters(final long timestamp, final SortedMap<String, Meter> meters) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(INSERT_METER_QUERY);

            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                String name = entry.getKey();
                Meter meter = entry.getValue();

                ps.setString(1, source);
                ps.setLong(2, timestamp);
                ps.setString(3, name);
                ps.setLong(4, meter.getCount());
                ps.setDouble(5, convertRate(meter.getMeanRate()));
                ps.setDouble(6, convertRate(meter.getOneMinuteRate()));
                ps.setDouble(7, convertRate(meter.getFiveMinuteRate()));
                ps.setDouble(8, convertRate(meter.getFifteenMinuteRate()));
                ps.setString(9, String.format("events/%s", getRateUnit()));
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            connection.close();
            ps = null;
            connection = null;
        } catch (SQLException e) {
            LOGGER.error("Error when reporting meters", e);
        } finally {
            closeQuietly(connection, ps);
        }
    }

    private void reportTimers(final long timestamp, final SortedMap<String, Timer> timers) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(INSERT_TIMER_QUERY);

            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                String name = entry.getKey();
                Timer timer = entry.getValue();
                final Snapshot snapshot = timer.getSnapshot();

                ps.setString(1, source);
                ps.setLong(2, timestamp);
                ps.setString(3, name);
                ps.setLong(4, timer.getCount());
                ps.setDouble(5, convertDuration(snapshot.getMax()));
                ps.setDouble(6, convertDuration(snapshot.getMean()));
                ps.setDouble(7, convertDuration(snapshot.getMin()));
                ps.setDouble(8, convertDuration(snapshot.getStdDev()));
                ps.setDouble(9, convertDuration(snapshot.getMedian()));
                ps.setDouble(10, convertDuration(snapshot.get75thPercentile()));
                ps.setDouble(11, convertDuration(snapshot.get95thPercentile()));
                ps.setDouble(12, convertDuration(snapshot.get98thPercentile()));
                ps.setDouble(13, convertDuration(snapshot.get99thPercentile()));
                ps.setDouble(14, convertDuration(snapshot.get999thPercentile()));
                ps.setDouble(15, convertRate(timer.getMeanRate()));
                ps.setDouble(16, convertRate(timer.getOneMinuteRate()));
                ps.setDouble(17, convertRate(timer.getFiveMinuteRate()));
                ps.setDouble(18, convertRate(timer.getFifteenMinuteRate()));
                ps.setString(19, String.format("calls/%s", getRateUnit()));
                ps.setString(20, getDurationUnit());
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            connection.close();
            ps = null;
            connection = null;
        } catch (SQLException e) {
            LOGGER.error("Error when reporting timers", e);
        } finally {
            closeQuietly(connection, ps);
        }
    }

    private void closeQuietly(Connection connection, PreparedStatement ps) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }
}
