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
    SOURCE VARCHAR(255) NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    VALUE VARCHAR(100) NOT NULL,
);

CREATE TABLE IF NOT EXISTS METRIC_COUNTER (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    SOURCE VARCHAR(255) NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    COUNT BIGINT NOT NULL,
);

CREATE TABLE IF NOT EXISTS METRIC_METER (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    SOURCE VARCHAR(255) NOT NULL,
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
    SOURCE VARCHAR(255) NOT NULL,
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
    SOURCE VARCHAR(255) NOT NULL,
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
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  1,
  'carbon-server',
  1428567356013,
  'jvm.class-loading.loaded.current',
  '10159'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  2,
  'carbon-server',
  1428567356013,
  'jvm.class-loading.loaded.total',
  '10427'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  3,
  'carbon-server',
  1428567356013,
  'jvm.class-loading.unloaded.total',
  '268'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  4,
  'carbon-server',
  1428567356013,
  'jvm.gc.PS-MarkSweep.count',
  '10'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  5,
  'carbon-server',
  1428567356013,
  'jvm.gc.PS-MarkSweep.time',
  '3101'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  6,
  'carbon-server',
  1428567356013,
  'jvm.gc.PS-Scavenge.count',
  '37'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  7,
  'carbon-server',
  1428567356013,
  'jvm.gc.PS-Scavenge.time',
  '898'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  8,
  'carbon-server',
  1428567356013,
  'jvm.memory.heap.committed',
  '425721856'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  9,
  'carbon-server',
  1428567356013,
  'jvm.memory.heap.init',
  '268435456'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  10,
  'carbon-server',
  1428567356013,
  'jvm.memory.heap.max',
  '954728448'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  11,
  'carbon-server',
  1428567356013,
  'jvm.memory.heap.usage',
  '0.18403085858398974'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  12,
  'carbon-server',
  1428567356013,
  'jvm.memory.heap.used',
  '175699496'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  13,
  'carbon-server',
  1428567356013,
  'jvm.memory.non-heap.committed',
  '108986368'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  14,
  'carbon-server',
  1428567356013,
  'jvm.memory.non-heap.init',
  '24576000'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  15,
  'carbon-server',
  1428567356013,
  'jvm.memory.non-heap.max',
  '318767104'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  16,
  'carbon-server',
  1428567356013,
  'jvm.memory.non-heap.usage',
  '0.24130966788844058'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  17,
  'carbon-server',
  1428567356013,
  'jvm.memory.non-heap.used',
  '76921584'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  18,
  'carbon-server',
  1428567356013,
  'jvm.memory.total.committed',
  '534708224'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  19,
  'carbon-server',
  1428567356013,
  'jvm.memory.total.init',
  '293011456'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  20,
  'carbon-server',
  1428567356013,
  'jvm.memory.total.max',
  '1273495552'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  21,
  'carbon-server',
  1428567356013,
  'jvm.memory.total.used',
  '252621080'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  22,
  'carbon-server',
  1428567356013,
  'jvm.os.cpu.load.process',
  '8.337154529159198E-4'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  23,
  'carbon-server',
  1428567356013,
  'jvm.os.cpu.load.system',
  '0.2378590187169119'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  24,
  'carbon-server',
  1428567356013,
  'jvm.os.file.descriptor.max.count',
  '65535'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  25,
  'carbon-server',
  1428567356013,
  'jvm.os.file.descriptor.open.count',
  '313'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  26,
  'carbon-server',
  1428567356013,
  'jvm.os.physical.memory.free.size',
  '4270161920'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  27,
  'carbon-server',
  1428567356013,
  'jvm.os.physical.memory.total.size',
  '16514957312'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  28,
  'carbon-server',
  1428567356013,
  'jvm.os.swap.space.free.size',
  '7348232192'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  29,
  'carbon-server',
  1428567356013,
  'jvm.os.swap.space.total.size',
  '8270114816'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  30,
  'carbon-server',
  1428567356013,
  'jvm.os.system.load.average',
  '0.99'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  31,
  'carbon-server',
  1428567356013,
  'jvm.os.virtual.memory.committed.size',
  '3846098944'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  32,
  'carbon-server',
  1428567356013,
  'jvm.threads.count',
  '116'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  33,
  'carbon-server',
  1428567356013,
  'jvm.threads.daemon.count',
  '108'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  34,
  'carbon-server',
  1428567416013,
  'jvm.class-loading.loaded.current',
  '10159'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  35,
  'carbon-server',
  1428567416013,
  'jvm.class-loading.loaded.total',
  '10427'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  36,
  'carbon-server',
  1428567416013,
  'jvm.class-loading.unloaded.total',
  '268'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  37,
  'carbon-server',
  1428567416013,
  'jvm.gc.PS-MarkSweep.count',
  '10'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  38,
  'carbon-server',
  1428567416013,
  'jvm.gc.PS-MarkSweep.time',
  '3101'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  39,
  'carbon-server',
  1428567416013,
  'jvm.gc.PS-Scavenge.count',
  '38'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  40,
  'carbon-server',
  1428567416013,
  'jvm.gc.PS-Scavenge.time',
  '905'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  41,
  'carbon-server',
  1428567416013,
  'jvm.memory.heap.committed',
  '487063552'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  42,
  'carbon-server',
  1428567416013,
  'jvm.memory.heap.init',
  '268435456'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  43,
  'carbon-server',
  1428567416013,
  'jvm.memory.heap.max',
  '954728448'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  44,
  'carbon-server',
  1428567416013,
  'jvm.memory.heap.usage',
  '0.2880009541728875'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  45,
  'carbon-server',
  1428567416013,
  'jvm.memory.heap.used',
  '274962704'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  46,
  'carbon-server',
  1428567416013,
  'jvm.memory.non-heap.committed',
  '109117440'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  47,
  'carbon-server',
  1428567416013,
  'jvm.memory.non-heap.init',
  '24576000'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  48,
  'carbon-server',
  1428567416013,
  'jvm.memory.non-heap.max',
  '318767104'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  49,
  'carbon-server',
  1428567416013,
  'jvm.memory.non-heap.usage',
  '0.24154577757182874'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  50,
  'carbon-server',
  1428567416013,
  'jvm.memory.non-heap.used',
  '76996848'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  51,
  'carbon-server',
  1428567416013,
  'jvm.memory.total.committed',
  '596180992'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  52,
  'carbon-server',
  1428567416013,
  'jvm.memory.total.init',
  '293011456'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  53,
  'carbon-server',
  1428567416013,
  'jvm.memory.total.max',
  '1273495552'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  54,
  'carbon-server',
  1428567416013,
  'jvm.memory.total.used',
  '351959552'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  55,
  'carbon-server',
  1428567416013,
  'jvm.os.cpu.load.process',
  '0.006559582877806745'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  56,
  'carbon-server',
  1428567416013,
  'jvm.os.cpu.load.system',
  '0.24863342023379026'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  57,
  'carbon-server',
  1428567416013,
  'jvm.os.file.descriptor.max.count',
  '65535'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  58,
  'carbon-server',
  1428567416013,
  'jvm.os.file.descriptor.open.count',
  '315'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  59,
  'carbon-server',
  1428567416013,
  'jvm.os.physical.memory.free.size',
  '4164788224'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  60,
  'carbon-server',
  1428567416013,
  'jvm.os.physical.memory.total.size',
  '16514957312'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  61,
  'carbon-server',
  1428567416013,
  'jvm.os.swap.space.free.size',
  '7348236288'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  62,
  'carbon-server',
  1428567416013,
  'jvm.os.swap.space.total.size',
  '8270114816'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  63,
  'carbon-server',
  1428567416013,
  'jvm.os.system.load.average',
  '1.01'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  64,
  'carbon-server',
  1428567416013,
  'jvm.os.virtual.memory.committed.size',
  '3847151616'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  65,
  'carbon-server',
  1428567416013,
  'jvm.threads.count',
  '118'
);

