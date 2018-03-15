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
package com.blackducksoftware.integration.hub.imageinspectorws.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.compressors.CompressorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.bdio.BdioWriter;
import com.blackducksoftware.integration.hub.bdio.model.SimpleBdioDocument;
import com.blackducksoftware.integration.hub.imageinspector.api.ImageInspectorApi;
import com.google.gson.Gson;

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

    public String getBdio(final String dockerTarfilePath, final String hubProjectName, final String hubProjectVersion, final String codeLocationPrefix, final boolean cleanupWorkingDir, final String containerFileSystemPath)
            throws IntegrationException, IOException, InterruptedException, CompressorException {
        final String msg = String.format("hub-imageinspector-ws v%s: dockerTarfilePath: %s, hubProjectName: %s, hubProjectVersion: %s, codeLocationPrefix: %s, cleanupWorkingDir: %b", programVersion.getProgramVersion(), dockerTarfilePath,
                hubProjectName, hubProjectVersion, codeLocationPrefix, cleanupWorkingDir);
        logger.info(msg);
        logger.info(String.format("Provided value of current.linux.distro: %s", currentLinuxDistro));
        final SimpleBdioDocument bdio = api.getBdio(dockerTarfilePath, hubProjectName, hubProjectVersion, codeLocationPrefix, cleanupWorkingDir, containerFileSystemPath, currentLinuxDistro);
        final ByteArrayOutputStream bdioBytes = new ByteArrayOutputStream();
        try (BdioWriter writer = new BdioWriter(gson, bdioBytes)) {
            writer.writeSimpleBdioDocument(bdio);
        }
        return bdioBytes.toString(StandardCharsets.UTF_8.name());
    }
}
