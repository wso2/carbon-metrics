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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.data.common.MetricAttribute;
import org.wso2.carbon.metrics.data.common.MetricType;

/**
 * Querying Metric Data via JDBC
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

    public List<String> queryAllSources() {
        List<String> results = new ArrayList<String>();

        Connection connection = null;

        try {
            connection = dataSource.getConnection();

            for (MetricType metricType : MetricType.values()) {
                List<String> list = querySources(connection, metricType);
                if (!list.isEmpty()) {
                    results.addAll(list);
                }
            }

            // Sort source names
            Collections.sort(results);

            connection.close();
            connection = null;
        } catch (SQLException e) {
            logger.error("Error when querying sources.", e);
        } finally {
            closeQuietly(connection, null);
        }

        return results;
    }

    private List<String> querySources(Connection connection, MetricType metricType) {
        List<String> results = new ArrayList<String>();
        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT SOURCE FROM ");
        queryBuilder.append(getTableName(metricType));

        Statement statement = null;

        try {
            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(queryBuilder.toString());

            while (rs.next()) {
                String source = rs.getString("SOURCE");
                results.add(source);
            }

            statement.close();
            statement = null;
        } catch (SQLException e) {
            logger.error("Error when querying sources. Metric Type: " + metricType, e);
        } finally {
            closeQuietly(null, statement);
        }

        return results;
    }

    public <T> void queryMetrics(MetricType metricType, List<String> names, List<MetricAttribute> attributes,
            String source, long startTime, long endTime, MetricDataProcessor<T> processor) {
        if (logger.isDebugEnabled()) {
            logger.debug(String
                    .format("Metric Search Query Parameters. Metric Type: %s, Names: %s, Attributes: %s, Source: %s, Start Time: %d, End Time: %d",
                            metricType, names, attributes, source, startTime, endTime));
        }
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
            logger.debug(String.format("Metric Search Query: %s", query));
        }

        Connection connection = null;
        PreparedStatement ps = null;

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

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString(1);
                long timestamp = rs.getLong(2);
                for (int j = 0; j < attributes.size(); j++) {
                    BigDecimal value;
                    try {
                        value = rs.getBigDecimal(3 + j);
                    } catch (NumberFormatException e) {
                        value = BigDecimal.ZERO;
                        // throw?
                    }
                    processor.process(source, timestamp, metricType, name, attributes.get(j), value);
                }

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
