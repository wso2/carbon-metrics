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
var metricsJQuery = jQuery.noConflict();

var charts = [ "Memory", "CPU", "LoadAverage", "PhysicalMemory", "FileDescriptor" ]

metricsJQuery(function($) {
	plotCharts();
	metricsJQuery(window).on('resize', resizeCharts);
	metricsJQuery("#source").change(plotCharts);
	metricsJQuery("#from").change(plotCharts);
});


function plotCharts() {
	metricsJQuery.map(charts, plotChart);
}

function plotChart(chart) {
	var url = "data-ajaxprocessor.jsp";

	var data = metricsJQuery("#formInput").serializeArray();
	data.push({
		name : "type",
		value : chart
	});

	metricsJQuery.getJSON(url, data).done(function(data) {
		igvizPlot(chart, data);
	});
}

function igvizPlot(chart, data) {

	var toggleId = "#toggle".concat(chart);
	var igvizId = "#igviz".concat(chart);

	var indices = [];

	var container = metricsJQuery(toggleId);
	// Remove all child nodes
	container.empty();

	if (data.metadata.names.length > 2) {
		metricsJQuery.each(data.metadata.names, function(index, value) {
			if (index > 0) {
				var checkboxId = "cb".concat(chart).concat(index);
				metricsJQuery('<input />', {
					type : 'checkbox',
					id : checkboxId,
					value : index,
					checked : true
				}).appendTo(container);
				metricsJQuery('<label />', {
					'for' : checkboxId,
					text : value,
					class : 'toggleLabel'
				}).appendTo(container);
				indices.push(index);
			}
		});

		metricsJQuery(toggleId).on("click", "input:checkbox", {
			chart : chart
		}, redrawChart);
	} else {
		indices = [ 1 ];
	}

	var width = metricsJQuery(igvizId).outerWidth() - 100; // canvas width
	var height = 300; // canvas height

	var chartConfig = {
		"xAxis" : 0,
		"yAxis" : indices,
		"padding" : 520,
		"width" : width,
		"height" : height,
		"chartType" : "line",
		"pointVisible" : false,
		"interpolationMode" : "linear"
	}

	var chart = igviz.setUp(igvizId, chartConfig, data);

	metricsJQuery(toggleId).data("chartConfig", chartConfig);
	metricsJQuery(toggleId).data("data", data);
	metricsJQuery(toggleId).data("chart", chart);

	chart.plot(data.data);

}

function resizeCharts() {
	metricsJQuery.map(charts, resizeChart);
}

function resizeChart(chart) {
	var toggleId = "#toggle".concat(chart);
	var igvizChart = metricsJQuery(toggleId).data("chart");
	if (igvizChart) {
		igvizChart.resize();
	}
}

function redrawChart(event) {

	var chart = event.data.chart;

	var toggleId = "#toggle".concat(chart);
	var igvizId = "#igviz".concat(chart);

	var indices = metricsJQuery.map(metricsJQuery(toggleId
			.concat(' input:checkbox:checked')), function(e, i) {
		return +e.value;
	});

	if (indices.length == 0) {
		event.preventDefault();
		return;
	}

	var chartConfig = metricsJQuery(toggleId).data("chartConfig");
	chartConfig.yAxis = indices;

	var data = metricsJQuery(toggleId).data("data");

	chart = igviz.setUp(igvizId, chartConfig, data);
	chart.plot(data.data);
}
