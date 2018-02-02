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

import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<String> getImagePackages(final HttpServletRequest request, @RequestParam(value = TARFILE_PATH_QUERY_PARAM) final String tarFilePath,
            @RequestParam(value = "hubprojectname", defaultValue = "") final String hubProjectName, @RequestParam(value = "hubprojectversion", defaultValue = "") final String hubProjectVersion,
            @RequestParam(value = "codelocationprefix", defaultValue = "") final String codeLocationPrefix) {
        System.out.println(String.format("*** Request: RequestURI: %s", request.getRequestURI()));
        System.out.println(String.format("*** Request: Scheme: %s", request.getScheme()));
        System.out.println(String.format("*** Request: Protocol: %s", request.getProtocol()));
        System.out.println(String.format("*** Request: Host: %s; Port: %d", request.getServerName(), request.getServerPort()));
        return imageInspectorHandler.getImagePackages(request.getScheme(), request.getServerName(), request.getServerPort(), request.getRequestURI(), tarFilePath, hubProjectName, hubProjectVersion, codeLocationPrefix);
    }

}
