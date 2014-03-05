package com.tesco.services;

import com.couchbase.client.ClusterManager;
import com.couchbase.client.clustermanager.BucketType;
import com.tesco.services.resources.TestConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class IntegrationTest {

    private static ClusterManager clusterManager;
    final static TestConfiguration testConfiguration = new TestConfiguration();

    @BeforeClass
    public static void init() throws URISyntaxException, InterruptedException, IOException {
        List<URI> hosts = Arrays.asList(
                new URI(testConfiguration.getDBServerUrl())
        );

        clusterManager = new ClusterManager(hosts, testConfiguration.getDBAdminUsername(), testConfiguration.getDBAdminPassword());
        if (!clusterManager.listBuckets().contains(testConfiguration.getDBBucketName())) {
            clusterManager.createNamedBucket(BucketType.COUCHBASE, testConfiguration.getDBBucketName(), 100, 0, testConfiguration.getDBBucketPassword(), true);
            Thread.sleep(5000); // Given sometime for couchbase bucket to be created.
        }
    }

    @AfterClass
    public static void destroy() {
        clusterManager.flushBucket(testConfiguration.getDBBucketName());
    }
}
