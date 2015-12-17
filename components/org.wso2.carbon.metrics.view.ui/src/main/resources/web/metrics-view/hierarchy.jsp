
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.metrics.view.ui.MetricsViewClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.metrics.view.ui.MetricHierarchyDataWrapper" %>
<%@ page import="org.wso2.carbon.metrics.view.ui.ChartView" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="org.wso2.carbon.metrics.view.ui.MetricMetaWrapper" %>

<%!
    private Map<String, ChartView> getViewMap(MetricMetaWrapper[] metas) {
        Map<String, ChartView> viewMap = new LinkedHashMap<String, ChartView>();
        int metasLength = metas.length;
        String[] charts = new String[metasLength];
        for (int i = 0; i < metasLength; i++) {
            charts[i] = metas[i].getDisplayName();
        }
        viewMap.put("Metrics Hierarchy", new ChartView(true, charts));
        return viewMap;
    }
%>

<div>
    <!-- Removed head tag. This page is rendered within the body tag in Management Console -->
    <!-- jQuery UI styles -->
    <link href="plugins/jquery-ui/jquery-ui.structure.min.css" type="text/css" rel="stylesheet" property="stylesheet"/>
    <link href="plugins/jquery-ui/jquery-ui.theme.min.css" type="text/css" rel="stylesheet" property="stylesheet"/>
    <!-- jQuery UI Timepicker styles -->
    <link href="plugins/jquery-ui/jquery-ui-timepicker-addon.min.css" type="text/css" rel="stylesheet"
          property="stylesheet"/>
    <!-- igviz styles -->
    <link href="plugins/igviz/igviz.css" type="text/css" rel="stylesheet" property="stylesheet"/>
    <!-- Metrics UI styles -->
    <link href="css/metrics.css" type="text/css" rel="stylesheet" property="stylesheet"/>

    <%
        String source = request.getParameter("source");
        String path = request.getParameter("path");
        String region = request.getParameter("region");
        String item = request.getParameter("item");

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        Map<String, ChartView> viewMap;
        MetricsViewClient metricsViewClient;
        MetricHierarchyDataWrapper hierarchy;
        String[] sources = null;
        String[] subLevels = null;
        try {
            metricsViewClient = new MetricsViewClient(cookie, backendServerURL, configContext);
            sources = metricsViewClient.getAllSources();
            path = (path != null && path.trim().length() > 0) ? path : "";
            source = (source != null && source.trim().length() > 0) ? source : sources[0];
            hierarchy = metricsViewClient.getHierarchy(source, path);
            subLevels = hierarchy.getChildren();
            viewMap = getViewMap(hierarchy.getMetrics());
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
            return;
        }
    %>

    <fmt:bundle basename="org.wso2.carbon.metrics.view.ui.i18n.Resources">

        <carbon:breadcrumb label="metrics.hierarchy.view"
                           resourceBundle="org.wso2.carbon.metrics.view.ui.i18n.Resources"
                           topPage="true"
                           request="<%=request%>"/>
        <div id="middle">
            <h2>
                <fmt:message key="metrics.hierarchy.view"/>
            </h2>

            <div id="workArea" class="metricsView">
                <form id="formInput">
                    <table border="0" class="styledLeft">
                        <tbody>
                        <tr>
                            <td>
                                <table id="metricsViewInputTable" class="normal" style="width: 100%">
                                    <tr>
                                        <td style="width: 10%;">
                                            <fmt:message key="metrics.source.tip" var="msgKeySourceTip"/>
                                            <label for="source" title="${msgKeySourceTip}">
                                                <fmt:message key="metrics.source"/>
                                            </label>
                                        </td>
                                        <td>
                                            <select name="source" id="source" title="${msgKeySourceTip}">
                                                <%
                                                    for (String s : sources) {
                                                %>
                                                <option value="<%=s%>"><%=s%>
                                                </option>
                                                <%
                                                    }
                                                %>
                                            </select>
                                        </td>
                                        <td style="width: 5%; text-align: right;">
                                            <fmt:message key="metrics.from.tip" var="msgKeyFromTip"/>
                                            <label for="fromTime" class="customRange" title="${msgKeyFromTip}">
                                                <fmt:message key="metrics.from"/>
                                            </label>
                                        </td>
                                        <td style="width: 10%;">
                                            <input type="text" id="fromTime" name="fromTime"
                                                   class="customRange inputTime" title="${msgKeyFromTip}"/>
                                        </td>
                                        <td style="width: 5%; text-align: right;">
                                            <fmt:message key="metrics.to.tip" var="msgKeyToTip"/>
                                            <label for="toTime" class="customRange" title="${msgKeyToTip}">
                                                <fmt:message key="metrics.to"/>
                                            </label>
                                        </td>
                                        <td style="width: 10%;">
                                            <input type="text" id="toTime" name="toTime"
                                                   class="customRange inputTime" title="${msgKeyToTip}"/>
                                        </td>
                                        <td style="width: 20%; text-align: right;">
                                            <fmt:message key="metrics.fromselect.tip" var="msgKeyFromSelectTip"/>
                                            <select name="from" id="from" title="${msgKeyFromSelectTip}">
                                                <option value="-5m" selected="selected">View last 5 minutes</option>
                                                <option value="-15m">View last 15 minutes</option>
                                                <option value="-1h">View last 1 hour</option>
                                                <option value="-2h">View last 2 hours</option>
                                                <option value="-6h">View last 6 hours</option>
                                                <option value="-12h">View last 12 hours</option>
                                                <option value="-24h">View last 24 hours</option>
                                                <option value="-2d">View last 2 days</option>
                                                <option value="-7d">View last 7 days</option>
                                                <option value="custom">View custom time frame</option>
                                            </select>
                                        </td>
                                        <td style="width: 10%; text-align: right;">
                                            <fmt:message key="metrics.autoupdate.tip" var="msgKeyAutoUpdateTip"/>
                                            <label for="autoUpdateButton" title="${msgKeyAutoUpdateTip}">
                                                <fmt:message key="metrics.autoupdate"/>
                                            </label>
                                            <input type="checkbox" id="autoUpdateButton"/>
                                        </td>
                                        <td style="text-align: right;">
                                            <fmt:message key="metrics.reload.tip" var="msgKeyReloadTip"/>
                                            <button id="reloadButton" type="button" title="${msgKeyReloadTip}">
                                                <fmt:message key="metrics.reload"/>
                                            </button>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <input type="hidden" id="path" name="path" value="<%=path%>"/>
                </form>
                <br/>

                <table border="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th>
                            <fmt:message key="metrics.hierarchy.path.heading"/>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>
                            <a href="hierarchy.jsp?region=<%=region.replaceAll(" ","%20")%>&item=<%=item.replaceAll(" ","%20")%>&source=<%=source.replaceAll(" ","%20")%>">
                                <fmt:message key="metrics.hierarchy.root.level"/>
                            </a>
                            <%
                                StringBuilder breadCrumbBuilder = new StringBuilder();
                                String[] split = path.split("\\.");
                                for (int i = 0, splitLength = split.length; i < splitLength; i++) {
                                    String breadcrumb = split[i];
                                    breadCrumbBuilder.append(breadcrumb);
                                    if (!breadcrumb.isEmpty()) {
                            %>
                            &nbsp;>&nbsp;
                            <a href="hierarchy.jsp?region=<%=region.replaceAll(" ","%20")%>&item=<%=item.replaceAll(" ","%20")%>&source=<%=source.replaceAll(" ","%20")%>&path=<%=breadCrumbBuilder.toString().replaceAll(" ","%20")%>"><%=breadcrumb%>
                            </a>
                            <%
                                    }
                                    if (i != splitLength - 1) {
                                        breadCrumbBuilder.append(".");
                                    }
                                }
                            %>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <br/>

                <% if (subLevels != null && subLevels.length > 0) { %>
                <table class="styledLeft" id="subTypeTable" style="width:100% !important;">
                    <thead>
                    <tr>
                        <th>
                            <fmt:message key="metrics.hierarchy.sub.levels"/>
                        </th>
                    </tr>
                    </thead>
                    <% for (String subLevel : subLevels) { %>
                    <tr>
                        <td>
                            <a href="hierarchy.jsp?region=<%=region.replaceAll(" ","%20")%>&item=<%=item.replaceAll(" ","%20")%>&source=<%=source.replaceAll(" ","%20")%>&path=<%=subLevel.replaceAll(" ","%20")%>"><%=subLevel.substring(subLevel.lastIndexOf('.') + 1)%>
                            </a>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </table>
                <% }%>
                <br/>

                <div id="chartHolder"></div>
            </div>
        </div>

        <script id="chartTemplate" type="text/x-handlebars-template">
            <div id="chart{{type}}">
                <table border="1" class="styledLeft">
                    <thead>
                    <tr>
                        <th>{{title}}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRow">
                                <span id="msgNoData{{type}}" style="display: none;">
                                    <fmt:message key="metrics.hierarchy.nodata"/>
                                </span>
                            <div id="toggle{{type}}"></div>
                            <div id="igviz{{type}}" class="igvizChart"></div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <br/>
        </script>
    </fmt:bundle>

    <!-- Initialize scripts for charts -->
    <script id="initialSetup" type="text/javascript">
        var views = {};
        var dataPageUrl = 'metrics_hierarchy_menu_data_ajaxprocessor.jsp';
        <fmt:bundle basename="org.wso2.carbon.metrics.view.ui.i18n.Resources">
        <%
        for (String key : viewMap.keySet()) {
            ChartView chartView = viewMap.get(key);
        %>
        chartNames = [];
        chartTitles = [];
        chartViewName = "<%=key%>";
        views["<%=key%>"] = {
            name: chartViewName,
            charts: chartNames,
            titles: chartTitles,
            visible: <%=chartView.isVisible()%>
        };
        <%
            String[] charts = chartView.getCharts();
            for (String chart : charts) {
        %>
        chartNames.push('<%=chart%>');
        chartTitles.push('<%=chart%>');
        <%
            }
        }
        %>
        </fmt:bundle>
    </script>

    <!-- Scripts required for charts -->
    <script src="plugins/d3/d3.min.js"></script>
    <script src="plugins/vega/vega.min.js"></script>
    <script src="plugins/igviz/igviz.js"></script>
    <!-- Script required for chart templates -->
    <script src="plugins/handlebars/handlebars-v3.0.0.js"></script>
    <!-- jQuery -->
    <script src="plugins/jquery/jquery-2.1.4.min.js"></script>
    <!-- jQuery Cookie Plugin -->
    <script src="plugins/jquery-plugins/jquery.cookie.js"></script>
    <!-- jQuery UI Custom Download -->
    <script src="plugins/jquery-ui/jquery-ui.min.js"></script>
    <!-- jQuery UI Timepicker plugin -->
    <script src="plugins/jquery-ui/jquery-ui-timepicker-addon.min.js"></script>
    <!-- Metrics UI script -->
    <script src="js/metrics.ui.js"></script>
    <script type="text/javascript">
        alternateTableRows('subTypeTable', 'tableEvenRow', 'tableOddRow');
    </script>
</div>
