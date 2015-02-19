WSO2 Carbon Metrics
===================

This is an API for WSO2 Carbon to use [Metrics library](https://dropwizard.github.io/metrics/).

For more information see JIRA [CARBON-15115](https://wso2.org/jira/browse/CARBON-15115)

## Components

There are two components. The `org.wso2.carbon.metrics.manager` component has the public API for WSO2 Metrics. The `org.wso2.carbon.metrics.impl` component has the implementation, which uses the Metrics library.

## Usage

All APIs are exposed via `org.wso2.carbon.metrics.manager.MetricManager` class.

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
    <artifactId>org.wso2.carbon.metrics.manager</artifactId>
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

Copyright (C) 2015 WSO2 Inc

Licensed under the Apache License, Version 2.0
