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
package org.wso2.carbon.metrics.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.wso2.carbon.metrics.api.dao.MetricDataProcessor;
import org.wso2.carbon.metrics.api.dao.ReporterDAO;

/**
 * Description about ReporterService
 */
public class ReporterService {

    // Do not query for sources.. Let those values to be shown as in a stack

    private final String PREFIX_METRIC_GAUGE = "Gauge";
    private final String PREFIX_METRIC_COUNTER = "Counter";
    private final String PREFIX_METRIC_METER = "Meter";
    private final String PREFIX_METRIC_HISTOGRAM = "Histogram";
    private final String PREFIX_METRIC_TIMER = "Timer";

    private final Metric METRIC_GAUGE = createMetric(PREFIX_METRIC_GAUGE, PREFIX_METRIC_GAUGE, false, true, true);
    private final Metric METRIC_COUNTER = createMetric(PREFIX_METRIC_COUNTER, PREFIX_METRIC_COUNTER, false, true, true);
    private final Metric METRIC_METER = createMetric(PREFIX_METRIC_METER, PREFIX_METRIC_METER, false, true, true);
    private final Metric METRIC_HISTOGRAM = createMetric(PREFIX_METRIC_HISTOGRAM, PREFIX_METRIC_HISTOGRAM, false, true,
            true);
    private final Metric METRIC_TIMER = createMetric(PREFIX_METRIC_TIMER, PREFIX_METRIC_TIMER, false, true, true);

    private ReporterDAO reporterDAO;

    private Map<String, MetricType> metricTypeMap = new HashMap<String, MetricType>();
    private Map<String, MetricAttribute> metricAttributeMap = new HashMap<String, MetricAttribute>();

    public ReporterService() {
        metricTypeMap.put(PREFIX_METRIC_GAUGE, MetricType.GAUGE);
        metricTypeMap.put(PREFIX_METRIC_COUNTER, MetricType.COUNTER);
        metricTypeMap.put(PREFIX_METRIC_METER, MetricType.METER);
        metricTypeMap.put(PREFIX_METRIC_HISTOGRAM, MetricType.HISTOGRAM);
        metricTypeMap.put(PREFIX_METRIC_TIMER, MetricType.TIMER);

        metricAttributeMap.put("value", MetricAttribute.VALUE);
        metricAttributeMap.put("count", MetricAttribute.COUNT);
        metricAttributeMap.put("meanRate", MetricAttribute.MEAN_RATE);
        metricAttributeMap.put("m1Rate", MetricAttribute.M1_RATE);
        metricAttributeMap.put("m5Rate", MetricAttribute.M5_RATE);
        metricAttributeMap.put("m15Rate", MetricAttribute.M15_RATE);
        metricAttributeMap.put("max", MetricAttribute.MAX);
        metricAttributeMap.put("mean", MetricAttribute.MEAN);
        metricAttributeMap.put("min", MetricAttribute.MIN);
        metricAttributeMap.put("stddev", MetricAttribute.STDDEV);
        metricAttributeMap.put("p50", MetricAttribute.P50);
        metricAttributeMap.put("p75", MetricAttribute.P75);
        metricAttributeMap.put("p95", MetricAttribute.P95);
        metricAttributeMap.put("p98", MetricAttribute.P98);
        metricAttributeMap.put("p99", MetricAttribute.P99);
        metricAttributeMap.put("p999", MetricAttribute.P999);
    }