INSERT INTO METRIC_GAUGE
(
  ID,
  SOURCE,
  TIMESTAMP,
  NAME,
  VALUE
)
VALUES
(
  66,
  'carbon-server',
  1428567416013,
  'jvm.threads.daemon.count',
  '110'
);

INSERT INTO METRIC_TIMER
(
  SOURCE,
  TIMESTAMP,
  NAME,
  "COUNT",
  MAX,
  MEAN,
  MIN,
  STDDEV,
  P50,
  P75,
  P95,
  P98,
  P99,
  P999,
  MEAN_RATE,
  M1_RATE,
  M5_RATE,
  M15_RATE,
  RATE_UNIT,
  DURATION_UNIT
)
VALUES
(
  'carbon-server',
  1428567356013,
  'database.read',
  12,
  0.29952799999999996,
  0.14383174999999995,
  0.07875399999999999,
  0.0768072716122688,
  0.109112,
  0.238786,
  0.29952799999999996,
  0.29952799999999996,
  0.29952799999999996,
  0.29952799999999996,
  0.23689358201410407,
  1.1336797265784357,
  2.065699143420139,
  2.282950618801713,
  'calls/second',
  'milliseconds'
);

INSERT INTO METRIC_TIMER
(
  SOURCE,
  TIMESTAMP,
  NAME,
  "COUNT",
  MAX,
  MEAN,
  MIN,
  STDDEV,
  P50,
  P75,
  P95,
  P98,
  P99,
  P999,
  MEAN_RATE,
  M1_RATE,
  M5_RATE,
  M15_RATE,
  RATE_UNIT,
  DURATION_UNIT
)
VALUES
(
  'carbon-server',
  1428567356013,
  'database.write',
  9,
  1.190064,
  0.5374895555555554,
  0.168406,
  0.3387105293413409,
  0.485568,
  0.543777,
  1.190064,
  1.190064,
  1.190064,
  1.190064,
  0.17773079479225823,
  0.8502597949338269,
  1.5492743575651042,
  1.7122129641012849,
  'calls/second',
  'milliseconds'
);

