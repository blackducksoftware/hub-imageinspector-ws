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
package com.blackducksoftware.integration.hub.imageinspectorws.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(scanBasePackages = { "com.blackducksoftware.integration.hub.imageinspector", "com.blackducksoftware.integration.hub.imageinspectorws" })
// @SpringBootApplication(scanBasePackages = "com.blackducksoftware.integration.hub")
@SpringBootApplication(scanBasePackages = { "com.blackducksoftware.integration.hub.imageinspector*", "com.blackducksoftware.integration.hub.imageinspectorws" }, scanBasePackageClasses = {
        com.blackducksoftware.integration.hub.imageinspector.api.ImageInspectorApi.class, com.blackducksoftware.integration.hub.imageinspector.lib.ImageInspector.class,
        com.blackducksoftware.integration.hub.imageinspector.linux.extractor.ExtractorManager.class, com.blackducksoftware.integration.hub.imageinspector.linux.extractor.ApkExtractor.class,
        com.blackducksoftware.integration.hub.imageinspector.linux.extractor.Extractor.class, com.blackducksoftware.integration.hub.imageinspector.linux.executor.ApkExecutor.class,
        com.blackducksoftware.integration.hub.imageinspector.linux.executor.Executor.class, com.blackducksoftware.integration.hub.imageinspector.imageformat.docker.DockerTarParser.class,
        com.blackducksoftware.integration.hub.imageinspector.linux.Os.class, com.blackducksoftware.integration.hub.imageinspectorws.controller.ImageInspectorAction.class })

public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
