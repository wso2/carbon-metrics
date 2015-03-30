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
package org.wso2.carbon.metrics.data.service.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.data.service.MetricAttribute;
import org.wso2.carbon.metrics.data.service.MetricNameSearchResult;
import org.wso2.carbon.metrics.data.service.MetricType;

/**
 * Description about ReporterDAO
 */
public class ReporterDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReporterDAO.class);

    // TODO Optimize code

    private final DataSource dataSource;

    private final Map<MetricType, Set<MetricAttribute>> validMetricAttributeMap;

    public ReporterDAO(final DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("Data source is null");
        }
        this.dataSource = dataSource;
        validMetricAttributeMap = new HashMap<MetricType, Set<MetricAttribute>>();
        populateValidMetricAttributeMap();
    }

    private void populateValidMetricAttributeMap() {
        Set<MetricAttribute> attributes = new HashSet<MetricAttribute>();
        attributes.add(MetricAttribute.VALUE);
        validMetricAttributeMap.put(MetricType.GAUGE, attributes);

        attributes = new HashSet<MetricAttribute>();
        attributes.add(MetricAttribute.COUNT);
        validMetricAttributeMap.put(MetricType.COUNTER, attributes);

        attributes = new HashSet<MetricAttribute>();
        attributes.add(MetricAttribute.COUNT);
        attributes.add(MetricAttribute.MEAN_RATE);
        attributes.add(MetricAttribute.M1_RATE);
        attributes.add(MetricAttribute.M5_RATE);
        attributes.add(MetricAttribute.M15_RATE);
        validMetricAttributeMap.put(MetricType.METER, attributes);

        attributes = new HashSet<MetricAttribute>();
        attributes.add(MetricAttribute.COUNT);
        attributes.add(MetricAttribute.MAX);
        attributes.add(MetricAttribute.MEAN);
        attributes.add(MetricAttribute.MIN);
        attributes.add(MetricAttribute.STDDEV);
        attributes.add(MetricAttribute.P50);
        attributes.add(MetricAttribute.P75);
        attributes.add(MetricAttribute.P95);
        attributes.add(MetricAttribute.P98);
        attributes.add(MetricAttribute.P99);
        attributes.add(MetricAttribute.P999);
        validMetricAttributeMap.put(MetricType.HISTOGRAM, attributes);

        attributes = new HashSet<MetricAttribute>();
        attributes.add(MetricAttribute.COUNT);
        attributes.add(MetricAttribute.MAX);
        attributes.add(MetricAttribute.MEAN);
        attributes.add(MetricAttribute.MIN);
        attributes.add(MetricAttribute.STDDEV);
        attributes.add(MetricAttribute.P50);
        attributes.add(MetricAttribute.P75);
        attributes.add(MetricAttribute.P95);
        attributes.add(MetricAttribute.P98);
        attributes.add(MetricAttribute.P99);
        attributes.add(MetricAttribute.P999);
        attributes.add(MetricAttribute.MEAN_RATE);
        attributes.add(MetricAttribute.M1_RATE);
        attributes.add(MetricAttribute.M5_RATE);
        attributes.add(MetricAttribute.M15_RATE);
        validMetricAttributeMap.put(MetricType.TIMER, attributes);
    }

    private void validateMetricAttribute(MetricType metricType, MetricAttribute metricAttribute) {
        Set<MetricAttribute> attributes = validMetricAttributeMap.get(metricType);
        if (attributes == null || !attributes.contains(metricAttribute)) {
            throw new IllegalArgumentException("Invalid Metric Attribute \"" + metricAttribute.name()
                    + "\" for the Metric Type \"" + metricType.name() + "\"");
        }
    }

    public List<String> queryMetricNames(MetricType metricType) {
        List<String> names = new ArrayList<String>();
        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT NAME FROM ");
        queryBuilder.append(getTableName(metricType));
        queryBuilder.append(" ORDER BY NAME");

        Connection connection = null;
        Statement statement = null;

        try {
            connection = dataSource.getConnection();

            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(queryBuilder.toString());

            while (rs.next()) {
                String name = rs.getString("NAME");
                names.add(name);
            }

            statement.close();
            connection.close();
            statement = null;
            connection = null;
        } catch (SQLException e) {
        } finally {
            closeQuietly(connection, statement);
        }

        return names;
    }

    public List<MetricNameSearchResult> searchMetricNames(MetricType metricType, String searchName) {
        List<MetricNameSearchResult> results = new ArrayList<MetricNameSearchResult>();
        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT NAME, SOURCE FROM ");
        queryBuilder.append(getTableName(metricType));
        if (searchName != null && searchName.trim().length() > 0) {
            queryBuilder.append(" WHERE NAME LIKE ?");
        }
        queryBuilder.append(" ORDER BY NAME");

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dataSource.getConnection();

            ps = connection.prepareStatement(queryBuilder.toString());

            if (searchName != null && searchName.trim().length() > 0) {
                ps.setString(1, searchName);
            }

            ResultSet rs = ps.executeQuery();

            Map<String, MetricNameSearchResult> map = new HashMap<String, MetricNameSearchResult>();

            while (rs.next()) {
                String name = rs.getString("NAME");
                String source = rs.getString("SOURCE");
                MetricNameSearchResult result = map.get(name);
                if (result != null) {
                    List<String> sources = new ArrayList<String>(Arrays.asList(result.getSource()));
                    sources.add(source);
                    result.setSource(sources.toArray(new String[sources.size()]));
                } else {
                    result = new MetricNameSearchResult();
                    result.setName(name);
                    result.setSource(new String[] { source });
                    map.put(name, result);
                    results.add(result);
                }
            }
            ps.close();
            connection.close();
            ps = null;
            connection = null;
        } catch (SQLException e) {
            logger.error("Error when searching metric names", e);
        } finally {
            closeQuietly(connection, ps);
        }

        return results;
    }

    public <T> void queryMetrics(MetricType metricType, String name, MetricAttribute metricAttribute, long startTime,
            long endTime, MetricDataProcessor<T> processor) {
        validateMetricAttribute(metricType, metricAttribute);
        StringBuilder queryBuilder = new StringBuilder("SELECT SOURCE, TIMESTAMP, ");
        queryBuilder.append(getColumnName(metricAttribute));
        queryBuilder.append(" FROM ");
        queryBuilder.append(getTableName(metricType));
        queryBuilder.append(" WHERE NAME = ? AND TIMESTAMP >= ? AND TIMESTAMP <= ? ORDER BY TIMESTAMP");

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dataSource.getConnection();

            ps = connection.prepareStatement(queryBuilder.toString());
            ps.setString(1, name);
            ps.setLong(2, startTime);
            ps.setLong(3, endTime);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String source = rs.getString(1);
                long timestamp = rs.getLong(2);
                BigDecimal value;
                try {
                    value = rs.getBigDecimal(3);
                } catch (NumberFormatException e) {
                    value = BigDecimal.ZERO;
                    // throw?
                }
                processor.process(source, name, timestamp, value);
            }

            ps.close();
            connection.close();
            ps = null;
            connection = null;
        } catch (SQLException e) {
            logger.error(String.format("Error when querying metrics. SQL %s", queryBuilder.toString()), e);
        } finally {
            closeQuietly(connection, ps);
        }
    }

    public <T> void queryMetrics(MetricType metricType, String[] names, MetricAttribute metricAttribute,
            long startTime, long endTime, MetricDataProcessor<T> processor) {
        validateMetricAttribute(metricType, metricAttribute);
        StringBuilder queryBuilder = new StringBuilder("SELECT NAME, SOURCE, TIMESTAMP, ");
        queryBuilder.append(getColumnName(metricAttribute));
        queryBuilder.append(" FROM ");
        queryBuilder.append(getTableName(metricType));
        queryBuilder.append(" WHERE NAME IN (");
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                queryBuilder.append(", ");
            }
            queryBuilder.append("?");
        }
        queryBuilder.append(") AND TIMESTAMP >= ? AND TIMESTAMP <= ? ORDER BY TIMESTAMP");

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dataSource.getConnection();

            ps = connection.prepareStatement(queryBuilder.toString());
            int i;
            for (i = 0; i < names.length; i++) {
                ps.setString(i + 1, names[i]);
            }
            ps.setLong(++i, startTime);
            ps.setLong(++i, endTime);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString(1);
                String source = rs.getString(2);
                long timestamp = rs.getLong(3);
                BigDecimal value;
                try {
                    value = rs.getBigDecimal(4);
                } catch (NumberFormatException e) {
                    value = BigDecimal.ZERO;
                    // throw?
                }
                processor.process(source, name, timestamp, value);
            }

            ps.close();
            connection.close();
            ps = null;
            connection = null;
        } catch (SQLException e) {
            logger.error(String.format("Error when querying metrics. SQL %s", queryBuilder.toString()), e);
        } finally {
            closeQuietly(connection, ps);
        }
    }

    private String getTableName(MetricType metricType) {
        switch (metricType) {
        case COUNTER:
            return "METRIC_COUNTER";
        case GAUGE:
            return "METRIC_GAUGE";
        case HISTOGRAM:
            return "METRIC_HISTOGRAM";
        case METER:
            return "METRIC_METER";
        case TIMER:
            return "METRIC_TIMER";
        }
        throw new IllegalStateException("Invalid Metric Type");
    }

    private String getColumnName(MetricAttribute metricAttribute) {
        return metricAttribute.name();
    }

    private void closeQuietly(Connection connection, Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    private void closeQuietly(Connection connection, PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }
}
