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
package org.wso2.carbon.metrics.core.impl.reservoir;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.UniformReservoir;
import org.HdrHistogram.Recorder;
import org.wso2.carbon.metrics.core.config.model.ReservoirParametersConfig;

/**
 * Reservoir Type
 */
public enum ReservoirType {

    EXPONENTIALLY_DECAYING {
        @Override
        public Reservoir getReservoir(ReservoirParametersConfig reservoirParametersConfig) {
            return new ExponentiallyDecayingReservoir();
        }
    },

    UNIFORM {
        @Override
        public Reservoir getReservoir(ReservoirParametersConfig reservoirParametersConfig) {
            return new UniformReservoir(reservoirParametersConfig.getSize());
        }
    },

    SLIDING_WINDOW {
        @Override
        public Reservoir getReservoir(ReservoirParametersConfig reservoirParametersConfig) {
            return new SlidingWindowReservoir(reservoirParametersConfig.getSize());
        }
    },

    SLIDING_TIME_WINDOW {
        @Override
        public Reservoir getReservoir(ReservoirParametersConfig reservoirParametersConfig) {
            return new SlidingTimeWindowReservoir(reservoirParametersConfig.getWindow(),
                    reservoirParametersConfig.getWindowUnit());
        }
    },

    HDR_HISTOGRAM {
        @Override
        public Reservoir getReservoir(ReservoirParametersConfig reservoirParametersConfig) {
            Recorder recorder = new Recorder(reservoirParametersConfig.getNumberOfSignificantValueDigits());
            if (reservoirParametersConfig.isResetOnSnapshot()) {
                return new HdrHistogramResetOnSnapshotReservoir(recorder);
            } else {
                return new HdrHistogramReservoir(recorder);
            }
        }
    };


    public abstract Reservoir getReservoir(ReservoirParametersConfig reservoirParametersConfig);

}
