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
    final static TestConfiguration testConfiguration = TestConfiguration.load();

    @BeforeClass
    public static void init() throws URISyntaxException, InterruptedException, IOException {
        List<URI> hosts = Arrays.asList(
                new URI(testConfiguration.getDBServerUrl())
        );

        clusterManager = new ClusterManager(hosts, testConfiguration.getCouchbaseAdminUsername(), testConfiguration.getCouchbaseAdminPassword());
        if (!clusterManager.listBuckets().contains(testConfiguration.getCouchbaseBucket())) {
            clusterManager.createNamedBucket(BucketType.COUCHBASE, testConfiguration.getCouchbaseBucket(), 100, 0, testConfiguration.getCouchbasePassword(), true);
            Thread.sleep(5000); // Given sometime for couchbase bucket to be created.
        }
    }

    @AfterClass
    public static void destroy() {
        clusterManager.flushBucket(testConfiguration.getCouchbaseBucket());
    }
}
