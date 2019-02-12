/**
 * hub-imageinspector-ws
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectorOsEnum;
import com.synopsys.integration.blackduck.imageinspector.api.WrongInspectorOsException;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class ImageInspectorHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ImageInspectorAction imageInspectorAction;

    @Autowired
    private ResponseFactory responseFactory;

    @Autowired
    private ProgramVersion programVersion;

    public ResponseEntity<String> getBdio(final String scheme, final String host, final int port, final String requestUri, final String dockerTarfilePath, final String blackDuckProjectName, final String blackDuckProjectVersion,
            final String codeLocationPrefix, final String givenImageRepo, final String givenImageTag, final boolean organizeComponentsByLayer, final boolean includeRemovedComponents, final boolean cleanupWorkingDir, final String containerFileSystemPath, final String loggingLevel) {
        try {
            final String msg = String.format("Black Duck Image Inspector v%s: dockerTarfilePath: %s, blackDuckProjectName: %s, blackDuckProjectVersion: %s, codeLocationPrefix: %s, organizeComponentsByLayer: %b, includeRemovedComponents: %b, cleanupWorkingDir: %b",
                programVersion.getProgramVersion(),
                dockerTarfilePath,
                blackDuckProjectName, blackDuckProjectVersion, codeLocationPrefix, organizeComponentsByLayer, includeRemovedComponents, cleanupWorkingDir);
            logger.info(msg);
            final String bdio = imageInspectorAction.getBdio(dockerTarfilePath, blackDuckProjectName, blackDuckProjectVersion, codeLocationPrefix, givenImageRepo, givenImageTag, organizeComponentsByLayer, includeRemovedComponents, cleanupWorkingDir,
                    containerFileSystemPath);
            logger.info("Succeeded: Returning BDIO response");
            return responseFactory.createResponse(bdio);
        } catch (final WrongInspectorOsException wrongOsException) {
            logger.error(String.format("WrongInspectorOsException thrown while getting image packages: %s", wrongOsException.getMessage()));
            final ImageInspectorOsEnum correctInspectorPlatform = wrongOsException.getcorrectInspectorOs();
            URI correctInspectorUri;
            try {
                correctInspectorUri = adjustUrl(scheme, host, requestUri, dockerTarfilePath, blackDuckProjectName, blackDuckProjectVersion, codeLocationPrefix, cleanupWorkingDir, containerFileSystemPath,
                        correctInspectorPlatform, loggingLevel, givenImageRepo, givenImageTag);
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

    public ResponseEntity<String> getServiceVersion() {
        try {
            return responseFactory.createResponse(programVersion.getProgramVersion());
        } catch (final Exception e) {
            final String msg = String.format("Exception thrown while getting service version: %s", e.getMessage());
            logger.error(msg, e);
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), msg);
        }
    }

    private URI adjustUrl(final String scheme, final String host, final String requestUriString, final String dockerTarfilePath, final String blackDuckProjectName, final String blackDuckProjectVersion,
            final String codeLocationPrefix, final boolean cleanupWorkingDir, final String containerFileSystemPath, final ImageInspectorOsEnum correctInspectorPlatform, final String loggingLevel,
        final String givenImageRepo, final String givenImageTag)
            throws IntegrationException {
        final StringBuilder querySb = new StringBuilder();
        querySb.append(String.format("%s=%s", ImageInspectorController.TARFILE_PATH_QUERY_PARAM, dockerTarfilePath));
        querySb.append(String.format("&%s=%s", ImageInspectorController.BLACKDUCK_PROJECT_NAME_QUERY_PARAM, blackDuckProjectName));
        querySb.append(String.format("&%s=%s", ImageInspectorController.BLACKDUCK_PROJECT_VERSION_QUERY_PARAM, blackDuckProjectVersion));
        querySb.append(String.format("&%s=%s", ImageInspectorController.CODELOCATION_PREFIX_QUERY_PARAM, codeLocationPrefix));
        querySb.append(String.format("&%s=%b", ImageInspectorController.CLEANUP_WORKING_DIR_QUERY_PARAM, cleanupWorkingDir));
        querySb.append(String.format("&%s=%s", ImageInspectorController.CONTAINER_FILESYSTEM_PATH_PARAM, containerFileSystemPath));
        querySb.append(String.format("&%s=%s", ImageInspectorController.LOGGING_LEVEL_PARAM, loggingLevel));
        querySb.append(String.format("&%s=%s", ImageInspectorController.IMAGE_REPO_PARAM, givenImageRepo));
        querySb.append(String.format("&%s=%s", ImageInspectorController.IMAGE_TAG_PARAM, givenImageTag));
        final String query = querySb.toString();
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
