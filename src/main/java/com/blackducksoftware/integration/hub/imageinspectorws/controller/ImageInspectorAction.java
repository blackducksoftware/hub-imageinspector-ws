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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.model.SimpleBdioDocument;
import com.blackducksoftware.integration.hub.docker.imageinspector.api.ImageInspectorApi;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

@Component
public class ImageInspectorAction {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    ImageInspectorApi api = new ImageInspectorApi();

    public SimpleBdioDocument getImagePackages(final String dockerTarfilePath, final String hubProjectName, final String hubProjectVersion, final String codeLocationPrefix) throws HubIntegrationException, IOException, InterruptedException {
        final String msg = String.format("dockerTarfilePath: %s, hubProjectName: %s, hubProjectVersion: %s, codeLocationPrefix: %s", dockerTarfilePath, hubProjectName, hubProjectVersion, codeLocationPrefix);
        logger.info(msg);
        final SimpleBdioDocument bdio = api.getBdio(dockerTarfilePath, hubProjectName, hubProjectVersion, codeLocationPrefix);
        return bdio;
    }
}
