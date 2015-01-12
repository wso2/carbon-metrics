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
```

## License

Copyright (C) 2015 WSO2 Inc

Licensed under the Apache License, Version 2.0
