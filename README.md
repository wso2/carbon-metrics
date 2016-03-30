WSO2 Carbon Metrics
===================

---
|  Branch | Build Status |
| :------ |:------------ | 
| master  | [![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=carbon-metrics)](https://wso2.org/jenkins/job/carbon-metrics/) |
---

"WSO2 Carbon Metrics" provides an API for WSO2 Carbon Components to use [Metrics library](https://dropwizard.github.io/metrics/).

For more information see JIRA [CARBON-15115](https://wso2.org/jira/browse/CARBON-15115)

## Enabling Metrics

The metrics feature can be enabled from `$CARBON_HOME/repository/conf/metrics.xml` and the reporters can also be configured from the same configuration file. 

The System Property `metrics.enabled` can be used to set the enabled status at startup. For example, use `-Dmetrics.enabled=true` to enable Metrics feature without editing the `metrics.xml` configuration file.


## Metric Levels

The WSO2 Carbon Metrics APIs associate a Level with each Metric. These levels are similar to the Levels used in Logging Libraries. 

Metrics Levels are organized from most specific to least:

  - OFF (most specific, no metrics)
  - INFO
  - DEBUG
  - TRACE (least specific, a lot of data)
  - ALL (least specific, all data)

The levels are configured in `$CARBON_HOME/repository/conf/metrics.properties` file.

Similar to [Apache Log4j](https://logging.apache.org/log4j/1.2/), the WSO2 Carbon Metrics implementation uses a Metric Hierarchy. The hierarchy is maintained via the Metric names.

The levels in `metrics.properties` can be configured to any hierarchy. For example, if we use `metric.level.jvm.memory.heap=INFO` in  `metrics.properties`, all metrics under `jvm.memory.heap` memory will have `INFO` as the configured level.

If there is no configured level for specific metric name hierarachy, the level in "`metrics.rootLevel`" will be used. The  System Property `metrics.rootLevel` can be used to override the configured root level in `metrics.properties` file. For example, use `-Dmetrics.rootLevel=INFO` to change the root level to `INFO`.


## Components

This repository has multiple components.

  - org.wso2.carbon.metrics.core - The public API for WSO2 Metrics. See Usage.
  - org.wso2.carbon.metrics.impl - Main implementation of Metric Service, which uses the [Metrics library](https://dropwizard.github.io/metrics/).
  - org.wso2.carbon.metrics.jdbc.reporter - A JDBC Reporter for Metrics Library.
  - org.wso2.carbon.metrics.common - A common component to read configurations.
  - org.wso2.carbon.metrics.data.service - A Web Service to get data reported by the JDBC Reporter.
  - org.wso2.carbon.metrics.view.ui - A Carbon UI component to display JMX Stats.

## Usage

All APIs are exposed via `org.wso2.carbon.metrics.core.MetricManager` class.

```
Meter meter = MetricManager.meter(Level.INFO, MetricManager.name(this.getClass(), "test-meter"));
meter.mark();

Timer timer = MetricManager.timer(Level.INFO, MetricManager.name(this.getClass(), "test-timer"));
Context context = timer.start();

Counter counter = MetricManager.counter(Level.INFO, MetricManager.name(this.getClass(), "test-counter"));
counter.inc();

Histogram histogram = MetricManager.histogram(Level.INFO, MetricManager.name(this.getClass(), "test-histogram"));
histogram.update(value);

Gauge<Integer> gauge = new Gauge<Integer>() {
    @Override
    public Integer getValue() {
        // Return a value
        return 1;
    }
};

MetricManager.gauge(Level.INFO, MetricManager.name(this.getClass(), "test-gauge"), gauge);
```

## Maven Dependency

In order to use WSO2 Carbon Metrics in your components, you need to add following dependency to your `pom.xml`

```
<dependency>
    <groupId>org.wso2.carbon.metrics</groupId>
    <artifactId>org.wso2.carbon.metrics.core</artifactId>
    <version>${carbon.metrics.version}</version>
</dependency>
```

## Adding Metrics to your products (based on WSO2 Carbon)

In your `p2-profile` module's `pom.xml`, add following under `<featureArtifacts>` in `p2-repo-gen` goal

```
<featureArtifactDef>
    org.wso2.carbon.metrics:org.wso2.carbon.metrics.feature:${carbon.metrics.version}
</featureArtifactDef>
```

Add following under `<features>` in `p2-profile-gen` goal

```
<feature>
    <id>org.wso2.carbon.metrics.feature.group</id>
    <version>${carbon.metrics.version}</version>
</feature>
```

## Copying required configuration files

You will have to copy following files to your product by adding relevant instructions in distribution module's `src/main/assembly/bin.xml`

For example:

```
<fileSets>
    <fileSet>
        <directory>
            ../p2-profile/target/wso2carbon-core-${carbon.kernel.version}/dbscripts/metrics/
        </directory>
        <outputDirectory>${project.artifactId}-${project.version}/dbscripts/metrics</outputDirectory>
        <includes>
            <include>**/*.sql</include>
        </includes>
    </fileSet>
</fileSets>

<files>
    <file>
        <source>../p2-profile/target/wso2carbon-core-${carbon.kernel.version}/repository/conf/metrics.xml</source>
        <outputDirectory>${project.artifactId}-${project.version}/repository/conf/</outputDirectory>
        <filtered>false</filtered>
    </file>
    <file>
        <source>../p2-profile/target/wso2carbon-core-${carbon.kernel.version}/repository/conf/metrics.properties</source>
        <outputDirectory>${project.artifactId}-${project.version}/repository/conf/</outputDirectory>
        <filtered>false</filtered>
    </file>
    <file>
        <source>../p2-profile/target/wso2carbon-core-${carbon.kernel.version}/repository/conf/datasources/metrics-datasources.xml</source>
        <outputDirectory>${project.artifactId}-${project.version}/repository/conf/datasources/</outputDirectory>
        <fileMode>644</fileMode>
    </file>
    <file>
        <source>../p2-profile/target/wso2carbon-core-${carbon.kernel.version}/repository/database/WSO2METRICS_DB.h2.db</source>
        <outputDirectory>${project.artifactId}-${project.version}/repository/database/</outputDirectory>
        <fileMode>644</fileMode>
    </file>
</files>
```

## License

Copyright (C) 2014 WSO2 Inc

Licensed under the Apache License, Version 2.0
