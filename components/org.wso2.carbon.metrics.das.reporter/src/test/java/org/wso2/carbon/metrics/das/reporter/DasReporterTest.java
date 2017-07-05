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
package org.wso2.carbon.metrics.das.reporter;

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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.commons.Event;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DasReporter}.
 */
public class DasReporterTest {

    private static final String SOURCE = DasReporterTest.class.getSimpleName();
    private static final String RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";
    private final TestEventServer testServer = new TestEventServer(RESOURCES_DIR);
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Clock clock = mock(Clock.class);
    private DasReporter reporter;
    private int serverPort;

    @Parameters("server-port")
    @BeforeSuite
    private void startServer(String serverPort) {
        this.serverPort = Integer.parseInt(serverPort);
        testServer.start("localhost", this.serverPort);
    }

    @AfterSuite
    private void stopServer() {
        testServer.stop();
    }

    @BeforeMethod
    private void createReporter() {
        when(clock.getTime()).thenReturn(19910191000L);

        this.reporter = DasReporter.forRegistry(registry).filter(MetricFilter.ALL)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withClock(clock)
                .build(SOURCE, "thrift", "tcp://localhost:" + serverPort, null, "admin", "admin", RESOURCES_DIR +
                        File.separator + "data.agent.config.yaml");
    }

