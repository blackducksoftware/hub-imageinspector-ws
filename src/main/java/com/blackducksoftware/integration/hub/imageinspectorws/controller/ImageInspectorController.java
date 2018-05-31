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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.qos.logback.classic.Level;

@RestController
public class ImageInspectorController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String BASE_LOGGER_NAME = "com.blackducksoftware";

    // Endpoint
    private static final String GET_BDIO_PATH = "/getbdio";
    // Mandatory query param
    static final String TARFILE_PATH_QUERY_PARAM = "tarfile";
    // Optional query params
    static final String HUB_PROJECT_NAME_QUERY_PARAM = "hubprojectname";
    static final String HUB_PROJECT_VERSION_QUERY_PARAM = "hubprojectversion";
    static final String CODELOCATION_PREFIX_QUERY_PARAM = "codelocationprefix";
    static final String CLEANUP_WORKING_DIR_QUERY_PARAM = "cleanup";
    static final String CONTAINER_FILESYSTEM_PATH_PARAM = "resultingcontainerfspath";
    static final String LOGGING_LEVEL_PARAM = "logginglevel";

    @Autowired
    private ImageInspectorHandler imageInspectorHandler;

    @RequestMapping(path = GET_BDIO_PATH, method = RequestMethod.GET)
    public ResponseEntity<String> getBdio(final HttpServletRequest request, @RequestParam(value = TARFILE_PATH_QUERY_PARAM) final String tarFilePath,
            @RequestParam(value = HUB_PROJECT_NAME_QUERY_PARAM, defaultValue = "") final String hubProjectName, @RequestParam(value = HUB_PROJECT_VERSION_QUERY_PARAM, defaultValue = "") final String hubProjectVersion,
            @RequestParam(value = CODELOCATION_PREFIX_QUERY_PARAM, defaultValue = "") final String codeLocationPrefix,
            @RequestParam(value = CLEANUP_WORKING_DIR_QUERY_PARAM, required = false, defaultValue = "true") final boolean cleanupWorkingDir,
            @RequestParam(value = CONTAINER_FILESYSTEM_PATH_PARAM, required = false, defaultValue = "") final String containerFileSystemPath,
            @RequestParam(value = LOGGING_LEVEL_PARAM, required = false, defaultValue = "INFO") final String loggingLevel) {
        logger.info(String.format("Endpoint %s called; tarFilePath: %s; containerFileSystemPath=%s, loggingLevel=%s", GET_BDIO_PATH, tarFilePath, containerFileSystemPath, loggingLevel));
        setLoggingLevel(loggingLevel);
        return imageInspectorHandler.getBdio(request.getScheme(), request.getServerName(), request.getServerPort(), request.getRequestURI(), tarFilePath, hubProjectName, hubProjectVersion, codeLocationPrefix, cleanupWorkingDir,
                containerFileSystemPath);
    }

    private void setLoggingLevel(final String newLoggingLevel) {
        logger.info(String.format("Setting logging level to %s", newLoggingLevel));
        try {
            final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(BASE_LOGGER_NAME);
            root.setLevel(Level.toLevel(newLoggingLevel));
            if (logger.isDebugEnabled()) {
                logger.info("DEBUG logging is enabled");
            } else {
                logger.info("DEBUG logging is not enabled");
            }
        } catch (final Exception e) {
            logger.error(String.format("Error setting logging level to %s: %s", newLoggingLevel, e.getMessage()));
        }
    }
}
