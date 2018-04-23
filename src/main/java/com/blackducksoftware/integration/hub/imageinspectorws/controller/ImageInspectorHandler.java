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

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.imageinspector.api.ImageInspectorOsEnum;
import com.blackducksoftware.integration.hub.imageinspector.api.WrongInspectorOsException;

@Component
public class ImageInspectorHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ImageInspectorAction imageInspectorAction;

    @Autowired
    private ResponseFactory responseFactory;

    public ResponseEntity<String> getBdio(final String scheme, final String host, final int port, final String requestUri, final String tarFilePath, final String hubProjectName, final String hubProjectVersion,
            final String codeLocationPrefix, final boolean cleanupWorkingDir, final String containerFileSystemPath) {
        try {
            final String bdio = imageInspectorAction.getBdio(tarFilePath, hubProjectName, hubProjectVersion, codeLocationPrefix, cleanupWorkingDir, containerFileSystemPath);
            logger.info("Succeeded: Returning BDIO response");
            return responseFactory.createResponse(bdio);
        } catch (final WrongInspectorOsException wrongOsException) {
            logger.error(String.format("WrongInspectorOsException thrown while getting image packages: %s", wrongOsException.getMessage()));
            final ImageInspectorOsEnum correctInspectorPlatform = wrongOsException.getcorrectInspectorOs();
            final String dockerTarfilePath = wrongOsException.getDockerTarfilePath();
            final String correctInspectorRelUrl = deriveRelativeUrl(requestUri, dockerTarfilePath, hubProjectName, hubProjectVersion, codeLocationPrefix, cleanupWorkingDir, containerFileSystemPath);
            String correctInspectorUrl;
            try {
                correctInspectorUrl = deriveUrl(scheme, host, derivePort(correctInspectorPlatform), correctInspectorRelUrl);
            } catch (final IntegrationException deriveUrlException) {
                final String msg = String.format("Exception thrown while deriving redirect URL: %s", deriveUrlException.getMessage());
                logger.error(msg, deriveUrlException);
                return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, deriveUrlException.getMessage(), msg);
            }
            final ResponseEntity<String> redirectResponse = responseFactory.createRedirect(correctInspectorUrl, wrongOsException.getMessage());
            return redirectResponse;
        } catch (final Exception e) {
            final String msg = String.format("Exception thrown while getting image packages: %s", e.getMessage());
            logger.error(msg, e);
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), msg);
        }
    }

    private String deriveRelativeUrl(final String requestUri, final String dockerTarfilePath, final String hubProjectName, final String hubProjectVersion, final String codeLocationPrefix, final boolean cleanupWorkingDir,
            final String containerFileSystemOutputPath) {
        final String relUrl = String.format("%s?%s=%s&%s=%s&%s=%s&%s=%s&%s=%b&%s=%s", deriveEndpoint(requestUri), ImageInspectorController.TARFILE_PATH_QUERY_PARAM, dockerTarfilePath, ImageInspectorController.HUB_PROJECT_NAME_QUERY_PARAM,
                hubProjectName, ImageInspectorController.HUB_PROJECT_VERSION_QUERY_PARAM, hubProjectVersion, ImageInspectorController.CODELOCATION_PREFIX_QUERY_PARAM, codeLocationPrefix,
                ImageInspectorController.CLEANUP_WORKING_DIR_QUERY_PARAM, cleanupWorkingDir, ImageInspectorController.CONTAINER_FILESYSTEM_PATH_PARAM, containerFileSystemOutputPath);
        logger.debug(String.format("relativeUrl for redirect: %s", relUrl));
        return relUrl;
    }

    private String deriveEndpoint(final String requestUri) {
        final int lastSlashIndex = requestUri.lastIndexOf('/');
        final String endpoint = lastSlashIndex < 0 ? requestUri : requestUri.substring(lastSlashIndex + 1);
        logger.debug(String.format("Converted requestUri %s to endpoint %s", requestUri, endpoint));
        return endpoint;
    }

    // TODO this should be configurable
    private int derivePort(final ImageInspectorOsEnum correctInspectorPlatform) throws IntegrationException {
        switch (correctInspectorPlatform) {
        case ALPINE:
            return 8080;
        case CENTOS:
            return 8081;
        case UBUNTU:
            return 8082;
        default:
            throw new IntegrationException(String.format("Unexpected inspector platform: %s", correctInspectorPlatform.name()));
        }
    }

    private String deriveUrl(final String scheme, final String host, final int port, final String relativeUrl) {
        final String slashLessRelativeUrl = relativeUrl.startsWith("/") ? relativeUrl.substring(1) : relativeUrl;
        final String url = String.format("%s://%s:%d/%s", scheme, host, port, slashLessRelativeUrl);
        logger.debug(String.format("deriveUrl() returning: %s", url));
        return url;
    }

}
