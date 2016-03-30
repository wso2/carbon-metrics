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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.data.common.MetricAttribute;
import org.wso2.carbon.metrics.data.common.MetricType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

/**
 * Querying Metric Data via JDBC.
 */
public class ReporterDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReporterDAO.class);
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

    private void validateMetricAttributes(MetricType metricType, List<MetricAttribute> metricAttributes) {
        Set<MetricAttribute> attributes = validMetricAttributeMap.get(metricType);
        if (attributes == null) {
            return;
        }
        for (MetricAttribute metricAttribute : metricAttributes) {
            if (!attributes.contains(metricAttribute)) {
                throw new IllegalArgumentException("Invalid Metric Attribute \"" + metricAttribute.name()
                        + "\" for the Metric Type \"" + metricType.name() + "\"");
            }
        }
    }

    public Set<String> queryAllSources() {
        Set<String> results = new TreeSet<String>();
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            for (MetricType metricType : MetricType.values()) {
                List<String> list = querySources(connection, metricType);
                if (!list.isEmpty()) {
                    results.addAll(list);
                }
            }
            connection.close();
            connection = null;
        } catch (SQLException e) {
            logger.error("Error when querying sources.", e);
        } finally {
            closeQuietly(connection);
        }
        return results;
    }

    private List<String> querySources(Connection connection, MetricType metricType) {
        List<String> results = new ArrayList<String>();
        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT SOURCE FROM ");
        queryBuilder.append(getTableName(metricType));
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(queryBuilder.toString());
            while (rs.next()) {
                String source = rs.getString("SOURCE");
                results.add(source);
            }
            rs.close();
            rs = null;
            statement.close();
            statement = null;
        } catch (SQLException e) {
            logger.error("Error when querying sources. Metric Type: " + metricType, e);
        } finally {
            closeQuietly(null, statement, rs);
        }
        return results;
    }

    public Map<String, MetricType> queryHierarchicalMetrics(String source, String path) {
        Map<String, MetricType> results = new HashMap<>();
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            for (MetricType metricType : MetricType.values()) {
                List<String> names = getHierarchicalMetricNames(connection, source, path, metricType);
                if (!names.isEmpty()) {
                    for (String name : names) {
                        results.put(name, metricType);
                    }
                }
            }
            connection.close();
            connection = null;
        } catch (SQLException e) {
            logger.error("Error when querying sources.", e);
        } finally {
            closeQuietly(connection);
        }
        return results;
    }

    private List<String> getHierarchicalMetricNames(Connection connection, String source, String path,
                                                    MetricType metricType) {
        List<String> results = new ArrayList<String>();
        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT NAME FROM ");
        queryBuilder.append(getTableName(metricType));
        queryBuilder.append(" WHERE SOURCE = ?");
        if (path != null && !path.isEmpty()) {
            queryBuilder.append(" AND NAME LIKE ?");
        }
        String query = queryBuilder.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, source);
            if (path != null && !path.isEmpty()) {
                ps.setString(2, path + "%");
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("NAME");
                results.add(name);
            }
            rs.close();
            rs = null;
            ps.close();
            ps = null;
        } catch (SQLException e) {
            logger.error(String.format("Error when querying metrics. SQL %s", query), e);
        } finally {
            closeQuietly(null, ps, rs);
        }
        return results;
    }

    public <T> void queryMetrics(MetricType metricType, List<String> names, List<MetricAttribute> attributes,
                                 String source, long startTime, long endTime, MetricDataProcessor<T> processor) {
        validateMetricAttributes(metricType, attributes);
        StringBuilder queryBuilder = new StringBuilder("SELECT NAME, TIMESTAMP, ");
        for (int i = 0; i < attributes.size(); i++) {
            if (i > 0) {
                queryBuilder.append(", ");
            }
            queryBuilder.append(getColumnName(attributes.get(i)));
        }
        queryBuilder.append(" FROM ");
        queryBuilder.append(getTableName(metricType));
        queryBuilder.append(" WHERE");
        if (names.size() == 1) {
            queryBuilder.append(" NAME = ?");
        } else {
            queryBuilder.append(" NAME IN (");
            for (int i = 0; i < names.size(); i++) {
                if (i > 0) {
                    queryBuilder.append(", ");
                }
                queryBuilder.append("?");
            }
            queryBuilder.append(")");
        }
        queryBuilder.append(" AND TIMESTAMP >= ? AND TIMESTAMP <= ?");
        queryBuilder.append(" AND SOURCE = ?");
        queryBuilder.append(" ORDER BY TIMESTAMP");
        String query = queryBuilder.toString();

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Metric Search Query: %s Parameters: Metric Type: %s, Names: %s," +
                            " Attributes: %s, Source: %s, Start Time: %d, End Time: %d",
                    query, metricType, names, attributes, source, startTime, endTime));
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);
            int i;
            for (i = 0; i < names.size(); i++) {
                ps.setString(i + 1, names.get(i));
            }
            ps.setLong(++i, startTime);
            ps.setLong(++i, endTime);
            ps.setString(++i, source);

            rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString(1);
                long timestamp = rs.getLong(2);
                for (int j = 0; j < attributes.size(); j++) {
                    BigDecimal value;
                    try {
                        value = rs.getBigDecimal(3 + j);
                    } catch (NumberFormatException e) {
                        value = BigDecimal.ZERO;
                    }
                    processor.process(source, timestamp, metricType, name, attributes.get(j), value);
                }
            }
            rs.close();
            rs = null;
            ps.close();
            ps = null;
            connection.close();
            connection = null;
        } catch (SQLException e) {
            logger.error(String.format("Error when querying metrics. SQL %s", queryBuilder.toString()), e);
        } finally {
            closeQuietly(connection, ps, rs);
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

    private void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    private void closeQuietly(Connection connection, Statement statement, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
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

    private void closeQuietly(Connection connection, PreparedStatement ps, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
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