    @AfterMethod
    private void stopReporter() {
        reporter.stop();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsGaugeValues() {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(1);

        reporter.report(map("test.gauge", gauge), this.map(), this.map(), this.map(), this.map());

        Event event = testServer.getEvent("Gauge");
        Assert.assertEquals(event.getTimeStamp(), clock.getTime());
        Assert.assertEquals(event.getMetaData()[0], clock.getTime());
        Assert.assertEquals(event.getPayloadData()[0], SOURCE);
        Assert.assertEquals(event.getPayloadData()[1], "test.gauge");
        Assert.assertEquals(event.getPayloadData()[2], 1.0D);

        // Report different data types for Gauge
        when(gauge.getValue()).thenReturn(1.0D);
        reporter.report(map("test.gauge", gauge), this.map(), this.map(), this.map(), this.map());

        when(gauge.getValue()).thenReturn(1.0F);
        reporter.report(map("test.gauge", gauge), this.map(), this.map(), this.map(), this.map());

        when(gauge.getValue()).thenReturn(1L);
        reporter.report(map("test.gauge", gauge), this.map(), this.map(), this.map(), this.map());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsCounterValues() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        reporter.report(this.map(), map("test.counter", counter), this.map(), this.map(), this.map());

        Event event = testServer.getEvent("Counter");
        Assert.assertEquals(event.getTimeStamp(), clock.getTime());
        Assert.assertEquals(event.getMetaData()[0], clock.getTime());
        Assert.assertEquals(event.getPayloadData()[0], SOURCE);
        Assert.assertEquals(event.getPayloadData()[1], "test.counter");
        Assert.assertEquals(event.getPayloadData()[2], 100L);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsHistogramValues() throws Exception {
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

        reporter.report(this.map(), this.map(), map("test.histogram", histogram), this.map(), this.map());

        Event event = testServer.getEvent("Histogram");
        Assert.assertEquals(event.getTimeStamp(), clock.getTime());
        Assert.assertEquals(event.getMetaData()[0], clock.getTime());
        Assert.assertEquals(event.getPayloadData()[0], SOURCE);
        Assert.assertEquals(event.getPayloadData()[1], "test.histogram");
        Assert.assertEquals(event.getPayloadData()[2], 1L);
        Assert.assertEquals(event.getPayloadData()[3], 2L);
        Assert.assertEquals(event.getPayloadData()[4], 3.0);
        Assert.assertEquals(event.getPayloadData()[5], 4L);
        Assert.assertEquals(event.getPayloadData()[6], 5.0);
        Assert.assertEquals(event.getPayloadData()[7], 6.0);
        Assert.assertEquals(event.getPayloadData()[8], 7.0);
        Assert.assertEquals(event.getPayloadData()[9], 8.0);
        Assert.assertEquals(event.getPayloadData()[10], 9.0);
        Assert.assertEquals(event.getPayloadData()[11], 10.0);
        Assert.assertEquals(event.getPayloadData()[12], 11.0);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsMeterValues() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);

        reporter.report(this.map(), this.map(), this.map(), map("test.meter", meter), this.map());

        Event event = testServer.getEvent("Meter");
        Assert.assertEquals(event.getTimeStamp(), clock.getTime());
        Assert.assertEquals(event.getMetaData()[0], clock.getTime());
        Assert.assertEquals(event.getPayloadData()[0], SOURCE);
        Assert.assertEquals(event.getPayloadData()[1], "test.meter");
        Assert.assertEquals(event.getPayloadData()[2], 1L);
        Assert.assertEquals(event.getPayloadData()[3], 2.0);
        Assert.assertEquals(event.getPayloadData()[4], 3.0);
        Assert.assertEquals(event.getPayloadData()[5], 4.0);
        Assert.assertEquals(event.getPayloadData()[6], 5.0);
        Assert.assertEquals(event.getPayloadData()[7], "events/second");
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

        reporter.report(this.map(), this.map(), this.map(), this.map(), map("test.timer", timer));

        Event event = testServer.getEvent("Timer");
        Assert.assertEquals(event.getTimeStamp(), clock.getTime());
        Assert.assertEquals(event.getMetaData()[0], clock.getTime());
        Assert.assertEquals(event.getPayloadData()[0], SOURCE);
        Assert.assertEquals(event.getPayloadData()[1], "test.timer");
        Assert.assertEquals(event.getPayloadData()[2], 1L);
        Assert.assertEquals(event.getPayloadData()[3], 100.0);
        Assert.assertEquals(event.getPayloadData()[4], 200.0);
        Assert.assertEquals(event.getPayloadData()[5], 300.0);
        Assert.assertEquals(event.getPayloadData()[6], 400.0);
        Assert.assertEquals(event.getPayloadData()[7], 500.0);
        Assert.assertEquals(event.getPayloadData()[8], 600.0);
        Assert.assertEquals(event.getPayloadData()[9], 700.0);
        Assert.assertEquals(event.getPayloadData()[10], 800.0);
        Assert.assertEquals(event.getPayloadData()[11], 900.0);
        Assert.assertEquals(event.getPayloadData()[12], 1000.0);
        Assert.assertEquals(event.getPayloadData()[13], 2.0);
        Assert.assertEquals(event.getPayloadData()[14], 3.0);
        Assert.assertEquals(event.getPayloadData()[15], 4.0);
        Assert.assertEquals(event.getPayloadData()[16], 5.0);
        Assert.assertEquals(event.getPayloadData()[17], "calls/second");
        Assert.assertEquals(event.getPayloadData()[18], "milliseconds");
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
    public void testDasReporterBuilderValidations() {
        build(null, null, null, null, null, null, null);
        build("", null, null, null, null, null, null);
        build(SOURCE, null, null, null, null, null, null);
        build(SOURCE, "", null, null, null, null, null);
        build(SOURCE, "thrift", null, null, null, null, null);
        build(SOURCE, "thrift", "", null, null, null, null);
        build(SOURCE, "thrift", "tcp://localhost:1234", null, null, null, null);
        build(SOURCE, "thrift", "tcp://localhost:1234", null, "", null, null);
        build(SOURCE, "thrift", "tcp://localhost:1234", null, "admin", null, null);
        build(SOURCE, "thrift", "tcp://localhost:1234", null, "admin", "", null);
        build(SOURCE, "thrift", "invalidURL", null, "admin", "admin", null);
    }

    private void build(String source, String type, String receiverURL, String authURL, String username,
                       String password, String dataAgentConfigPath) {
        try {
            DasReporter.forRegistry(registry).build(source, type, receiverURL, authURL, username, password,
                    dataAgentConfigPath);
            Assert.fail("The DasReporter Builder should fail");
        } catch (IllegalArgumentException e) {
        } catch (IllegalStateException e) {
        }
    }
}
