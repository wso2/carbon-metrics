
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
        if ("Memory".equals(type)) {
            metricData = metricsViewClient.findLastJMXMemoryMetrics(source, from);
        } else if ("CPU".equals(type)) {
            metricData = metricsViewClient.findLastJMXCPULoadMetrics(source, from);
        } else if ("LoadAverage".equals(type)) {
            metricData = metricsViewClient.findLastJMXLoadAverageMetrics(source, from);
        } else if ("FileDescriptor".equals(type)) {
            metricData = metricsViewClient.findLastJMXFileDescriptorMetrics(source, from);
        } else if ("PhysicalMemory".equals(type)) {
            metricData = metricsViewClient.findLastJMXPhysicalMemoryMetrics(source, from);
        }

        if (metricData != null) {
            response.getWriter().write(gson.toJson(metricData));
        }
    } catch (Exception e) {
        return;
    }

    response.setContentType("application/json");
%>
