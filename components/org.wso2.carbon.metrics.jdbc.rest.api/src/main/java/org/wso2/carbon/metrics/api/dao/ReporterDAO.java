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
package org.wso2.carbon.metrics.api.dao;

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

import javax.sql.DataSource;

import org.wso2.carbon.metrics.api.MetricAttribute;
import org.wso2.carbon.metrics.api.MetricType;

/**
 * Description about ReporterDAO
 */
public class ReporterDAO {

    // TODO Optimize code

    private final DataSource dataSource;

    private final Map<MetricType, Set<MetricAttribute>> validMetricAttributeMap;

    public ReporterDAO(final DataSource dataSource) {
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

    // private String replace(String name) {
    // return name.replaceAll("\\*", "%");
    // }
    //
    // private <T> Set<T> addAllSet(List<T>... lists) {
    // Set<T> combinedSet = new HashSet<T>();
    // for (List<T> list : lists) {
    // if (list != null) {
    // combinedSet.addAll(list);
    // }
    // }
    // return combinedSet;
    // }
    //
    // private <T> List<T> addAll(List<T>... lists) {
    // List<T> combinedList = new ArrayList<T>();
    // for (List<T> list : lists) {
    // if (list != null) {
    // combinedList.addAll(list);
    // }
    // }
    // return combinedList;
    // }

    //
    // @SuppressWarnings("unchecked")
    // public List<String> queryMetricNames() {
    // return addAll(queryMetricNames(TABLE_METRIC_GAUGE), queryMetricNames(TABLE_METRIC_COUNTER),
    // queryMetricNames(TABLE_METRIC_METER), queryMetricNames(TABLE_METRIC_HISTOGRAM),
    // queryMetricNames(TABLE_METRIC_TIMER));
    // }

    // private List<String> querySources(String tableName) {
    // List<String> sources = new ArrayList<String>();
    // StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT SOURCE FROM ");
    // queryBuilder.append(tableName);
    //
    // Connection connection = null;
    // Statement statement = null;
    //
    // try {
    // connection = dataSource.getConnection();
    //
    // statement = connection.createStatement();
    //
    // // execute select SQL stetement
    // ResultSet rs = statement.executeQuery(queryBuilder.toString());
    //
    // while (rs.next()) {
    // String source = rs.getString("SOURCE");
    // sources.add(source);
    // }
    //
    // statement.close();
    // connection.close();
    // statement = null;
    // connection = null;
    // } catch (SQLException e) {
    // // LOGGER.error("Error when reporting timers", e);
    // } finally {
    // closeQuietly(connection, statement);
    // }
    //
    // return sources;
    // }

    public List<String> queryMetricNames(MetricType metricType) {
        List<String> names = new ArrayList<String>();
        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT NAME FROM ");
        queryBuilder.append(getTableName(metricType));

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
            // LOGGER.error("Error when reporting timers", e);
        } finally {
            closeQuietly(connection, statement);
        }

        return names;
    }

    // @SuppressWarnings("unchecked")
    // public Set<String> queryMatchingSources(String name) {
    // return addAll(queryMatchingSources(name, TABLE_METRIC_GAUGE), queryMatchingSources(name, TABLE_METRIC_COUNTER),
    // queryMatchingSources(name, TABLE_METRIC_METER), queryMatchingSources(name, TABLE_METRIC_HISTOGRAM),
    // queryMatchingSources(name, TABLE_METRIC_TIMER));
    // }

//    private List<String> queryMatchingSources(String name, String tableName) {
//        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT SOURCE FROM ");
//        queryBuilder.append(tableName);
//        queryBuilder.append(" WHERE SOURCE LIKE ?");
//        // return template.queryForList(queryBuilder.toString(), String.class, replace(name));
//        return null;
//    }

    // public void queryMetrics(String name) {
    // queryMetrics(name, TABLE_METRIC_GAUGE);
    // // assertEquals(1, result.size());
    // // assertEquals("gauge", result.get(0).get("NAME"));
    // // assertEquals("1", result.get(0).get("VALUE"));
    // // assertEquals(SOURCE, result.get(0).get("SOURCE"));
    // // assertEquals(timestamp, result.get(0).get("TIMESTAMP"));
    // }

//    private void queryMetrics(String name, String tableName) {
//        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT NAME FROM ");
//        queryBuilder.append(tableName);
//        queryBuilder.append(" WHERE NAME LIKE ?");
//
//        System.out.println(queryBuilder.toString());
//
//        // List<Map<String, Object>> result = template.queryForList(queryBuilder.toString(), String.class, name);
//        // System.out.println(result.get(0).get("NAME"));
//
//        // List<String> result = template.queryForList(queryBuilder.toString(), String.class, replace(name));
//        // System.out.println(result.get(0));
//    }

    private void validateMetricAttribute(MetricType metricType, MetricAttribute metricAttribute) {
        Set<MetricAttribute> attributes = validMetricAttributeMap.get(metricType);
        if (attributes == null || !attributes.contains(metricAttribute)) {
            throw new IllegalArgumentException("Invalid Metric Attribute \'" + metricAttribute.name()
                    + "\' for the Metric Type \'" + metricType.name() + "\'");
        }
    }

    public <T> void queryMetricDataPoints(MetricType metricType, String name, MetricAttribute metricAttribute,
            long startTime, long endTime, int maxDataPoints, MetricDataProcessor<T> processor) {
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
            ps.setMaxRows(maxDataPoints);

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
            e.printStackTrace();
            // LOGGER.error("Error when reporting timers", e);
        } finally {
            closeQuietly(connection, ps);
        }
    }

    private String getTableName(MetricType metricType) {
        // switch (metricType) {
        // case COUNTER:
        // return "METRIC_COUNTER";
        // case GAUGE:
        // return "METRIC_GAUGE";
        // case HISTOGRAM:
        // return "METRIC_HISTOGRAM";
        // case METER:
        // return "METRIC_METER";
        // case TIMER:
        // return "METRIC_TIMER";
        // }
        // throw new IllegalStateException("Invalid Metric Type");
        return "METRIC_" + metricType.name();
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
