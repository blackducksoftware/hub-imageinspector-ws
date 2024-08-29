/*
 * blackduck-imageinspector
 *
 * Copyright (c) 2024 Blackduck, Inc.
 *
 * Use subject to the terms and conditions of the Blackduck End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.sca.integration.blackduck.imageinspectorws.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectionRequest;
import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectorApi;
import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectorOsEnum;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.bdio.BdioWriter;
import com.synopsys.integration.bdio.model.SimpleBdioDocument;

@Component
public class ImageInspectorAction {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ImageInspectorApi api;

    @Autowired
    private Gson gson;

    // In environments like OpenShift, each URL (route) may be different
    @Value("${inspector.url.alpine:}")
    private String inspectorUrlAlpine;

    @Value("${inspector.url.centos:}")
    private String inspectorUrlCentos;

    @Value("${inspector.url.ubuntu:}")
    private String inspectorUrlUbuntu;

    // If the inspectorUrls are not specified, just the port is adjusted
    @Value("${inspector.port.alpine:8080}")
    private String inspectorPortAlpine;

    @Value("${inspector.port.centos:8081}")
    private String inspectorPortCentos;

    @Value("${inspector.port.ubuntu:8082}")
    private String inspectorPortUbuntu;

    public String getBdio(final ImageInspectionRequest imageInspectionRequest)
            throws IntegrationException, IOException, InterruptedException {
        final SimpleBdioDocument bdio = api.getBdio(imageInspectionRequest);
        final ByteArrayOutputStream bdioBytes = new ByteArrayOutputStream();
        try (BdioWriter writer = new BdioWriter(gson, bdioBytes)) {
            writer.writeSimpleBdioDocument(bdio);
        }
        return bdioBytes.toString(StandardCharsets.UTF_8.name());
    }

    public String getConfiguredUrlForInspector(final ImageInspectorOsEnum inspectorPlatform) throws IntegrationException {
        logger.debug(String.format("Getting configured URL for inspector platform %s", inspectorPlatform.name()));
        switch (inspectorPlatform) {
        case ALPINE:
            return inspectorUrlAlpine;
        case CENTOS:
            return inspectorUrlCentos;
        case UBUNTU:
            return inspectorUrlUbuntu;
        default:
            throw new IntegrationException(String.format("Unexpected inspector platform: %s", inspectorPlatform.name()));
        }
    }

    public int derivePort(final ImageInspectorOsEnum inspectorPlatform) throws IntegrationException {
        logger.debug(String.format("Deriving port for inspector platform %s", inspectorPlatform.name()));
        switch (inspectorPlatform) {
        case ALPINE:
            return Integer.parseInt(inspectorPortAlpine);
        case CENTOS:
            return Integer.parseInt(inspectorPortCentos);
        case UBUNTU:
            return Integer.parseInt(inspectorPortUbuntu);
        default:
            throw new IntegrationException(String.format("Unexpected inspector platform: %s", inspectorPlatform.name()));
        }
    }

}
