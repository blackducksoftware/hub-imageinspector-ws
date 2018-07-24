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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
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
            final String codeLocationPrefix, final String givenImageRepo, final String givenImageTag, final boolean cleanupWorkingDir, final String containerFileSystemPath) {
        try {
            final String bdio = imageInspectorAction.getBdio(tarFilePath, hubProjectName, hubProjectVersion, codeLocationPrefix, givenImageRepo, givenImageTag, cleanupWorkingDir, containerFileSystemPath);
            logger.info("Succeeded: Returning BDIO response");
            return responseFactory.createResponse(bdio);
        } catch (final WrongInspectorOsException wrongOsException) {
            logger.error(String.format("WrongInspectorOsException thrown while getting image packages: %s", wrongOsException.getMessage()));
            final ImageInspectorOsEnum correctInspectorPlatform = wrongOsException.getcorrectInspectorOs();
            final String dockerTarfilePath = wrongOsException.getDockerTarfilePath();
            URI correctInspectorUri;
            try {
                correctInspectorUri = adjustUrl(scheme, host, requestUri, dockerTarfilePath, hubProjectName, hubProjectVersion, codeLocationPrefix, cleanupWorkingDir, containerFileSystemPath,
                        correctInspectorPlatform);
            } catch (final IntegrationException deriveUrlException) {
                final String msg = String.format("Exception thrown while deriving redirect URL: %s", deriveUrlException.getMessage());
                logger.error(msg, deriveUrlException);
                return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, deriveUrlException.getMessage(), msg);
            }

            final ResponseEntity<String> redirectResponse = responseFactory.createRedirect(wrongOsException.getcorrectInspectorOs(),
                    correctInspectorUri.toString(), wrongOsException.getMessage());
            return redirectResponse;
        } catch (final Exception e) {
            final String msg = String.format("Exception thrown while getting image packages: %s", e.getMessage());
            logger.error(msg, e);
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), msg);
        }
    }

    private URI adjustUrl(final String scheme, final String host, final String requestUriString, final String dockerTarfilePath, final String hubProjectName, final String hubProjectVersion,
            final String codeLocationPrefix, final boolean cleanupWorkingDir, final String containerFileSystemPath, final ImageInspectorOsEnum correctInspectorPlatform) throws IntegrationException {
        final String query = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%b&%s=%s", ImageInspectorController.TARFILE_PATH_QUERY_PARAM, dockerTarfilePath, ImageInspectorController.HUB_PROJECT_NAME_QUERY_PARAM,
                hubProjectName, ImageInspectorController.HUB_PROJECT_VERSION_QUERY_PARAM, hubProjectVersion, ImageInspectorController.CODELOCATION_PREFIX_QUERY_PARAM, codeLocationPrefix,
                ImageInspectorController.CLEANUP_WORKING_DIR_QUERY_PARAM, cleanupWorkingDir, ImageInspectorController.CONTAINER_FILESYSTEM_PATH_PARAM, containerFileSystemPath);
        URI adjustedUri;
        URI requestUri;
        try {
            requestUri = new URI(requestUriString);
            final String inspectorBaseUrl = imageInspectorAction.getConfiguredUrlForInspector(correctInspectorPlatform);
            if (StringUtils.isBlank(inspectorBaseUrl)) {
                logger.debug(String.format("Deriving redirect URL from request scheme (%s), host (%s), inspector platform (%s) plus request path (%s) and query (%s)", scheme, host, correctInspectorPlatform.toString(), requestUri.getPath(),
                        query));
                final int port = imageInspectorAction.derivePort(correctInspectorPlatform);
                adjustedUri = new URI(scheme, null, host, port, requestUri.getPath(), query, null);
                logger.debug(String.format("adjusted URL: %s", adjustedUri.toString()));
            } else {
                logger.debug(String.format("Deriving redirect URL from configured platform-specific (%s) inspector base URL (%s) plus request path (%s) and query (%s)", correctInspectorPlatform.toString(), inspectorBaseUrl,
                        requestUri.getPath(), query));
                final URI inspectorBaseUri = new URI(inspectorBaseUrl);
                adjustedUri = new URI(inspectorBaseUri.getScheme(), null, inspectorBaseUri.getHost(), inspectorBaseUri.getPort(), requestUri.getPath(), query, null);
                logger.debug(String.format("adjusted URL: %s", adjustedUri.toString()));
            }
        } catch (final URISyntaxException e) {
            throw new IntegrationException(String.format("Error adjusting url %s for redirect", requestUriString), e);
        }
        return adjustedUri;
    }
}
