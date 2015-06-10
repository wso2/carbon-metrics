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

var charts = [];
var titles = [];

metricsJQuery(function($) {
    createViewSelection();
	plotCharts();
	metricsJQuery(window).on('resize', resizeCharts);
	metricsJQuery("#source").change(plotCharts);
	metricsJQuery("#from").change(plotCharts);
});

function createViewSelection() {

    var container = metricsJQuery("#viewsSelection");
    
    metricsJQuery.each(views, function( key, view ) {
        var checkboxId = "cb".concat(key);
        // Check whether the value is stored as a cookie
        var checkedCookieValue = metricsJQuery.cookie(checkboxId);
        var checked = view.visible;
        if (checkedCookieValue != null) {
            checked = (checkedCookieValue === "true");
        }
        
        metricsJQuery('<input />', { type: 'checkbox', id: checkboxId, value: key, checked: checked }).appendTo(container);
        metricsJQuery('<label />', { 'for': checkboxId, text: view.name, class: 'toggleLabel' }).appendTo(container);

        if (checked) {
            metricsJQuery.each(view.charts, function(i, value) {
                charts.push(value);
            });
            metricsJQuery.each(view.titles, function(i, value) {
                titles.push(value);
            });
        }

    });
    
    container.on("click", "input:checkbox",  refreshViews);
}

function createChartHolders() {
    metricsJQuery("#chartHolder").empty();
    // Create Chart Holders from the template
    metricsJQuery.map(charts, createChartHolder);
}

function createChartHolder(chart, i) {
    // Get the template, compile and append to main chart holder
    var source   = $("#chartTemplate").html();
    var template = Handlebars.compile(source);
    var context = {type: chart, title: titles[i]};
    var html    = template(context);

    metricsJQuery("#chartHolder").append(html);
};

function plotCharts() {
    // Create holders first. Chart Views might have been changed
    createChartHolders();
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
	})
    .fail(function() {
        displayNoData(chart);
    });
}

function displayNoData(chart) {
    var noDataMsgId = "#msgNoData".concat(chart);
    metricsJQuery(noDataMsgId).show();
}

function igvizPlot(chart, data) {

    if (!data.data || data.data.length == 0) {
        displayNoData(chart);
        return;
    }

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

                // Check whether the value is stored as a cookie
                var checkedCookieValue = metricsJQuery.cookie(checkboxId);
                var checked = true;
                if (checkedCookieValue != null) {
                    checked = (checkedCookieValue === "true");
                }

				metricsJQuery('<input />', {
					type : 'checkbox',
					id : checkboxId,
					value : index,
					checked : checked
				}).appendTo(container);
				metricsJQuery('<label />', {
					'for' : checkboxId,
					text : value,
					class : 'toggleLabel'
				}).appendTo(container);

				if (checked) {
				    indices.push(index);
				}
			}
		});

		container.on("click", "input:checkbox", {
			chart : chart
		}, redrawChart);
	} else {
		indices = [ 1 ];
	}

	var width = metricsJQuery("#chartHolder").outerWidth() - 150; // canvas width
	var height = 300; // canvas height

	var chartConfig = {
		"xAxis" : 0,
		"yAxis" : indices,
		"padding" : 520,
		"width" : width,
		"height" : height,
		"chartType" : "line",
		"pointVisible": true,
		"markerSize": 2,
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

    // Save checked value in a cookie
    var inputCheckbox = metricsJQuery(this);
    var inputId = inputCheckbox.attr("id");
    var checked = inputCheckbox.prop("checked");
    metricsJQuery.cookie(inputId, checked);

	var chartConfig = metricsJQuery(toggleId).data("chartConfig");
	chartConfig.yAxis = indices;

	var data = metricsJQuery(toggleId).data("data");

	chart = igviz.setUp(igvizId, chartConfig, data);
	chart.plot(data.data);
}


function refreshViews(event) {
    var viewsSelectionId = "#viewsSelection";
    var container = metricsJQuery();

    var viewKeys = metricsJQuery.map(metricsJQuery(viewsSelectionId.concat(' input:checkbox:checked')), function(e, i) {
        return e.value;
    });

    if (viewKeys.length == 0) {
        event.preventDefault();
        return;
    }

    // Reinitialize charts array & titles array
    charts = [];
    titles = [];

    metricsJQuery.each(viewKeys, function(index, value) {
        console.log(value);
        var view = views[value];
        metricsJQuery.each(view.charts, function(index, value) {
            charts.push(value);
        });
        metricsJQuery.each(view.titles, function(index, value) {
            titles.push(value);
        });
    });

    // Save checked value in a cookie
    var inputCheckbox = metricsJQuery(this);
    var inputId = inputCheckbox.attr("id");
    var checked = inputCheckbox.prop("checked");
    metricsJQuery.cookie(inputId, checked);
}
