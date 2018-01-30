package com.blackducksoftware.integration.hub.imageinspectorws.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = { com.blackducksoftware.integration.hub.imageinspector.api.ImageInspectorApi.class, com.blackducksoftware.integration.hub.imageinspector.lib.ImageInspector.class,
        com.blackducksoftware.integration.hub.imageinspector.linux.extractor.ExtractorManager.class, com.blackducksoftware.integration.hub.imageinspector.linux.extractor.ApkExtractor.class,
        com.blackducksoftware.integration.hub.imageinspector.linux.extractor.Extractor.class, com.blackducksoftware.integration.hub.imageinspector.linux.executor.ApkExecutor.class,
        com.blackducksoftware.integration.hub.imageinspector.linux.executor.Executor.class, com.blackducksoftware.integration.hub.imageinspector.imageformat.docker.DockerTarParser.class,
        com.blackducksoftware.integration.hub.imageinspector.linux.Os.class, com.blackducksoftware.integration.hub.imageinspectorws.controller.ImageInspectorAction.class })

// @ComponentScan(basePackages = { "com.blackducksoftware.integration.hub.imageinspectorws", "com.blackducksoftware.integration.hub.imageinspector" })
public class AppConfig {

}
