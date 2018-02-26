package com.blackducksoftware.integration.hub.imageinspectorws.app;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.imageinspector.linux.executor.Executor;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class InMinikubeTest {
    private static final String POD_NAME = "hub-imageinspector-ws";
    private static final String PORT_ALPINE = "8080";
    private static final String PORT_CENTOS = "8081";
    private static final String PORT_UBUNTU = "8082";
    private static KubernetesClient client;
    private static String clusterIp;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new Executor().executeCommand("mkdir -p build/test/target", 5000L);
        new Executor().executeCommand("docker pull alpine:latest", 120000L);
        new Executor().executeCommand("docker save -o build/test/target/alpine.tar alpine:latest", 20000L);
        new Executor().executeCommand("chmod a+r build/test/target/alpine.tar", 5000L);

        new Executor().executeCommand("docker pull debian:latest", 120000L);
        new Executor().executeCommand("docker save -o build/test/target/debian.tar debian:latest", 20000L);
        new Executor().executeCommand("chmod a+r build/test/target/debian.tar", 5000L);

        new Executor().executeCommand("docker pull fedora:latest", 120000L);
        new Executor().executeCommand("docker save -o build/test/target/fedora.tar fedora:latest", 20000L);
        new Executor().executeCommand("chmod a+r build/test/target/fedora.tar", 5000L);

        final String[] kubeStatusOutput = new Executor().executeCommand("minikube status", 10000L);
        final String kubeStatusOutputJoined = Arrays.asList(kubeStatusOutput).stream().collect(Collectors.joining("\n"));
        System.out.println(String.format("kubeStatusOutputJoined: %s", kubeStatusOutputJoined));
        assertTrue("Minikube is not running", kubeStatusOutputJoined.contains("minikube: Running"));
        assertTrue("Minikube is not running", kubeStatusOutputJoined.contains("cluster: Running"));

        final String[] ipOutput = new Executor().executeCommand("minikube ip", 10000L);
        clusterIp = ipOutput[0];
        client = new DefaultKubernetesClient();
        try {
            System.out.printf("API version: %s\n", client.getApiVersion());
        } catch (final Exception e) {
            e.printStackTrace();
        }

        client.load(InMinikubeTest.class.getResourceAsStream("/kube-test-pod.yml")).inNamespace("default").createOrReplace();
        Thread.sleep(10000L);
        client.load(InMinikubeTest.class.getResourceAsStream("/kube-service.yml")).inNamespace("default").createOrReplace();
        Thread.sleep(5000L);

        final PodList podList = client.pods().inNamespace("default").list();
        final String podListString = podList.getItems().stream().map(pod -> pod.getMetadata().getName()).collect(Collectors.joining("\n"));
        System.out.printf("Pods: %s\n", podListString);
        // for (final Pod pod : podList.getItems()) {
        // System.out.printf("Pod: %s; app: %s\n", pod.getMetadata().getName(), pod.getMetadata().getLabels().get("app"));
        // }

        final ServiceList serviceList = client.services().inNamespace("default").list();
        for (final Service service : serviceList.getItems()) {
            System.out.printf("Service: %s; app: %s\n", service.getMetadata().getName(), service.getMetadata().getLabels().get("app"));
        }
        Thread.sleep(20000L);
        assertTrue(isServiceHealthy(PORT_ALPINE));
        assertTrue(isServiceHealthy(PORT_CENTOS));
        assertTrue(isServiceHealthy(PORT_UBUNTU));
        final String[] getPodsOutput = new Executor().executeCommand("kubectl get pods", 10000L);
        final String getPodsOutputJoined = Arrays.asList(getPodsOutput).stream().collect(Collectors.joining("\n"));
        System.out.printf("get pods output:\n%s\n", getPodsOutputJoined);
        System.out.println("The service is ready");
    }

    private static boolean isServiceHealthy(final String port) throws InterruptedException, UnsupportedEncodingException {
        boolean serviceIsHealthy = false;
        final int healthCheckLimit = 20;
        for (int i = 0; i < healthCheckLimit; i++) {
            String[] healthCheckOutput;
            try {
                System.out.printf("Port %s Health check attempt %d of %d:\n", port, i, healthCheckLimit);
                healthCheckOutput = new Executor().executeCommand(String.format("curl -i http://%s:%s/health", clusterIp, port), 10000L);
                for (final String line : healthCheckOutput) {
                    System.out.printf("Port %s Health check output: %s\n", port, line);
                    if ((line.startsWith("HTTP")) && (line.contains(" 200"))) {
                        System.out.printf("Port %s Health check passed\n", port);
                        serviceIsHealthy = true;
                        break;
                    }
                }
                if (serviceIsHealthy) {
                    break;
                }
            } catch (final IntegrationException e) {
                System.out.printf("Port %s Health check failed: %s\n", port, e.getMessage());
            }
            Thread.sleep(10000L);
        }
        return serviceIsHealthy;
    }

    @AfterClass
    public static void tearDownAfterClass() throws UnsupportedEncodingException, IntegrationException, InterruptedException {
        if (client != null) {
            client.close();
        }
        new Executor().executeCommand(String.format("kubectl delete service %s", POD_NAME), 10000L);
        new Executor().executeCommand(String.format("kubectl delete pod %s", POD_NAME), 10000L);
        Thread.sleep(20000L);
        boolean podExited = false;
        for (int i = 0; i < 10; i++) {
            try {
                new Executor().executeCommand(String.format("kubectl get pod %s", POD_NAME), 10000L);
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

    @Ignore // TODO TEMP
    @Test
    public void testAlpineOnAlpine() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException, IntegrationException {
        final String[] getBdioOutput = new Executor().executeCommand(String.format("curl -i http://%s:%s/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/alpine.tar", clusterIp, PORT_ALPINE), 10000L);
        final String getBdioOutputJoined = Arrays.asList(getBdioOutput).stream().collect(Collectors.joining(";"));
        System.out.printf("getBdioOutputJoined: %s", getBdioOutputJoined);
        assertTrue(getBdioOutputJoined.contains("alpine_latest_lib_apk_APK"));
        assertTrue(getBdioOutputJoined.contains("BillOfMaterials"));
        assertTrue(getBdioOutputJoined.contains("http:alpine/libc_utils"));
        assertTrue(getBdioOutputJoined.contains("musl/"));
        assertTrue(getBdioOutputJoined.contains("musl_utils/"));
        assertTrue(getBdioOutputJoined.contains("libressl2.6-libssl/"));
        assertTrue(getBdioOutputJoined.contains("x86_64"));
        assertTrue(getBdioOutputJoined.endsWith("]"));
    }

    @Ignore // TODO TEMP
    @Test
    public void testAlpineOnUbuntu() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException, IntegrationException {
        final String[] getBdioOutput = new Executor().executeCommand(String.format("curl -i http://%s:%s/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/alpine.tar", clusterIp, PORT_UBUNTU), 10000L);
        final String getBdioOutputJoined = Arrays.asList(getBdioOutput).stream().collect(Collectors.joining(";"));
        System.out.printf("getBdioOutputJoined: %s", getBdioOutputJoined);
        final String expectedRedirect = String.format("Location: http://%s:%s/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/alpine.tar&hubprojectname=&hubprojectversion=&codelocationprefix=&cleanup=true", clusterIp,
                PORT_ALPINE);
        assertTrue(getBdioOutputJoined.contains(String.format(";%s", expectedRedirect)));
    }

    @Ignore // TODO TEMP
    @Test
    public void testAlpineOnCentos() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException, IntegrationException {
        final String[] getBdioOutput = new Executor().executeCommand(String.format("curl -i http://%s:%s/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/alpine.tar", clusterIp, PORT_CENTOS), 10000L);
        final String getBdioOutputJoined = Arrays.asList(getBdioOutput).stream().collect(Collectors.joining("\n"));
        System.out.printf("getBdioOutputJoined: %s", getBdioOutputJoined);
        final String expectedRedirect = String.format("Location: http://%s:%s/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/alpine.tar&hubprojectname=&hubprojectversion=&codelocationprefix=&cleanup=true", clusterIp,
                PORT_ALPINE);
        assertTrue(getBdioOutputJoined.contains(String.format(";%s", expectedRedirect)));
    }

    @Ignore // TODO TEMP
    @Test
    public void testFedoraOnCentos() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException, IntegrationException {
        final String[] getBdioOutput = new Executor().executeCommand(String.format("curl -i http://%s:%s/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/fedora.tar", clusterIp, PORT_CENTOS), 10000L);
        final String getBdioOutputJoined = Arrays.asList(getBdioOutput).stream().collect(Collectors.joining("\n"));
        System.out.printf("getBdioOutputJoined: %s", getBdioOutputJoined);
        assertTrue(getBdioOutputJoined.contains("file-libs/"));
        assertTrue(getBdioOutputJoined.contains("x86_64"));
        assertTrue(getBdioOutputJoined.endsWith("]"));

    }

    @Test
    public void testDebianOnUbuntu() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException, IntegrationException {
        final String[] getBdioOutput = new Executor().executeCommand(String.format("curl -i http://%s:%s/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/debian.tar", clusterIp, PORT_UBUNTU), 10000L);
        final String getBdioOutputJoined = Arrays.asList(getBdioOutput).stream().collect(Collectors.joining("\n"));
        System.out.printf("getBdioOutputJoined: %s", getBdioOutputJoined);
        assertTrue(getBdioOutputJoined.contains("libsemanage-common/"));
        assertTrue(getBdioOutputJoined.contains("amd64"));
        assertTrue(getBdioOutputJoined.endsWith("]"));

    }
}
