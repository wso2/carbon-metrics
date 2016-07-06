WSO2 Carbon Metrics
===================

---
|  Branch | Build Status |
| :------ |:------------ | 
| master  | [![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=carbon-metrics)](https://wso2.org/jenkins/job/carbon-metrics/) |
---

"WSO2 Carbon Metrics" provides an API for WSO2 Carbon Components to use the [Metrics library](http://metrics.dropwizard.io).

## Configuring Metrics

The metrics is configured via a [YAML](components/org.wso2.carbon.metrics.core/src/main/resources/metrics.yml) file. The Metrics is enabled by default and it can be disabled from the configuration. The Metrics is also configured to register a standard Java MBean for management operations.

All reporters are also configured using the same configuration file. By default, the JMX Reporter will be enabled. In WSO2 products, the JDBC Reporter will be enabled by default.

When using Metrics in a WSO2 product, the configuration file is available at `$CARBON_HOME/conf/metrics.yml`. If the Metrics is used in a standalone application, the configuration file can be given using the `metrics.conf` system property.  By default, the Metrics will look for metrics.yml file in the root directory (of the Java application) or in the classpath. This approach is also used for other configurations, such as the Metrics Level configuration and the JDBC Datasource Configuration.

## Metric Levels

The WSO2 Carbon Metrics APIs associate a Level with each Metric. These levels are similar to the Levels used in Logging Libraries. 

Metrics Levels are organized from most specific to least:

  - OFF (most specific, no metrics)
  - INFO
  - DEBUG
  - TRACE (least specific, a lot of data)
  - ALL (least specific, all data)

The levels are configured in `$CARBON_HOME/conf/metrics.properties` file. In a standalone application, the properties file can be specified using the `metrics.level.conf` system property.

Similar to [Apache Log4j](http://logging.apache.org/log4j), the WSO2 Carbon Metrics implementation uses a Metric Hierarchy. The hierarchy is maintained via the Metric names.

The levels in `metrics.properties` can be configured to any hierarchy. For example, if we use `metric.level.jvm.memory.heap=INFO` in `metrics.properties`, all metrics under `jvm.memory.heap` memory will have `INFO` as the configured level.

If there is no configured level for specific metric name hierarachy, the level in "`metrics.rootLevel`" will be used.

## Components

This repository has multiple components.

  - org.wso2.carbon.metrics.core - Provides the core Metrics and Management APIs. The main Metric Manager implementation uses the [Metrics library](http://metrics.dropwizard.io).
  - org.wso2.carbon.metrics.jdbc.reporter - A JDBC Reporter for Metrics Library.
  - org.wso2.carbon.metrics.das.reporter - A reporter to send metrics events to WSO2 Data Analytics Server (WSO2 DAS).
  - org.wso2.carbon.metrics.das.capp - The artifacts used to create the main Carbon Application (C-App). This C-App is required by the DAS Reporter. The artifacts in this C-App are the event streams, event receivers and the event stores for all Metric types.
  - org.wso2.carbon.metrics.jdbc.core - Extending Metrics Core to support the JDBC Reporter.
  - org.wso2.carbon.metrics.das.core - Extending Metrics Core to support the DAS Reporter.

## Maven Dependency

In order to use WSO2 Carbon Metrics in your components, you need to add following dependency to your `pom.xml`

```
<dependency>
    <groupId>org.wso2.carbon.metrics</groupId>
    <artifactId>org.wso2.carbon.metrics.core</artifactId>
    <version>${carbon.metrics.version}</version>
</dependency>
```

## Usage

The APIs to create Metrics are defined in `org.wso2.carbon.metrics.core.MetricService`. The APIs to manage Metrics, such as setting metric levels and adding reporters are defined in `org.wso2.carbon.metrics.core.MetricManagementService`.

In Carbon (OSGi) environment, these APIs are available as OSGi services.

See the sample [ServiceComponent](samples/org.wso2.carbon.metrics.sample.service/src/main/java/org/wso2/carbon/metrics/sample/service/internal/ServiceComponent.java).

When using Metrics in standalone application, the Metrics can be initialized as follows.

```
Metrics metrics = new Metrics.Builder().build();
metrics.activate();

MetricService metricService = metrics.getMetricService();
MetricManagementService metricManagementService = metrics.getMetricManagementService();

```

After getting a reference to the MetricService, the Metrics can be created as follows.

```
// Create a Gauge
metricService.gauge(MetricService.name("test", "gauge"), Level.INFO, () -> number);

// Create a Counter
Counter counter = metricService.counter(MetricService.name("test", "count"), Level.INFO);
// Increment
counter.inc();
// Decrement
counter.dec();

// Create a Meter
Meter meter = metricService.meter(MetricService.name("test", "meter"), Level.INFO);
// Mark an event
meter.mark();

// Create a Histogram
Histogram histogram = metricService.histogram(MetricService.name("test", "histogram"), Level.INFO);
// Update the histogram
histogram.update(value);

// Create a Timer
Timer timer = metricService.timer(MetricService.name("test", "timer"), Level.INFO);
// Start the timer
Timer.Context context = timer.start();
// Stop the timer
context.stop();
```

See the sample [RandomNumberServiceImpl](samples/org.wso2.carbon.metrics.sample.service/src/main/java/org/wso2/carbon/metrics/sample/service/internal/RandomNumberServiceImpl.java).


## License

Copyright (C) 2014-2016 WSO2 Inc.

Licensed under the Apache License, Version 2.0
