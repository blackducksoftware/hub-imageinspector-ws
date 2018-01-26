package com.blackducksoftware.integration.hub.imageinspectorws.controller;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.blackducksoftware.integration.hub.docker.imageinspector.api.WrongInspectorOsException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfig.class })
public class ImageInspectorActionTest {

    @Autowired
    private ImageInspectorAction imageInspectorAction;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws HubIntegrationException, IOException, InterruptedException {
        try {
            imageInspectorAction.getImagePackages("/tmp/alpine.tar", "SB001", "testVersion", "testCodeLocationPrefix");
        } catch (final WrongInspectorOsException e) {
            // expected
        }
    }

}
