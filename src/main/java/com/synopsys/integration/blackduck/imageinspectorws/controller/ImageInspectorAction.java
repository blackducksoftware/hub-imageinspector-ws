/**
 * hub-imageinspector-ws
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.imageinspectorws.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.compressors.CompressorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectorApi;
import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectorOsEnum;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.hub.bdio.BdioWriter;
import com.synopsys.integration.hub.bdio.model.SimpleBdioDocument;

@Component
public class ImageInspectorAction {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ImageInspectorApi api;

    @Autowired
    private ProgramVersion programVersion;

    @Autowired
    private Gson gson;

    @Value("${current.linux.distro:}")
    private String currentLinuxDistro;

    // In environments like OpenShift, each URL (route) may be different
    @Value("${inspector.url.alpine:}")
    private String inspectorUrlAlpine;

    @Value("${inspector.url.centos:}")
    private String inspectorUrlCentos;

    @Value("${inspector.url.ubuntu:}")
    private String inspectorUrlUbuntu;

    // If the inspectorUrls are not specified, just the port is adjusted
    @Value("${inspector.port.alpine:8080}")
    private String inspectorPortAlpine;

    @Value("${inspector.port.centos:8081}")
    private String inspectorPortCentos;

    @Value("${inspector.port.ubuntu:8082}")
    private String inspectorPortUbuntu;

    public String getBdio(final String dockerTarfilePath, final String blackDuckProjectName, final String blackDuckProjectVersion, final String codeLocationPrefix, final String givenImageRepo, final String givenImageTag,
            final boolean cleanupWorkingDir, final boolean usePreferredAliasNamespaceForge, final String containerFileSystemPath)
            throws IntegrationException, IOException, InterruptedException, CompressorException {
        final String msg = String.format("Black Duck Image Inspector v%s: dockerTarfilePath: %s, blackDuckProjectName: %s, blackDuckProjectVersion: %s, codeLocationPrefix: %s, cleanupWorkingDir: %b, usePreferredAliasNamespaceForge: %b",
                programVersion.getProgramVersion(),
                dockerTarfilePath,
                blackDuckProjectName, blackDuckProjectVersion, codeLocationPrefix, cleanupWorkingDir, usePreferredAliasNamespaceForge);
        logger.info(msg);
        logger.info(String.format("Provided value of current.linux.distro: %s", currentLinuxDistro));
        final SimpleBdioDocument bdio = api.getBdio(dockerTarfilePath, blackDuckProjectName, blackDuckProjectVersion, codeLocationPrefix, givenImageRepo, givenImageTag, cleanupWorkingDir, usePreferredAliasNamespaceForge,
                containerFileSystemPath, currentLinuxDistro);
        final ByteArrayOutputStream bdioBytes = new ByteArrayOutputStream();
        try (BdioWriter writer = new BdioWriter(gson, bdioBytes)) {
            writer.writeSimpleBdioDocument(bdio);
        }
        return bdioBytes.toString(StandardCharsets.UTF_8.name());
    }

    public String getConfiguredUrlForInspector(final ImageInspectorOsEnum inspectorPlatform) throws IntegrationException {
        logger.debug(String.format("Getting configured URL for inspector platform %s", inspectorPlatform.name()));
        switch (inspectorPlatform) {
        case ALPINE:
            return inspectorUrlAlpine;
        case CENTOS:
            return inspectorUrlCentos;
        case UBUNTU:
            return inspectorUrlUbuntu;
        default:
            throw new IntegrationException(String.format("Unexpected inspector platform: %s", inspectorPlatform.name()));
        }
    }

    public int derivePort(final ImageInspectorOsEnum inspectorPlatform) throws IntegrationException {
        logger.debug(String.format("Deriving port for inspector platform %s", inspectorPlatform.name()));
        switch (inspectorPlatform) {
        case ALPINE:
            return Integer.parseInt(inspectorPortAlpine);
        case CENTOS:
            return Integer.parseInt(inspectorPortCentos);
        case UBUNTU:
            return Integer.parseInt(inspectorPortUbuntu);
        default:
            throw new IntegrationException(String.format("Unexpected inspector platform: %s", inspectorPlatform.name()));
        }
    }

}