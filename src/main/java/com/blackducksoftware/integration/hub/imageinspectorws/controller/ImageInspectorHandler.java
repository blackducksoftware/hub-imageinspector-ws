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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.model.SimpleBdioDocument;
import com.google.gson.Gson;

@Component
public class ImageInspectorHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ImageInspectorAction imageInspectorAction;

    @Autowired
    private ResponseFactory responseFactory;

    @Autowired
    private Gson gson;

    public ResponseEntity<String> getImagePackages(final String tarFilePath, final String hubProjectName, final String hubProjectVersion, final String codeLocationPrefix) {

        try {
            final SimpleBdioDocument bdio = imageInspectorAction.getImagePackages(tarFilePath, hubProjectName, hubProjectVersion, codeLocationPrefix);
            final String usersJson = gson.toJson(bdio);
            return responseFactory.createResponse(HttpStatus.OK, usersJson);
            // } catch (final IntegrationRestException e) {
            // logger.error(e.getMessage(), e);
            // return responseFactory.createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), e.getHttpStatusMessage() + " : " + e.getMessage());
            // } catch (final IntegrationException e) {
            // logger.error(e.getMessage(), e);
            // return responseFactory.createResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (final Exception e) {
            logger.error(String.format("Exception thrown while getting image packages: %s", e.getMessage()), e);
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
