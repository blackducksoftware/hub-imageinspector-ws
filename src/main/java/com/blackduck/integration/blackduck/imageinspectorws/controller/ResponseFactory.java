/*
 * blackduck-imageinspector
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.imageinspectorws.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectorOsEnum;

@Component
public class ResponseFactory {

    public ResponseEntity<String> createResponse(final String body) {
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    public ResponseEntity<String> createResponse(final HttpStatus status, final String body) {
        final HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(body, headers, status);
    }

    public ResponseEntity<String> createRedirect(final ImageInspectorOsEnum newInspectorOs, final String newUrl, final String warning) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", newUrl);
        headers.add("Warning", warning);
        return new ResponseEntity<>(newInspectorOs.name(), headers, HttpStatus.FOUND);
    }
}
