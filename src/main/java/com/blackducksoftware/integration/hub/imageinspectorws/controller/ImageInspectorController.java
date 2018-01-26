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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageInspectorController {

    @Autowired
    private ImageInspectorHandler imageInspectorHandler;

    @RequestMapping(path = "/getimagepackages", method = RequestMethod.GET)
    public ResponseEntity<String> getImagePackages(@RequestParam(value = "tarfile") final String tarFilePath, @RequestParam(value = "hubprojectname", defaultValue = "") final String hubProjectName,
            @RequestParam(value = "hubprojectversion", defaultValue = "") final String hubProjectVersion, @RequestParam(value = "codelocationprefix", defaultValue = "") final String codeLocationPrefix) {
        return imageInspectorHandler.getImagePackages(tarFilePath, hubProjectName, hubProjectVersion, codeLocationPrefix);
    }

}