    @PostConstruct
    public void init() {
        DataSource dataSource = null;
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup("jdbc/WSO2MetricsDB");
        } catch (NamingException e) {
            // if (logger.isWarnEnabled()) {
            // logger.warn(String.format("Error when looking up the Data Source: '%s'. The JDBC a will not be enabled",
            // dataSourceName));
            // }
            // return null;
        }
        setReporterDAO(new ReporterDAO(dataSource));
    }

    public void setReporterDAO(ReporterDAO reporterDAO) {
        this.reporterDAO = reporterDAO;
    }

    @GET
    @Path("metrics/find")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Metric> metricsFind(@QueryParam("query") String query) {
        String regex = "\\.[\\S\\.]+\\*";
        List<Metric> metricTypes = new ArrayList<Metric>();
        if ("*".equals(query)) {
            metricTypes.add(METRIC_GAUGE);
            metricTypes.add(METRIC_COUNTER);
            metricTypes.add(METRIC_METER);
            metricTypes.add(METRIC_HISTOGRAM);
            metricTypes.add(METRIC_TIMER);
            return metricTypes;
            // } else if (PREFIX_METRIC_GAUGE.equals(query)) {
            // metricTypes.add(GAUGE);
            // return metricTypes;
            // } else if (PREFIX_METRIC_COUNTER.equals(query)) {
            // metricTypes.add(COUNTER);
            // return metricTypes;
            // } else if (PREFIX_METRIC_METER.equals(query)) {
            // metricTypes.add(METER);
            // return metricTypes;
            // } else if (PREFIX_METRIC_HISTOGRAM.equals(query)) {
            // metricTypes.add(HISTOGRAM);
            // return metricTypes;
            // } else if (PREFIX_METRIC_TIMER.equals(query)) {
            // metricTypes.add(TIMER);
            // return metricTypes;
        } else if ((PREFIX_METRIC_GAUGE + ".*").equals(query)) {
            return getMetricNames(MetricType.GAUGE);
        } else if ((PREFIX_METRIC_COUNTER + ".*").equals(query)) {
            return getMetricNames(MetricType.COUNTER);
        } else if ((PREFIX_METRIC_METER + ".*").equals(query)) {
            return getMetricNames(MetricType.METER);
        } else if ((PREFIX_METRIC_HISTOGRAM + ".*").equals(query)) {
            return getMetricNames(MetricType.HISTOGRAM);
        } else if ((PREFIX_METRIC_TIMER + ".*").equals(query)) {
            return getMetricNames(MetricType.TIMER);
            // } else if (query.startsWith(PREFIX_METRIC_GAUGE) || query.startsWith(PREFIX_METRIC_COUNTER)
            // || query.startsWith(PREFIX_METRIC_METER) || query.startsWith(PREFIX_METRIC_GAUGE)
            // || query.startsWith(PREFIX_METRIC_GAUGE)) {
        } else if (query.matches(PREFIX_METRIC_GAUGE + regex)) {
            return getMetricAttributes(MetricType.GAUGE);
        } else if (query.matches(PREFIX_METRIC_COUNTER + regex)) {
            return getMetricAttributes(MetricType.COUNTER);
        } else if (query.matches(PREFIX_METRIC_METER + regex)) {
            return getMetricAttributes(MetricType.METER);
        } else if (query.matches(PREFIX_METRIC_HISTOGRAM + regex)) {
            return getMetricAttributes(MetricType.HISTOGRAM);
        } else if (query.matches(PREFIX_METRIC_TIMER + regex)) {
            return getMetricAttributes(MetricType.TIMER);
        } else {
            // return the same query
            metricTypes.add(createMetric(query, query, true, false, false));
        }

        return metricTypes;
    }

    private List<Metric> getMetricNames(MetricType metricType) {
        List<String> names = reporterDAO.queryMetricNames(metricType);

        if (names == null || names.isEmpty()) {
            return null;
        }

        List<Metric> metrics = new ArrayList<Metric>();

        for (String name : names) {
            metrics.add(createMetric(name, name, false, true, true));
        }

        return metrics;
    }

    private List<Metric> getMetricAttributes(MetricType metricType) {
        List<String> attributes = new ArrayList<String>();

        switch (metricType) {
        case GAUGE:
            attributes.add("value");
            break;
        case COUNTER:
            attributes.add("count");
            break;
        case METER:
            attributes.add("count");
            attributes.add("meanRate");
            attributes.add("m1Rate");
            attributes.add("m5Rate");
            attributes.add("m15Rate");
            break;
        case HISTOGRAM:
            attributes.add("count");
            attributes.add("max");
            attributes.add("mean");
            attributes.add("min");
            attributes.add("stddev");
            attributes.add("p50");
            attributes.add("p75");
            attributes.add("p95");
            attributes.add("p98");
            attributes.add("p99");
            attributes.add("p999");
            break;
        case TIMER:
            attributes.add("count");
            attributes.add("max");
            attributes.add("mean");
            attributes.add("min");
            attributes.add("stddev");
            attributes.add("p50");
            attributes.add("p75");
            attributes.add("p95");
            attributes.add("p98");
            attributes.add("p99");
            attributes.add("p999");
            attributes.add("meanRate");
            attributes.add("m1Rate");
            attributes.add("m5Rate");
            attributes.add("m15Rate");
            break;
        }

        List<Metric> metrics = new ArrayList<Metric>();

        for (String attribute : attributes) {
            metrics.add(createMetric(attribute, attribute, true, false, false));
        }

        return metrics;
    }

    private Metric createMetric(String id, String text, boolean leaf, boolean allowChildren, boolean expandable) {
        Metric metric = new Metric();
        metric.setId(id);
        metric.setText(text);
        metric.setLeaf((byte) (leaf ? 1 : 0));
        metric.setAllowChildren((byte) (allowChildren ? 1 : 0));
        metric.setExpandable((byte) (expandable ? 1 : 0));
        return metric;
    }

    // See http://graphite.readthedocs.org/en/latest/render_api.html
    @GET
    @Path("render")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MetricData> renderMetrics(@QueryParam("target") String target, @QueryParam("format") String format,
            @QueryParam("from") String from, @QueryParam("until") String until,
            @QueryParam("maxDataPoints") int maxDataPoints) {

        List<MetricData> datapoints = new ArrayList<MetricData>();

        if (target == null || target.isEmpty()) {
            return datapoints;
        }

        if (from == null || from.isEmpty()) {
            return datapoints;
        }

        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        Pattern pattern = Pattern.compile("(\\-?\\d+)([hdm](in)?)");

        Matcher matcher = pattern.matcher(from);

        long startTime = currentTimeSeconds;
        long endTime = currentTimeSeconds;

        if (matcher.find()) {
            int count = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            if ("min".equals(unit)) {
                startTime = currentTimeSeconds + (count * 60);
            } else if ("h".equals(unit)) {
                startTime = currentTimeSeconds + (count * 60 * 60);
            } else if ("d".equals(unit)) {
                startTime = currentTimeSeconds + (count * 60 * 60 * 24);
            }
        } else if (from.matches("\\d+")) {
            startTime = Integer.parseInt(from);
        } else {
            return datapoints;
        }

        if (until != null & until.matches("\\d+")) {
            endTime = Integer.parseInt(until);
        }

        DatapointsProcessor processor = new DatapointsProcessor(target);

        MetricType metricType;

        int firstDot = target.indexOf('.');

        if (firstDot >= 0) {
            metricType = metricTypeMap.get(target.substring(0, firstDot));
        } else {
            return datapoints;
        }

        MetricAttribute metricAttribute;

        int lastDot = target.lastIndexOf('.');

        if (lastDot >= 0) {
            metricAttribute = metricAttributeMap.get(target.substring(lastDot + 1, target.length()));
        } else {
            return datapoints;
        }

        if (firstDot == lastDot) {
            // There is only one dot
            return datapoints;
        }

        if (metricType == null || metricAttribute == null) {
            return datapoints;
        }

        String name = target.substring(firstDot + 1, lastDot);

        reporterDAO.queryMetricDataPoints(metricType, name, metricAttribute, startTime, endTime, maxDataPoints,
                processor);

        return processor.getResult();
        // GET
        // /org.wso2.carbon.metrics.jdbc.rest.api/render?target=Timer.test.another.timer.p999&from=-6h&until=now&format=json&maxDataPoints=1879
        // HTTP/1.1

    }

    private class DatapointsProcessor implements MetricDataProcessor<List<MetricData>> {

        private List<MetricData> result = new ArrayList<MetricData>();

        private Map<String, MetricData> metricDataMap = new HashMap<String, MetricData>();

        private String target;

        /**
         * 
         */
        public DatapointsProcessor(String target) {
            // TODO Auto-generated constructor stub
            this.target = target;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.wso2.carbon.metrics.api.dao.MetricDataProcessor#process(java.lang.String, java.lang.String, long,
         * java.math.BigDecimal)
         */
        @Override
        public void process(String source, String name, long timestamp, BigDecimal value) {
            String target = source + "|" + this.target;
            MetricData metricData = metricDataMap.get(target);
            List<BigDecimal[]> datapoints;
            if (metricData == null) {
                metricData = new MetricData();
                datapoints = new ArrayList<BigDecimal[]>();
                metricData.setTarget(target);
                metricData.setDatapoints(datapoints);
                result.add(metricData);
                metricDataMap.put(target, metricData);
            } else {
                datapoints = metricData.getDatapoints();
            }

            datapoints.add(new BigDecimal[] { value, BigDecimal.valueOf(timestamp) });
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.wso2.carbon.metrics.api.dao.MetricDataProcessor#getResult()
         */
        @Override
        public List<MetricData> getResult() {
            return result;
        }

    }
}
