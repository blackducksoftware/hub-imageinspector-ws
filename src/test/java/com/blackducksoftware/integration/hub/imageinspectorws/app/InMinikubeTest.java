package com.blackducksoftware.integration.hub.imageinspectorws.app;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.imageinspector.linux.executor.Executor;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class InMinikubeTest {
    private static final String POD_NAME = "hub-imageinspector-ws";
    static KubernetesClient client;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final String[] ipOutput = new Executor().executeCommand("minikube ip", 5000L);
        for (final String line : ipOutput) {
            System.out.printf("getBdio output: %s\n", line);
        }

        client = new DefaultKubernetesClient();
        try {
            System.out.printf("API version: %s\n", client.getApiVersion());
        } catch (final Exception e) {
            e.printStackTrace();
        }

        client.load(InMinikubeTest.class.getResourceAsStream("/kube-pod.yml")).inNamespace("default").createOrReplace();
        client.load(InMinikubeTest.class.getResourceAsStream("/kube-service.yml")).inNamespace("default").createOrReplace();

        final PodList podList = client.pods().inNamespace("default").list();
        for (final Pod pod : podList.getItems()) {
            System.out.printf("Pod: %s; app: %s\n", pod.getMetadata().getName(), pod.getMetadata().getLabels().get("app"));
        }

        final ServiceList serviceList = client.services().inNamespace("default").list();
        for (final Service service : serviceList.getItems()) {
            System.out.printf("Service: %s; app: %s\n", service.getMetadata().getName(), service.getMetadata().getLabels().get("app"));
        }
        Thread.sleep(20000L);
        final int healthCheckLimit = 20;
        boolean serviceIsHealthy = false;
        for (int i = 0; i < healthCheckLimit; i++) {
            Thread.sleep(10000L);
            String[] healthCheckOutput;
            try {
                System.out.printf("Health check attempt %d of %d:\n", i, healthCheckLimit);
                healthCheckOutput = new Executor().executeCommand("curl -i http://192.168.99.100:8080/health", 5000L);
                for (final String line : healthCheckOutput) {
                    System.out.printf("Health check output: %s\n", line);
                    if ((line.startsWith("HTTP")) && (line.contains(" 200"))) {
                        System.out.println("Health check passed");
                        serviceIsHealthy = true;
                        break;
                    }
                }
                if (serviceIsHealthy) {
                    break;
                }
                final String[] getPodsOutput = new Executor().executeCommand("kubectl get pods", 5000L);
                final String getPodsOutputJoined = Arrays.asList(getPodsOutput).stream().collect(Collectors.joining("\n"));
                System.out.printf("get pods output:\n%s\n", getPodsOutputJoined);
            } catch (final IntegrationException e) {
                System.out.printf("Health check failed: %s\n", e.getMessage());
            }
        }
        assertTrue(serviceIsHealthy);
        System.out.println("The service is ready");
    }

    @AfterClass
    public static void tearDownAfterClass() throws UnsupportedEncodingException, IntegrationException, InterruptedException {
        client.close();

        new Executor().executeCommand("kubectl delete service hub-imageinspector-ws", 5000L);
        new Executor().executeCommand("kubectl delete pod hub-imageinspector-ws", 5000L);
        Thread.sleep(20000L);
        boolean podExited = false;
        for (int i = 0; i < 10; i++) {
            try {
                new Executor().executeCommand("kubectl get pod hub-imageinspector-ws", 5000L);
            } catch (final IntegrationException e) {
                if (e.getMessage().contains("NotFound")) {
                    podExited = true;
                    break;
                } else {
                    throw e;
                }
            }
            Thread.sleep(10000L);
        }
        if (!podExited) {
            System.out.println(String.format("Warning: Pod %s has not exited", POD_NAME));
        }
        System.out.println("Test has completed");
    }

    @Test
    public void test() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException, IntegrationException {
        final String[] getBdioOutput = new Executor().executeCommand("curl -i http://192.168.99.100:8080/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/alpine.tar", 5000L);
        for (final String line : getBdioOutput) {
            System.out.printf("getBdio output: %s\n", line);
        }
        final String getBdioOutputJoined = Arrays.asList(getBdioOutput).stream().collect(Collectors.joining(";"));
        assertTrue(getBdioOutputJoined.contains("alpine_latest_lib_apk_APK"));
        assertTrue(getBdioOutputJoined.contains("BillOfMaterials"));
        assertTrue(getBdioOutputJoined.contains("http:alpine/libc_utils"));
        assertTrue(getBdioOutputJoined.contains("musl/"));
        assertTrue(getBdioOutputJoined.contains("musl_utils/"));
        assertTrue(getBdioOutputJoined.contains("libressl2.6-libssl/"));
        assertTrue(getBdioOutputJoined.contains("x86_64"));
        assertTrue(getBdioOutputJoined.endsWith("]"));
    }
}
