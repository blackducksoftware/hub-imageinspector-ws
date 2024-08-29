/*
 * blackduck-imageinspector
 *
 * Copyright (c) 2024 Blackduck, Inc.
 *
 * Use subject to the terms and conditions of the Blackduck End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.sca.integration.blackduck.imageinspectorws.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceDetails {
    private final Logger logger = LoggerFactory.getLogger(ServiceDetails.class);
    private String version;

    public String getVersion() throws IOException {
        if (version == null) {
            final Properties props = new Properties();
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("version.properties")) {
                props.load(stream);
            }
            version = props.getProperty("program.version");
            logger.debug(String.format("programVersion: %s", version));
        }
        return version;
    }
}
