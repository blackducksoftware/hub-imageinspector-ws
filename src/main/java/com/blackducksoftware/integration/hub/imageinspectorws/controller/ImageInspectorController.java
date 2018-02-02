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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageInspectorController {
    static final String GET_BDIO_PATH = "/getbdio";
    static final String TARFILE_PATH_QUERY_PARAM = "tarfile";
    // TODO there are more query params to extract to constants

    @Autowired
    private ImageInspectorHandler imageInspectorHandler;

    @RequestMapping(path = GET_BDIO_PATH, method = RequestMethod.GET)
    public ResponseEntity<String> getBdio(final HttpServletRequest request, @RequestParam(value = TARFILE_PATH_QUERY_PARAM) final String tarFilePath, @RequestParam(value = "hubprojectname", defaultValue = "") final String hubProjectName,
            @RequestParam(value = "hubprojectversion", defaultValue = "") final String hubProjectVersion, @RequestParam(value = "codelocationprefix", defaultValue = "") final String codeLocationPrefix) {
        return imageInspectorHandler.getBdio(request.getScheme(), request.getServerName(), request.getServerPort(), request.getRequestURI(), tarFilePath, hubProjectName, hubProjectVersion, codeLocationPrefix);
    }

    @RequestMapping(path = "/getbdiofile", method = RequestMethod.GET)
    public ResponseEntity<Resource> getBdioFile(final String param) throws IOException {

        // TODO this is all TEMP code
        // final File file = new File("/opt/blackduck/hub-imageinspector-ws/target/alpine.tar");
        final File file = new File("/tmp/alpine.tar");

        final Path path = Paths.get(file.getAbsolutePath());
        final ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        final HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.ok().headers(headers).contentLength(file.length()).contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
    }

}
