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
package org.wso2.carbon.metrics.impl.internal;

import java.io.File;
import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

/**
 * Creating relevant database tables required for Metrics
 */
public class LocalDatabaseCreator extends DatabaseCreator {

    private DataSource dataSource;

    private static final Logger logger = LoggerFactory.getLogger(LocalDatabaseCreator.class);

    public LocalDatabaseCreator(DataSource dataSource) {
        super(dataSource);
        this.dataSource = dataSource;
    }

    /**
     * Creates database tables if the relevant SQL script exist; returns otherwise.
     *
     * @throws Exception
     */
    @Override
    public void createRegistryDatabase() throws Exception {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            String databaseType = getDatabaseType(connection);
            String sqlScriptPath = getDbScriptLocation(databaseType);
            File scriptFile = new File(sqlScriptPath);
            if (scriptFile.exists()) {
                if (logger.isInfoEnabled()) {
                    logger.info(String.format("Using the SQL script file '%s' to create Metrics Database tables",
                            sqlScriptPath));
                }
                super.createRegistryDatabase();
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format(
                            "The expected SQL script file '%s' is not available to create Metrics Database tables",
                            sqlScriptPath));
                }
                return;
            }
            connection.close();
            connection = null;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

    }

    @Override
    protected String getDbScriptLocation(String databaseType) {
        String scriptName = databaseType + ".sql";
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("SQL Script File Name: %s", scriptName));
        }
        String carbonHome = CarbonUtils.getCarbonHome();
        return carbonHome + "/dbscripts/metrics/" + scriptName;

    }
}
