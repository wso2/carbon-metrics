<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@page import="com.google.gson.Gson" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.metrics.data.common.*" %>
<%@ page import="org.wso2.carbon.metrics.view.ui.MetricDataWrapper" %>
<%@ page import="org.wso2.carbon.metrics.view.ui.MetricHierarchyDataWrapper" %>
<%@ page import="org.wso2.carbon.metrics.view.ui.MetricsViewClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.metrics.view.ui.MetricMetaWrapper" %>
<%@ page import="java.util.List" %>

<%
    String source = request.getParameter("source");
    String from = request.getParameter("from");
    String to = request.getParameter("to");
    String path = request.getParameter("path");
    String type = request.getParameter("type");
    path = (path != null && path.trim().length() > 0) ? path : "";
    type = (type != null && type.trim().length() > 0) ? type : "";

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
            CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MetricsViewClient metricsViewClient;
    try {
        metricsViewClient = new MetricsViewClient(cookie, backendServerURL, configContext);
        Gson gson = new Gson();
        MetricDataWrapper metricData = null;
        ArrayList<Metric> metrics = getMetrics(metricsViewClient, source, path, type);
        MetricList metricList = new MetricList();
        metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));
        if (to != null && to.trim().length() > 0) {
            metricData = metricsViewClient.findMetricsByTimePeriod(metricList, source,
                    Long.parseLong(from), Long.parseLong(to));
        } else {
            metricData = metricsViewClient.findLastMetrics(metricList, source, from);
        }
        if (metricData != null) {
            response.getWriter().write(gson.toJson(metricData));
        }
    } catch (Exception e) {
        return;
    }
    response.setContentType("application/json");
%>

<%!
    private MetricHierarchyDataWrapper getHierarchyData(MetricsViewClient client, String source, String path) {
        MetricHierarchyDataWrapper metricHierarchy;
        try {
            metricHierarchy = client.getHierarchy(source, path);
        } catch (Exception e) {
            metricHierarchy = null;
        }
        return metricHierarchy;
    }
%>

<%!
    private ArrayList<Metric> getMetrics(MetricsViewClient client, String source, String path, String type) {
        MetricHierarchyDataWrapper hierarchyData = getHierarchyData(client, source, path);
        ArrayList<Metric> metrics = new ArrayList<Metric>();
        for (MetricMetaWrapper metricMeta : hierarchyData.getMetrics()) {
            if (type.equalsIgnoreCase(metricMeta.getDisplayName())) {
                metrics.addAll(getMetricsForChart(metricMeta));
            }
        }
        return metrics;
    }
%>

<%!
    private MetricType getMetricType(String type) {
        if ("GAUGE".equals(type)) {
            return MetricType.GAUGE;
        } else if ("COUNTER".equals(type)) {
            return MetricType.COUNTER;
        } else if ("METER".equals(type)) {
            return MetricType.METER;
        } else if ("HISTOGRAM".equals(type)) {
            return MetricType.HISTOGRAM;
        } else if ("TIMER".equals(type)) {
            return MetricType.TIMER;
        } else {
            return null;
        }
    }
%>

<%!
    private List<Metric> getMetricsForChart(MetricMetaWrapper metricMeta) {
        // TODO : add MetricDataFormat for GAUGE
        String type = metricMeta.getType();
        ArrayList<Metric> metrics = new ArrayList<Metric>();
        MetricAttribute[] attributes;
        if ("GAUGE".equals(type)) {
            attributes = new MetricAttribute[]{MetricAttribute.VALUE};
        } else if ("COUNTER".equals(type)) {
            attributes = new MetricAttribute[]{MetricAttribute.COUNT};
        } else if ("METER".equals(type)) {
            attributes = new MetricAttribute[]{MetricAttribute.COUNT, MetricAttribute.MEAN_RATE, MetricAttribute.M1_RATE, MetricAttribute.M5_RATE, MetricAttribute.M15_RATE};
        } else if ("HISTOGRAM".equals(type)) {
            attributes = new MetricAttribute[]{MetricAttribute.COUNT, MetricAttribute.MAX, MetricAttribute.MEAN, MetricAttribute.MIN, MetricAttribute.STDDEV, MetricAttribute.P50, MetricAttribute.P75, MetricAttribute.P95, MetricAttribute.P98, MetricAttribute.P99, MetricAttribute.P999};
        } else if ("TIMER".equals(type)) {
            attributes = new MetricAttribute[]{MetricAttribute.COUNT, MetricAttribute.MEAN_RATE, MetricAttribute.M1_RATE, MetricAttribute.M5_RATE, MetricAttribute.M15_RATE, MetricAttribute.MAX, MetricAttribute.MEAN, MetricAttribute.MIN, MetricAttribute.STDDEV, MetricAttribute.P50, MetricAttribute.P75, MetricAttribute.P95, MetricAttribute.P98, MetricAttribute.P99, MetricAttribute.P999};
        } else {
            attributes = new MetricAttribute[0];
        }

        for (MetricAttribute attribute : attributes) {
            metrics.add(new Metric(getMetricType(type), metricMeta.getName(), String.format("%s [%s]", metricMeta.getDisplayName(), attribute), attribute, MetricDataFormat.B));
        }
        return metrics;
    }
%>