INSERT INTO METRIC_TIMER
(
  SOURCE,
  TIMESTAMP,
  NAME,
  "COUNT",
  MAX,
  MEAN,
  MIN,
  STDDEV,
  P50,
  P75,
  P95,
  P98,
  P99,
  P999,
  MEAN_RATE,
  M1_RATE,
  M5_RATE,
  M15_RATE,
  RATE_UNIT,
  DURATION_UNIT
)
VALUES
(
  'carbon-server',
  1428567416013,
  'database.read',
  12,
  0.29952799999999996,
  0.14383174999999995,
  0.07875399999999999,
  0.0768072716122688,
  0.109112,
  0.238786,
  0.29952799999999996,
  0.29952799999999996,
  0.29952799999999996,
  0.29952799999999996,
  0.1081167392037622,
  0.41705746428106877,
  1.6912514153249125,
  2.135716250371257,
  'calls/second',
  'milliseconds'
);

INSERT INTO METRIC_TIMER
(
  SOURCE,
  TIMESTAMP,
  NAME,
  "COUNT",
  MAX,
  MEAN,
  MIN,
  STDDEV,
  P50,
  P75,
  P95,
  P98,
  P99,
  P999,
  MEAN_RATE,
  M1_RATE,
  M5_RATE,
  M15_RATE,
  RATE_UNIT,
  DURATION_UNIT
)
VALUES
(
  'carbon-server',
  1428567416013,
  'database.write',
  9,
  1.190064,
  0.5374895555555554,
  0.168406,
  0.3387105293413409,
  0.485568,
  0.543777,
  1.190064,
  1.190064,
  1.190064,
  1.190064,
  0.0811002312465375,
  0.31279309821080165,
  1.2684385614936846,
  1.6017871877784422,
  'calls/second',
  'milliseconds'
);

COMMIT;

