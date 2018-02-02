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
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.imageinspector.api.ImageInspectorOsEnum;
import com.blackducksoftware.integration.hub.imageinspector.api.WrongInspectorOsException;

@Component
public class ImageInspectorHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ImageInspectorAction imageInspectorAction;

    @Autowired
    private ResponseFactory responseFactory;

    public ResponseEntity<String> getImagePackages(final String scheme, final String host, final int port, final String requestUri, final String tarFilePath, final String hubProjectName, final String hubProjectVersion,
            final String codeLocationPrefix) {
        try {
            final SimpleBdioDocument bdio = imageInspectorAction.getImagePackages(tarFilePath, hubProjectName, hubProjectVersion, codeLocationPrefix);

            return responseFactory.createResponse(bdio);
        } catch (final WrongInspectorOsException e) {
            logger.error(String.format("WrongInspectorOsException thrown while getting image packages: %s", e.getMessage()));
            final ImageInspectorOsEnum correctInspectorPlatform = e.getcorrectInspectorOs();
            final String dockerTarfilePath = e.getDockerTarfilePath();
            // TODO need to handle more query params: hub project/version
            final String correctInspectorRelUrl = String.format("%s?%s=%s", deriveEndpoint(requestUri), ImageInspectorController.TARFILE_PATH_QUERY_PARAM, dockerTarfilePath);
            String correctInspectorUrl;
            try {
                correctInspectorUrl = deriveUrl(scheme, host, derivePort(correctInspectorPlatform), correctInspectorRelUrl);
            } catch (final HubIntegrationException deriveUrlException) {
                logger.error(String.format("Exception thrown while deriving redirect URL: %s", deriveUrlException.getMessage()), deriveUrlException);
                return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, deriveUrlException.getMessage());
            }
            final ResponseEntity<String> redirectResponse = responseFactory.createRedirect(correctInspectorUrl, e.getMessage());
            return redirectResponse;
        } catch (final Exception e) {
            logger.error(String.format("Exception thrown while getting image packages: %s", e.getMessage()), e);
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // TODO unit test
    private String deriveEndpoint(final String requestUri) {
        final int lastSlashIndex = requestUri.lastIndexOf('/');
        final String endpoint = lastSlashIndex < 0 ? requestUri : requestUri.substring(lastSlashIndex + 1);
        logger.debug(String.format("Converted requestUri %s to endpoint %s", requestUri, endpoint));
        return endpoint;
    }

    // TODO unit test
    private int derivePort(final ImageInspectorOsEnum correctInspectorPlatform) throws HubIntegrationException {
        switch (correctInspectorPlatform) {
        case ALPINE:
            return 8080;
        case CENTOS:
            return 8081;
        case UBUNTU:
            return 8082;
        default:
            throw new HubIntegrationException(String.format("Unexpected inspector platform: %s", correctInspectorPlatform.name()));
        }
    }

    // TODO unit test
    private String deriveUrl(final String scheme, final String host, final int port, final String relativeUrl) {
        final String slashLessRelativeUrl = relativeUrl.startsWith("/") ? relativeUrl.substring(1) : relativeUrl;
        final String url = String.format("%s://%s:%d/%s", scheme, host, port, slashLessRelativeUrl);
        logger.debug(String.format("deriveUrl() returning: %s", url));
        return url;
    }

}
