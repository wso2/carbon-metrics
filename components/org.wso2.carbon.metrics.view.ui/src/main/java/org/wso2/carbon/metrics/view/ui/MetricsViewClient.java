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
package org.wso2.carbon.metrics.view.ui;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.data.service.stub.Metric;
import org.wso2.carbon.metrics.data.service.stub.MetricList;
import org.wso2.carbon.metrics.data.service.stub.MetricMeta;
import org.wso2.carbon.metrics.data.service.stub.MetricsDataServiceStub;

public class MetricsViewClient {

    private static final Logger logger = LoggerFactory.getLogger(MetricsViewClient.class);

    public MetricsDataServiceStub stub;

    public MetricsViewClient(String cookie, String backendServerURL, ConfigurationContext configCtx) throws AxisFault {
        if (cookie == null) {
            throw new IllegalStateException("Admin Service Cookie is not available");
        }
        String serviceURL = backendServerURL + "MetricsDataService";
        stub = new MetricsDataServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String[] getAllSources() throws RemoteException {
        try {
            return stub.getAllSources();
        } catch (RemoteException e) {
            String msg = "Error occurred while accessing Metrics Data Service. Backend service may be unavailable";
            logger.error(msg, e);
            throw e;
        }
    }

    public MetricHierarchyDataWrapper getHierarchy(String source, String path) throws RemoteException {
        try {
            return new MetricHierarchyDataWrapper(stub.getHierarchy(source, path));
        } catch (RemoteException e) {
            String msg = "Error occurred while accessing Metrics Data Service. Backend service may be unavailable";
            logger.error(msg, e);
            throw e;
        }
    }

    public MetricDataWrapper findLastMetrics(org.wso2.carbon.metrics.data.common.MetricList metrics, String source, String from) throws RemoteException {
        try {
            return new MetricDataWrapper(stub.findLastMetrics(convert(metrics), source, from));
        } catch (RemoteException e) {
            String msg = "Error occurred while accessing Metrics Data Service. Backend service may be unavailable";
            logger.error(msg, e);
            throw e;
        }
    }

    public MetricDataWrapper findMetricsByTimePeriod(org.wso2.carbon.metrics.data.common.MetricList metrics, String source, long from, long to)
            throws RemoteException {
        try {
            return new MetricDataWrapper(stub.findMetricsByTimePeriod(convert(metrics), source, from, to));
        } catch (RemoteException e) {
            String msg = "Error occurred while accessing Metrics Data Service. Backend service may be unavailable";
            logger.error(msg, e);
            throw e;
        }
    }

    private MetricList convert(org.wso2.carbon.metrics.data.common.MetricList list) {
        MetricList xsdMetricList = new MetricList();
        org.wso2.carbon.metrics.data.common.Metric[] metrics = list.getMetric();
        Metric[] xsdMetrics = new Metric[metrics.length];
        xsdMetricList.setMetric(xsdMetrics);
        for (int i = 0; i < metrics.length; i++) {
            org.wso2.carbon.metrics.data.common.Metric metric = metrics[i];
            Metric xsdMetric = new Metric();
            xsdMetrics[i] = xsdMetric;
            xsdMetric.setAttr(metric.getAttr());
            xsdMetric.setDisplayName(metric.getDisplayName());
            xsdMetric.setFormat(metric.getFormat());
            xsdMetric.setName(metric.getName());
            xsdMetric.setType(metric.getType());
        }
        return xsdMetricList;
    }

}
