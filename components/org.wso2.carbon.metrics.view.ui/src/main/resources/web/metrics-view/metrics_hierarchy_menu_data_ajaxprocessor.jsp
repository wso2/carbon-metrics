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

<%
    String source = request.getParameter("source");
    String from = request.getParameter("from");
    String to = request.getParameter("to");
    String path = request.getParameter("path");
    String type = request.getParameter("type");
    path = (path != null && path.trim().length() > 0) ? path : "";

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
            CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MetricsViewClient metricsViewClient;
    try {
        metricsViewClient = new MetricsViewClient(cookie, backendServerURL, configContext);
        Gson gson = new Gson();
        MetricDataWrapper metricData = null;

        switch (type) {
            case "hierarchy": {
                MetricHierarchyDataWrapper hierarchy = getHierarchyData(metricsViewClient, source, path);
                if (hierarchy != null) {
                    response.getWriter().write(gson.toJson(hierarchy));
                }
                break;
            }
            case "metrics": {
                ArrayList<Metric> metrics = getMetrics(metricsViewClient, source, path);
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
                break;
            }
            default: {
                return;
            }
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
    private ArrayList<Metric> getMetrics(MetricsViewClient client, String source, String path) {
        MetricHierarchyDataWrapper hierarchyData = getHierarchyData(client, source, path);
        ArrayList<Metric> metrics = new ArrayList<>();
        for (MetricMetaWrapper metricMeta : hierarchyData.getMetrics()) {
            metrics.add(new Metric(getMetricType(metricMeta.getType()), metricMeta.getName(),
                    metricMeta.getDisplayName(), MetricAttribute.VALUE, MetricDataFormat.B));
        }
        return metrics;
    }
%>

<%!
    private MetricType getMetricType(String type) {
        switch (type) {
            case "GAUGE":
                return MetricType.GAUGE;
            case "COUNTER":
                return MetricType.COUNTER;
            case "METER":
                return MetricType.METER;
            case "HISTOGRAM":
                return MetricType.HISTOGRAM;
            case "TIMER":
                return MetricType.TIMER;
            default:
                return null;
        }
    }
%>
