/*
 * Copyright 2016 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.das.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestEventServer {

    private static Logger logger = LoggerFactory.getLogger(TestEventServer.class);
    private static final String RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";
    private ThriftDataReceiver thriftDataReceiver;
    private List<Event> events = Collections.synchronizedList(new ArrayList<>());

    public static void setTrustStore() {
        // Required for data agent
        String trustStorePath = RESOURCES_DIR + File.separator + "client-truststore.jks";
        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }

    public void start(String host, int receiverPort) {
        setKeyStore();
        setTrustStore();
        String dataBridgePath = RESOURCES_DIR + File.separator + "data-bridge-config.xml";
        AbstractStreamDefinitionStore streamDefinitionStore = new InMemoryStreamDefinitionStore();
        DataBridge databridge = new DataBridge(new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName, String password) {
                return true;
            }

            @Override
            public String getTenantDomain(String userName) {
                return "admin";
            }

            @Override
            public int getTenantId(String s) throws UserStoreException {
                return -1234;
            }

            @Override
            public void initContext(AgentSession agentSession) {
            }

            @Override
            public void destroyContext(AgentSession agentSession) {
            }

        }, streamDefinitionStore, dataBridgePath);


        Finder finder = new Finder();
        try {
            Files.walkFileTree(Paths.get("metrics_capp"), finder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finder.matchedFiles.stream().map(file -> {
            try {
                return EventDefinitionConverterUtils.convertFromJson(new String(Files.readAllBytes(file)));
            } catch (MalformedStreamDefinitionException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).forEach(stream -> {
            try {
                streamDefinitionStore.saveStreamDefinitionToStore(stream, -1234);
                logger.info("Stream Definition: {} is added to store", stream.getStreamId());
            } catch (StreamDefinitionStoreException e) {
                throw new RuntimeException(e);
            }
        });

        databridge.subscribe(new AgentCallback() {

            public void definedStream(StreamDefinition streamDefinition, int tenantID) {
                logger.info("Stream Definition: {}", streamDefinition);
            }

            @Override
            public void removeStream(StreamDefinition streamDefinition, int tenantID) {
            }

            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                logger.info("Received event count: {}, Username: {}, Events: {}", eventList.size(),
                        credentials.getUsername(), eventList);
                events.addAll(eventList);
            }


        });

        thriftDataReceiver = new ThriftDataReceiver(receiverPort, databridge);

        try {
            thriftDataReceiver.start(host);
            logger.info("Test Server Started");
        } catch (DataBridgeException e) {
            throw new RuntimeException(e);
        }
    }

    private void setKeyStore() {
        // Required for data bridge
        String keyStorePath = RESOURCES_DIR + File.separator + "wso2carbon.jks";
        System.setProperty("Security.KeyStore.Location", keyStorePath);
        System.setProperty("Security.KeyStore.Password", "wso2carbon");
    }

    public List<Event> getEvents() {
        return events;
    }

    public void stop() {
        if (thriftDataReceiver != null) {
            thriftDataReceiver.stop();
            logger.info("Test Server Stopped");
        }
    }

    // Find stream definition files
    private static class Finder extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private List<Path> matchedFiles = new ArrayList<>();

        private Finder() {
            matcher = FileSystems.getDefault().getPathMatcher("glob:org.wso2.carbon.metrics.*_1.0.0.json");
        }

        private void find(Path file) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                matchedFiles.add(file);
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            find(file);
            return FileVisitResult.CONTINUE;
        }
    }
}
