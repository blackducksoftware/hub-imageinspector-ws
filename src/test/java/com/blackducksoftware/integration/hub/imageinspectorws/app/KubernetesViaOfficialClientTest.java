package com.blackducksoftware.integration.hub.imageinspectorws.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.imageinspector.linux.executor.Executor;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.AppsV1beta1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.auth.ApiKeyAuth;
import io.kubernetes.client.models.AppsV1beta1Deployment;
import io.kubernetes.client.models.AppsV1beta1DeploymentSpec;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;

public class KubernetesViaOfficialClientTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Ignore
    @Test
    public void test() throws IntegrationException, ApiException, IOException {
        // final Executor exec = new Executor();
        new Executor().executeCommand("ls", 5000L);
        new Executor().executeCommand("docker pull alpine:latest", 120000L);
        new Executor().executeCommand("docker save -o test/alpine.tar alpine:latest", 120000L);
        new Executor().executeCommand("chmod a+r test/alpine.tar", 5000L);

        final ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        final CoreV1Api api = new CoreV1Api();
        final V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
        int i = 0;
        for (final V1Pod pod : list.getItems()) {
            i++;
            System.out.printf("Pod %d: %s\n", i, pod.getMetadata().getName());
        }

        // kubectl create -f src/main/resources/kube-deployment.yml
        // waitForPodToStart ${deploymentName}
        // Configure API key authorization: BearerToken
        final ApiKeyAuth BearerToken = (ApiKeyAuth) client.getAuthentication("BearerToken");
        BearerToken.setApiKey(
                "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImRlZmF1bHQtdG9rZW4tZ3g3Y2siLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZGVmYXVsdCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImYxYjhlY2VmLWVjYzEtMTFlNy04ZTI3LTA4MDAyNzg3ZThhYyIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmRlZmF1bHQifQ.g2X3Wg4XeEelnTfakqy23_kQ4mJJ0wwnIEd23pWzkvugLICknNGmu3nREguaJUkkX9FO9pgY9KWBkg_u0PVYPu-uuWxJOWkWzrPwV9fjrGE8B3DHo1gy8scMpjvjF6LBJVmUn0WgEZcTW23reuULVE-LB47pO57pv2wZ3_npJ9tXKmt4qyqGJPYyrVz2_z_OK5si4xCJGEBriXT4zTHbQw3jQ-6Xiu4uf-q1yLi7k2txv10x9wsPPRoGRLNAzRBWj1djpmnHXUsitMEJ85XPrqkBfjC8azavB9uuvW4aRxT-WmxciNN5sCDzjQ_icSvUiUY6im0_HqAPBwS0rVfG1w");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        // BearerToken.setApiKeyPrefix("Token");

        final AppsV1beta1Api apiInstance = new AppsV1beta1Api();
        final String namespace = "default"; // String | object name and auth scope, such as for teams and projects
        final AppsV1beta1Deployment body = new AppsV1beta1Deployment(); // AppsV1beta1Deployment |
        body.setApiVersion("apps/v1");
        body.setKind("Deployment");

        final V1ObjectMeta deploymentMetadata = new V1ObjectMeta();
        deploymentMetadata.setName("hub-imageinspector-ws");
        final Map<String, String> deploymentMetadataLabels = new HashMap<>();
        deploymentMetadataLabels.put("app", "hub-imageinspector-ws");
        deploymentMetadata.setLabels(deploymentMetadataLabels);
        body.setMetadata(deploymentMetadata);
        final AppsV1beta1DeploymentSpec spec = new AppsV1beta1DeploymentSpec();
        // spec.setTemplate(template);
        body.setSpec(spec);
        final String pretty = "true"; // String | If 'true', then the output is pretty printed.
        try {
            final AppsV1beta1Deployment result = apiInstance.createNamespacedDeployment(namespace, body, pretty);
            System.out.println(result);
        } catch (final ApiException e) {
            System.err.println("Exception when calling AppsV1beta1Api#createNamespacedDeployment");
            e.printStackTrace();
        }
    }

}
