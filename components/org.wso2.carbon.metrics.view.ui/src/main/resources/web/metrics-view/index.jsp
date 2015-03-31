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
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder"%>

<%@ page import="java.util.regex.Matcher"%>
<%@ page import="java.util.regex.Pattern"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.TimeZone"%>

<!-- igviz styles -->
<link href="plugins/igviz/igviz.css" rel="stylesheet">

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>View Metrics</title>

</head>
<body>
	
	<fmt:bundle basename="org.wso2.carbon.metrics.view.ui.i18n.Resources">

		<carbon:breadcrumb label="metrics.view" resourceBundle="org.wso2.carbon.metrics.view.ui.i18n.Resources" topPage="true"
			request="<%=request%>" />
		<div id="middle">
			<h2>
				<fmt:message key="metrics.view" />
			</h2>
			<div id="workArea">
				<div id="igviz-metrics"></div>
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

