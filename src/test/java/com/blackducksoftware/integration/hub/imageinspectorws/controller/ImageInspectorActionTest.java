package com.blackducksoftware.integration.hub.imageinspectorws.controller;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.imageinspector.api.WrongInspectorOsException;

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

    @Ignore // TODO TEMP
    @Test
    public void test() throws HubIntegrationException, IOException, InterruptedException {
        try {
            imageInspectorAction.getBdio("/tmp/alpine.tar", "SB001", "testVersion", "testCodeLocationPrefix", true);
        } catch (final WrongInspectorOsException e) {
            System.out.println(String.format("Got expected WrongInspectorOsException: %s", e.getMessage()));
        }
    }

}
