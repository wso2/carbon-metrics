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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon"%>
<%@ page import="org.wso2.carbon.metrics.view.ui.MetricsViewClient"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<!-- igviz styles -->
<link href="plugins/igviz/igviz.css" rel="stylesheet" />
<link href="css/metrics.css" rel="stylesheet">

<title>View Metrics</title>

</head>
<body>

	<%
	    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
	    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
	            CarbonConstants.CONFIGURATION_CONTEXT);
	    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	    MetricsViewClient metricsViewClient;
	    String[] sources = null;
	    try {
	        metricsViewClient = new MetricsViewClient(cookie, backendServerURL, configContext);
	        sources = metricsViewClient.getAllSources();
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

		<carbon:breadcrumb label="metrics.view" resourceBundle="org.wso2.carbon.metrics.view.ui.i18n.Resources" topPage="true"
			request="<%=request%>" />
		<div id="middle">
			<h2>
				<fmt:message key="metrics.view" />
			</h2>
			<div id="workArea">
				<form id="formInput">
					<table border="0" class="styledLeft">
						<tbody>
							<tr>
								<td>
									<table class="normal" style="width: 100%">
										<tr>
											<td style="width: 5%; padding-right: 2px !important;"><fmt:message key="metrics.source" /></td>
											<td><select name="source" id="source">

													<%
												        for (String source : sources) {
													%>
													<option value="<%=source%>"><%=source%></option>
													<%
													    }
													%>

											</select></td>
											<td style="width: 25px; padding-right: 2px !important;"><a id="refreshButton" class="icon-link"
												style="background-image: url(images/refresh.png);" href="javascript:plotCharts()"></a></td>
											<td style="width: 10%;"><select name="from" id="from">
													<option value="-5m">Last 5 minutes</option>
													<option value="-15m">Last 15 minutes</option>
													<option value="-1h">Last 1 hour</option>
													<option value="-2h">Last 2 hours</option>
													<option value="-6h">Last 6 hours</option>
													<option value="-12h">Last 12 hours</option>
													<option value="-24h" selected="selected">Last 24 hours</option>
													<option value="-2d">Last 2 days</option>
													<option value="-7d">Last 7 days</option>
													<option value="-30d">Last 30 days</option>
											</select></td>
										</tr>
									</table>
								</td>
							</tr>
						</tbody>
					</table>
				</form>

 				<br/>
 
				<table border="1" class="styledLeft">
					<tbody>
						<tr>
							<td class="formRow">
								<div id="toggleMemory"></div>
								<div id="igvizMemory" class="igvizChart"></div>
							</td>
						</tr>
					</tbody>
				</table>

 				<br/>

				<table border="1" class="styledLeft">
					<tbody>
						<tr>
							<td class="formRow">
								<div id="toggleCPU"></div>
								<div id="igvizCPU" class="igvizChart"></div>
							</td>
						</tr>
					</tbody>
				</table>

 				<br/>

				<table border="1" class="styledLeft">
					<tbody>
						<tr>
							<td class="formRow">
								<div id="toggleLoadAverage"></div>
								<div id="igvizLoadAverage" class="igvizChart"></div>
							</td>
						</tr>
					</tbody>
				</table>
				
                <br/>

                <table border="1" class="styledLeft">
                    <tbody>
                        <tr>
                            <td class="formRow">
                                <div id="togglePhysicalMemory"></div>
                                <div id="igvizPhysicalMemory" class="igvizChart"></div>
                            </td>
                        </tr>
                    </tbody>
                </table>
				
                <br/>

                <table border="1" class="styledLeft">
                    <tbody>
                        <tr>
                            <td class="formRow">
                                <div id="toggleFileDescriptor"></div>
                                <div id="igvizFileDescriptor" class="igvizChart"></div>
                            </td>
                        </tr>
                    </tbody>
                </table>
			</div>
		</div>
	</fmt:bundle>


	<script src="plugins/d3/d3.min.js"></script>
	<script src="plugins/vega/vega.js"></script>
	<script src="plugins/igviz/igviz.js"></script>
	<script src="plugins/jquery/jquery-2.1.3.min.js"></script>
	<script src="js/metrics.ui.js"></script>
</body>
</html>

