
<%
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
%>
<%@page import="java.io.OutputStreamWriter"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="org.wso2.carbon.metrics.data.common.MetricList"%>
<%@ page import="org.wso2.carbon.metrics.data.common.Metric"%>
<%@ page import="org.wso2.carbon.metrics.data.common.MetricType"%>
<%@ page import="org.wso2.carbon.metrics.data.common.MetricAttribute"%>
<%@ page import="org.wso2.carbon.metrics.data.common.MetricDataFormat"%>
<%@ page import="org.wso2.carbon.metrics.view.ui.MetricsViewClient"%>
<%@ page import="org.wso2.carbon.metrics.view.ui.MetricDataWrapper"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.CarbonUtils"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>

<%
    String source = request.getParameter("source");
    String from = request.getParameter("from");
    String to = request.getParameter("to");
    String type = request.getParameter("type");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
            CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MetricsViewClient metricsViewClient;
    try {
        metricsViewClient = new MetricsViewClient(cookie, backendServerURL, configContext);
        Gson gson = new Gson();
        MetricDataWrapper metricData = null;
        ArrayList<Metric> metrics = new ArrayList<Metric>();
        if ("Memory".equals(type)) {
            metrics = getMemoryMetrics();
        } else if ("CPU".equals(type)) {
            metrics.add(new Metric(MetricType.GAUGE, "jvm.os", "cpu.load.process", "Process CPU Load",
                    MetricAttribute.VALUE, MetricDataFormat.P));
            metrics.add(new Metric(MetricType.GAUGE, "jvm.os", "cpu.load.system", "System CPU Load",
                    MetricAttribute.VALUE, MetricDataFormat.P));
        } else if ("LoadAverage".equals(type)) {
            metrics.add(new Metric(MetricType.GAUGE, "jvm.os", "system.load.average", "System Load Average",
                    MetricAttribute.VALUE, null));
        } else if ("FileDescriptor".equals(type)) {
            metrics.add(new Metric(MetricType.GAUGE, "jvm.os", "file.descriptor.open.count",
                    "Open File Descriptor Count", MetricAttribute.VALUE, null));
            metrics.add(new Metric(MetricType.GAUGE, "jvm.os", "file.descriptor.max.count",
                    "Max File Descriptor Count", MetricAttribute.VALUE, null));
        } else if ("PhysicalMemory".equals(type)) {
            metrics.add(new Metric(MetricType.GAUGE, "jvm.os", "physical.memory.free.size",
                    "Free Physical Memory Size", MetricAttribute.VALUE, MetricDataFormat.B));
            metrics.add(new Metric(MetricType.GAUGE, "jvm.os", "physical.memory.total.size",
                    "Total Physical Memory Size", MetricAttribute.VALUE, MetricDataFormat.B));
            metrics.add(new Metric(MetricType.GAUGE, "jvm.os", "swap.space.free.size", "Free Swap Space Size",
                    MetricAttribute.VALUE, MetricDataFormat.B));
            metrics.add(new Metric(MetricType.GAUGE, "jvm.os", "swap.space.total.size", "Total Swap Space Size",
                    MetricAttribute.VALUE, MetricDataFormat.B));
            metrics.add(new Metric(MetricType.GAUGE, "jvm.os", "virtual.memory.committed.size",
                    "Committed Virtual Memory Size", MetricAttribute.VALUE, MetricDataFormat.B));
        } else if ("ClassLoading".equals(type)) {
            metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading", "loaded.current",
                    "Current Classes Loaded", MetricAttribute.VALUE, null));
            metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading", "loaded.total", "Total Classes Loaded",
                    MetricAttribute.VALUE, null));
            metrics.add(new Metric(MetricType.GAUGE, "jvm.class-loading", "unloaded.total",
                    "Total Classes Unloaded", MetricAttribute.VALUE, null));
        } else if ("Threading".equals(type)) {
            metrics.add(new Metric(MetricType.GAUGE, "jvm.threads", "count", "Live Threads",
                    MetricAttribute.VALUE, null));
            metrics.add(new Metric(MetricType.GAUGE, "jvm.threads", "daemon.count", "Daemon Threads",
                    MetricAttribute.VALUE, null));
        }

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
    private ArrayList<Metric> getMemoryMetrics() {
        ArrayList<Metric> metrics = new ArrayList<Metric>();
        addMemoryMetrics(metrics, "heap", "Heap");
        addMemoryMetrics(metrics, "non-heap", "Non-Heap");
        return metrics;
    }
%>
<%!
    private void addMemoryMetrics(ArrayList<Metric> metrics, String type, String displayType) {
        metrics.add(new Metric(MetricType.GAUGE, "jvm.memory", String.format("%s.init", type), String.format("%s Init",
                displayType), MetricAttribute.VALUE, MetricDataFormat.B));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.memory", String.format("%s.used", type), String.format("%s Used",
                displayType), MetricAttribute.VALUE, MetricDataFormat.B));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.memory", String.format("%s.committed", type), String.format(
                "%s Committed", displayType), MetricAttribute.VALUE, MetricDataFormat.B));
        metrics.add(new Metric(MetricType.GAUGE, "jvm.memory", String.format("%s.max", type), String.format("%s Max",
                displayType), MetricAttribute.VALUE, MetricDataFormat.B));

    }
%>
