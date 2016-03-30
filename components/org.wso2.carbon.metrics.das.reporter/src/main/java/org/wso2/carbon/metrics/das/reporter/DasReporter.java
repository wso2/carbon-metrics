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
package org.wso2.carbon.metrics.das.reporter;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * Reporting the measurements for each metric to Data Analytics Server (DAS)
 */
public class DasReporter extends ScheduledReporter {

    /**
     * Returns a new {@link Builder} for {@link DasReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link DasReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link DasReporter} instances. Defaults to converting rates to events/second, converting durations
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
         * Builds a {@link DasReporter} with the given properties to report metrics to DAS
         *
         * @param source              A value to identify the source of each metrics
         * @param type
         * @param receiverURL
         * @param authURL
         * @param username
         * @param password
         * @param dataAgentConfigPath
         * @return a {@link DasReporter}
         */
        public DasReporter build(String source, String type, String receiverURL, String authURL, String username,
                                 String password, String dataAgentConfigPath) {
            return new DasReporter(registry, source, type, receiverURL, authURL, username, password,
                    dataAgentConfigPath, rateUnit, durationUnit, clock, filter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DasReporter.class);

    private final Clock clock;
    private final String source;
    private final DataPublisher dataPublisher;

    private static final String GAUGE_STREAM = "org.wso2.carbon.metrics.gauge";
    private static final String COUNTER_STREAM = "org.wso2.carbon.metrics.counter";
    private static final String METER_STREAM = "org.wso2.carbon.metrics.meter";
    private static final String HISTOGRAM_STREAM = "org.wso2.carbon.metrics.histogram";
    private static final String TIMER_STREAM = "org.wso2.carbon.metrics.timer";

    private static final String VERSION = "1.0.0";

    private static final String GAUGE_STREAM_ID;
    private static final String COUNTER_STREAM_ID;
    private static final String METER_STREAM_ID;
    private static final String HISTOGRAM_STREAM_ID;
    private static final String TIMER_STREAM_ID;

    static {
        GAUGE_STREAM_ID = DataBridgeCommonsUtils.generateStreamId(GAUGE_STREAM, VERSION);
        COUNTER_STREAM_ID = DataBridgeCommonsUtils.generateStreamId(COUNTER_STREAM, VERSION);
        METER_STREAM_ID = DataBridgeCommonsUtils.generateStreamId(METER_STREAM, VERSION);
        HISTOGRAM_STREAM_ID = DataBridgeCommonsUtils.generateStreamId(HISTOGRAM_STREAM, VERSION);
        TIMER_STREAM_ID = DataBridgeCommonsUtils.generateStreamId(TIMER_STREAM, VERSION);
    }

    private DasReporter(MetricRegistry registry, String source, String type, String receiverURL, String authURL,
                        String username, String password, String dataAgentConfigPath, TimeUnit rateUnit,
                        TimeUnit durationUnit, Clock clock, MetricFilter filter) {
        super(registry, "das-reporter", filter, rateUnit, durationUnit);
        this.source = source;
        this.clock = clock;
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (receiverURL == null) {
            throw new IllegalArgumentException("Data Receiver URL cannot be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (dataAgentConfigPath != null) {
            AgentHolder.setConfigPath(dataAgentConfigPath);
        }
        try {
            dataPublisher = new DataPublisher(type, receiverURL, authURL, username, password);
        } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException
                | DataEndpointAuthenticationException | TransportException e) {
            throw new IllegalStateException("Error when initializing the Data Publisher", e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        try {
            dataPublisher.shutdown();
        } catch (DataEndpointException e) {
            LOGGER.error("Error when stopping the Data Publisher", e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        // Report time in milliseconds
        final long timestamp = clock.getTime();

        if (!gauges.isEmpty()) {
            reportGauges(timestamp, gauges);
        }
        if (!counters.isEmpty()) {
            reportCounters(timestamp, counters);
        }
        if (!histograms.isEmpty()) {
            reportHistograms(timestamp, histograms);
        }
        if (!meters.isEmpty()) {
            reportMeters(timestamp, meters);
        }
        if (!timers.isEmpty()) {
            reportTimers(timestamp, timers);
        }
    }

    @SuppressWarnings("rawtypes")
    private void reportGauges(final long timestamp, final SortedMap<String, Gauge> gauges) {
        Object[] meta = new Object[]{timestamp};
        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            String name = entry.getKey();
            Gauge gauge = entry.getValue();
            Object[] payload = new Object[3];
            payload[0] = source;
            payload[1] = name;
            payload[2] = convertToDouble(gauge.getValue());
            Event event = new Event(GAUGE_STREAM_ID, timestamp, meta, null, payload);
            dataPublisher.publish(event);
        }
    }

    private Double convertToDouble(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else {
            return null;
        }
    }

    private void reportCounters(final long timestamp, final SortedMap<String, Counter> counters) {
        Object[] meta = new Object[]{timestamp};
        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            String name = entry.getKey();
            Counter counter = entry.getValue();
            Object[] payload = new Object[3];
            payload[0] = source;
            payload[1] = name;
            payload[2] = counter.getCount();
            Event event = new Event(COUNTER_STREAM_ID, timestamp, meta, null, payload);
            dataPublisher.publish(event);
        }
    }

    private void reportHistograms(final long timestamp, final SortedMap<String, Histogram> histograms) {
        Object[] meta = new Object[]{timestamp};
        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            String name = entry.getKey();
            Histogram histogram = entry.getValue();
            Snapshot snapshot = histogram.getSnapshot();
            Object[] payload = new Object[13];
            payload[0] = source;
            payload[1] = name;
            payload[2] = histogram.getCount();
            payload[3] = snapshot.getMax();
            payload[4] = snapshot.getMean();
            payload[5] = snapshot.getMin();
            payload[6] = snapshot.getStdDev();
            payload[7] = snapshot.getMedian();
            payload[8] = snapshot.get75thPercentile();
            payload[9] = snapshot.get95thPercentile();
            payload[10] = snapshot.get98thPercentile();
            payload[11] = snapshot.get99thPercentile();
            payload[12] = snapshot.get999thPercentile();
            Event event = new Event(HISTOGRAM_STREAM_ID, timestamp, meta, null, payload);
            dataPublisher.publish(event);
        }
    }

    private void reportMeters(final long timestamp, final SortedMap<String, Meter> meters) {
        Object[] meta = new Object[]{timestamp};
        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            String name = entry.getKey();
            Meter meter = entry.getValue();
            Object[] payload = new Object[8];
            payload[0] = source;
            payload[1] = name;
            payload[2] = meter.getCount();
            payload[3] = convertRate(meter.getMeanRate());
            payload[4] = convertRate(meter.getOneMinuteRate());
            payload[5] = convertRate(meter.getFiveMinuteRate());
            payload[6] = convertRate(meter.getFifteenMinuteRate());
            payload[7] = String.format("events/%s", getRateUnit());
            Event event = new Event(METER_STREAM_ID, timestamp, meta, null, payload);
            dataPublisher.publish(event);
        }
    }

    private void reportTimers(final long timestamp, final SortedMap<String, Timer> timers) {
        Object[] meta = new Object[]{timestamp};
        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            String name = entry.getKey();
            Timer timer = entry.getValue();
            Snapshot snapshot = timer.getSnapshot();
            Object[] payload = new Object[19];
            payload[0] = source;
            payload[1] = name;
            payload[2] = timer.getCount();
            payload[3] = convertDuration(snapshot.getMax());
            payload[4] = convertDuration(snapshot.getMean());
            payload[5] = convertDuration(snapshot.getMin());
            payload[6] = convertDuration(snapshot.getStdDev());
            payload[7] = convertDuration(snapshot.getMedian());
            payload[8] = convertDuration(snapshot.get75thPercentile());
            payload[9] = convertDuration(snapshot.get95thPercentile());
            payload[10] = convertDuration(snapshot.get98thPercentile());
            payload[11] = convertDuration(snapshot.get99thPercentile());
            payload[12] = convertDuration(snapshot.get999thPercentile());
            payload[13] = convertRate(timer.getMeanRate());
            payload[14] = convertRate(timer.getOneMinuteRate());
            payload[15] = convertRate(timer.getFiveMinuteRate());
            payload[16] = convertRate(timer.getFifteenMinuteRate());
            payload[17] = String.format("calls/%s", getRateUnit());
            payload[18] = getDurationUnit();
            Event event = new Event(TIMER_STREAM_ID, timestamp, meta, null, payload);
            dataPublisher.publish(event);
        }
    }
}
