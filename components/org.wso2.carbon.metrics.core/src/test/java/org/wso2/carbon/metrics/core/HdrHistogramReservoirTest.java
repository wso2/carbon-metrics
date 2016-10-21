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
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.impl.SnapshotImpl;
import org.wso2.carbon.metrics.core.impl.reservoir.HdrHistogramReservoir;
import org.wso2.carbon.metrics.core.impl.reservoir.HdrHistogramResetOnSnapshotReservoir;

import java.io.ByteArrayOutputStream;
import java.util.stream.IntStream;

/**
 * Test HdrHistogram Reservoir implementations.
 * These tests work for 100 measurements. When using more numbers, for example, 1000, the tests may fail.
 */
public class HdrHistogramReservoirTest extends BaseMetricTest {

    @Test
    public void testHdrHistogramReservoir() {
        HdrHistogramReservoir hdrHistogramReservoir = new HdrHistogramReservoir();
        IntStream.rangeClosed(1, 100).forEach(hdrHistogramReservoir::update);
        Assert.assertEquals(hdrHistogramReservoir.size(), 100);

        com.codahale.metrics.Snapshot snapshot = hdrHistogramReservoir.getSnapshot();
        testSnapshot(new SnapshotImpl(snapshot));
        testDump(snapshot);

        // Get snapshot again
        com.codahale.metrics.Snapshot snapshot2 = hdrHistogramReservoir.getSnapshot();
        long values[] = snapshot2.getValues();
        testSnapshot(new SnapshotImpl(snapshot2));
        for (int i = 0; i < values.length; i++) {
            Assert.assertTrue(values[i] > 0);
        }
    }

    @Test
    public void testHdrHistogramResetOnSnapshotReservoir() {
        HdrHistogramResetOnSnapshotReservoir hdrHistogramResetOnSnapshotReservoir =
                new HdrHistogramResetOnSnapshotReservoir();
        IntStream.rangeClosed(1, 100).forEach(hdrHistogramResetOnSnapshotReservoir::update);
        Assert.assertEquals(hdrHistogramResetOnSnapshotReservoir.size(), 100);

        // Update again as the HdrHistogram is reset when calculating the size
        IntStream.rangeClosed(1, 100).forEach(hdrHistogramResetOnSnapshotReservoir::update);

        com.codahale.metrics.Snapshot snapshot = hdrHistogramResetOnSnapshotReservoir.getSnapshot();
        testSnapshot(new SnapshotImpl(snapshot));
        testDump(snapshot);

        // Get snapshot again
        com.codahale.metrics.Snapshot snapshot2 = hdrHistogramResetOnSnapshotReservoir.getSnapshot();
        long values[] = snapshot2.getValues();
        Assert.assertEquals(values.length, 0);
    }

    private void testDump(com.codahale.metrics.Snapshot snapshot) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        snapshot.dump(outputStream);
        String output = new String(outputStream.toByteArray());

        StringBuilder stringBuilder = new StringBuilder();
        IntStream.rangeClosed(1, 100).forEach(i -> stringBuilder.append(String.format("%d%n", i)));

        Assert.assertEquals(output, stringBuilder.toString());
    }
}
