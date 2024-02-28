/*
 * blackduck-imageinspector
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.imageinspectorws.controller;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectionRequest;
import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectionRequestBuilder;
import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectorOsEnum;
import com.synopsys.integration.blackduck.imageinspector.api.WrongInspectorOsException;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class ImageInspectorHandler {
    private static final String INITIAL_URL_PARAMETER_FORMAT_STRING = "%s=%s";
    private static final String SUBSEQUENT_URL_PARAMETER_FORMAT_STRING = "&%s=%s";
    private static final String SUBSEQUENT_URL_PARAMETER_FORMAT_BOOLEAN = "&%s=%b";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ImageInspectorAction imageInspectorAction;

    @Autowired
    private ResponseFactory responseFactory;

    @Autowired
    private ServiceDetails serviceDetails;

    public ResponseEntity<String> getBdio(final String scheme, final String host, final String requestUri,
        final ImageInspectionRequest imageInspectionRequest) {
        try {
            logger.info(String.format("Black Duck Image Inspector v%s request: %s",
                serviceDetails.getVersion(), imageInspectionRequest));
            final String bdio = imageInspectorAction.getBdio(imageInspectionRequest);
            logger.info("Succeeded: Returning BDIO response");
            return responseFactory.createResponse(bdio);
        } catch (final WrongInspectorOsException wrongOsException) {
            logger.error(String.format("WrongInspectorOsException thrown while getting image packages: %s", wrongOsException.getMessage()));
            final ImageInspectorOsEnum correctInspectorPlatform = wrongOsException.getcorrectInspectorOs();
            URI correctInspectorUri;
            try {
                correctInspectorUri = adjustUrl(scheme, host, requestUri,
                    correctInspectorPlatform,
                    imageInspectionRequest);
            } catch (final IntegrationException deriveUrlException) {
                final String msg = String.format("Exception thrown while deriving redirect URL: %s", deriveUrlException.getMessage());
                logger.error(msg, deriveUrlException);
                return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, msg);
            }

            return responseFactory.createRedirect(wrongOsException.getcorrectInspectorOs(),
                    correctInspectorUri.toString(), wrongOsException.getMessage());
        } catch (final Exception e) {
            final String msg = String.format("Exception thrown while getting image packages: %s", e.getMessage());
            logger.error(msg, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, msg);
        }
    }

    public ResponseEntity<String> getServiceVersion() {
        try {
            return responseFactory.createResponse(serviceDetails.getVersion());
        } catch (final Exception e) {
            final String msg = String.format("Exception thrown while getting service version: %s", e.getMessage());
            logger.error(msg, e);
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, msg);
        }
    }

    private URI adjustUrl(final String scheme, final String host, final String requestUriString,
        final ImageInspectorOsEnum correctInspectorPlatform,
        final ImageInspectionRequest imageInspectionRequest)
            throws IntegrationException {
        final StringBuilder querySb = new StringBuilder();
        querySb.append(String.format(INITIAL_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.TARFILE_PATH_QUERY_PARAM, imageInspectionRequest.getImageTarfilePath()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.BLACKDUCK_PROJECT_NAME_QUERY_PARAM, imageInspectionRequest.getBlackDuckProjectName()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.BLACKDUCK_PROJECT_VERSION_QUERY_PARAM, imageInspectionRequest.getBlackDuckProjectVersion()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.CODELOCATION_PREFIX_QUERY_PARAM, imageInspectionRequest.getCodeLocationPrefix()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_BOOLEAN, ImageInspectorController.CLEANUP_WORKING_DIR_QUERY_PARAM, imageInspectionRequest.isCleanupWorkingDir()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.CONTAINER_FILESYSTEM_PATH_PARAM, imageInspectionRequest.getContainerFileSystemOutputPath()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.CONTAINER_FILESYSTEM_EXCLUDED_PATHS_PARAM, imageInspectionRequest.getContainerFileSystemExcludedPathListString()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.LOGGING_LEVEL_PARAM, imageInspectionRequest.getLoggingLevel()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.IMAGE_REPO_PARAM, imageInspectionRequest.getGivenImageRepo()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.IMAGE_TAG_PARAM, imageInspectionRequest.getGivenImageTag()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.PLATFORM_TOP_LAYER_ID_PARAM, imageInspectionRequest.getPlatformTopLayerExternalId()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_STRING, ImageInspectorController.TARGET_LINUX_DISTRO_OVERRIDE_PARAM, imageInspectionRequest.getTargetLinuxDistroOverride()));
        querySb.append(String.format(SUBSEQUENT_URL_PARAMETER_FORMAT_BOOLEAN, ImageInspectorController.ORGANIZE_COMPONENTS_BY_LAYER_QUERY_PARAM, imageInspectionRequest.isOrganizeComponentsByLayer()));
        // TODO shouldn't have to add each new param here too, or: an omission should be caught
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
