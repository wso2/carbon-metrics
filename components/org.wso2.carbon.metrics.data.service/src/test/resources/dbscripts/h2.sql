-- 
-- Copyright 2015 WSO2 Inc. (http://wso2.org)
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--     http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- 

CREATE TABLE IF NOT EXISTS METRIC_GAUGE (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    SOURCE VARCHAR(100) NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    VALUE VARCHAR(100) NOT NULL,
);

CREATE TABLE IF NOT EXISTS METRIC_COUNTER (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    SOURCE VARCHAR(100) NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    COUNT BIGINT NOT NULL,
);

CREATE TABLE IF NOT EXISTS METRIC_METER (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    SOURCE VARCHAR(100) NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    COUNT BIGINT NOT NULL,
    MEAN_RATE DOUBLE NOT NULL,
    M1_RATE DOUBLE NOT NULL,
    M5_RATE DOUBLE NOT NULL,
    M15_RATE DOUBLE NOT NULL,
    RATE_UNIT VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS METRIC_HISTOGRAM (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    SOURCE VARCHAR(100) NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    COUNT BIGINT NOT NULL,
    MAX DOUBLE NOT NULL,
    MEAN DOUBLE NOT NULL,
    MIN DOUBLE NOT NULL,
    STDDEV DOUBLE NOT NULL,
    P50 DOUBLE NOT NULL,
    P75 DOUBLE NOT NULL,
    P95 DOUBLE NOT NULL,
    P98 DOUBLE NOT NULL,
    P99 DOUBLE NOT NULL,
    P999 DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS METRIC_TIMER (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    SOURCE VARCHAR(100) NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    COUNT BIGINT NOT NULL,
    MAX DOUBLE NOT NULL,
    MEAN DOUBLE NOT NULL,
    MIN DOUBLE NOT NULL,
    STDDEV DOUBLE NOT NULL,
    P50 DOUBLE NOT NULL,
    P75 DOUBLE NOT NULL,
    P95 DOUBLE NOT NULL,
    P98 DOUBLE NOT NULL,
    P99 DOUBLE NOT NULL,
    P999 DOUBLE NOT NULL,
    MEAN_RATE DOUBLE NOT NULL,
    M1_RATE DOUBLE NOT NULL,
    M5_RATE DOUBLE NOT NULL,
    M15_RATE DOUBLE NOT NULL,
    RATE_UNIT VARCHAR(50) NOT NULL,
    DURATION_UNIT VARCHAR(50) NOT NULL
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.gc.PS-MarkSweep.count',
  '1'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.gc.PS-MarkSweep.time',
  '386'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.gc.PS-Scavenge.count',
  '21'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.gc.PS-Scavenge.time',
  '458'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.heap.committed',
  '478674944'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.heap.init',
  '268435456'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.heap.max',
  '954728448'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.heap.usage',
  '0.3472468183958419'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.heap.used',
  '331526416'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.non-heap.committed',
  '135790592'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.non-heap.init',
  '24576000'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.non-heap.max',
  '318767104'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.non-heap.usage',
  '0.2561725315294768'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.non-heap.used',
  '81659376'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.total.committed',
  '614465536'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.total.init',
  '293011456'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.total.max',
  '1273495552'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.memory.total.used',
  '413185792'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.os.cpu.load.process',
  '5.535875629239749E-4'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.os.cpu.load.system',
  '0.17192766073400548'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.os.file.descriptor.max.count',
  '65535'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.os.file.descriptor.open.count',
  '347'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.os.physical.memory.free.size',
  '174706688'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.os.physical.memory.total.size',
  '16514957312'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.os.swap.space.free.size',
  '8269930496'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.os.swap.space.total.size',
  '8270114816'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.os.system.load.average',
  '0.74'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714860,
  'jvm.os.virtual.memory.committed.size',
  '3719208960'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.gc.PS-MarkSweep.count',
  '1'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.gc.PS-MarkSweep.time',
  '386'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.gc.PS-Scavenge.count',
  '22'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.gc.PS-Scavenge.time',
  '472'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.heap.committed',
  '483393536'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.heap.init',
  '268435456'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.heap.max',
  '954728448'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.heap.usage',
  '0.1279627942960384'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.heap.used',
  '122169720'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.non-heap.committed',
  '135921664'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.non-heap.init',
  '24576000'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.non-heap.max',
  '318767104'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.non-heap.usage',
  '0.25640756205508586'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.non-heap.used',
  '81734296'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.total.committed',
  '619315200'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.total.init',
  '293011456'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.total.max',
  '1273495552'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.memory.total.used',
  '203904016'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.os.cpu.load.process',
  '0.0016532521209484446'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.os.cpu.load.system',
  '0.16232325429628017'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.os.file.descriptor.max.count',
  '65535'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.os.file.descriptor.open.count',
  '346'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.os.physical.memory.free.size',
  '168161280'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.os.physical.memory.total.size',
  '16514957312'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.os.swap.space.free.size',
  '8269930496'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.os.swap.space.total.size',
  '8270114816'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.os.system.load.average',
  '0.98'
);

INSERT INTO METRIC_GAUGE
(
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  'isurup-ThinkPad-T530',
  1427714920,
  'jvm.os.virtual.memory.committed.size',
  '3721314304'
);


COMMIT;

