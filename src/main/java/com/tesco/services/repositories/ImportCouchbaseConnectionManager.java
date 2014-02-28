package com.tesco.services.repositories;

import com.couchbase.client.ClusterManager;
import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.clustermanager.BucketType;
import com.couchbase.client.vbucket.config.ConfigParsingException;
import com.tesco.services.Configuration;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class ImportCouchbaseConnectionManager {
    public static final int BUCKET_MEMORY_SIZE_MB = 256;
    public static final int NO_OF_REPLICAS = 0;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ImportCouchbaseConnectionManager.class);
    private static final String BUCKET_ONE = "Bucket_One";
    private static final String BUCKET_TWO = "Bucket_Two";
    public static final String BUCKET_PASSWORD = "tesco";

    private List<URI> hosts;
    private final ClusterManager clusterManager;
    private String currentBucket;
    private CouchbaseClient couchbaseClient;

    public ImportCouchbaseConnectionManager(Configuration configuration) throws URISyntaxException, IOException, InterruptedException {
        // (Subset) of nodes in the cluster to establish a connection
        hosts = Arrays.asList(
                new URI("http://127.0.0.1:8091/pools")
        );

        clusterManager = new ClusterManager(hosts, "admin", "password");

        final List<String> buckets = clusterManager.listBuckets();
        if(buckets.contains(BUCKET_ONE)) currentBucket = BUCKET_ONE;
        if(buckets.contains(BUCKET_TWO)) currentBucket = BUCKET_TWO;
    }

    public CouchbaseClient getReplacementBucketClient() throws IOException, InterruptedException {
        String replacementBucketName = toggleBucketName();

        clusterManager.createNamedBucket(BucketType.COUCHBASE, replacementBucketName, BUCKET_MEMORY_SIZE_MB, NO_OF_REPLICAS, BUCKET_PASSWORD, false);

        return getCouchbaseClient(replacementBucketName);
    }

    private CouchbaseClient getCouchbaseClient(String bucketName) throws IOException, InterruptedException {
        int timeout = 2000;

        int retryAttempt = 0;   /* TODO: Sometimes, the buckets are not ready immediately after creation of new bucket. Needs to be verified if this is right approach.
                                   As per this documentation, http://docs.couchbase.com/couchbase-devguide-2.5/#creating-a-bucket
                                  "You can check your new bucket exists and is running by making a request REST request to the new bucket:
                                   curl http://localhost:8091/pools/default/buckets/newBucket" */

        while (retryAttempt < 5) {
            try {
                synchronized (this) {
                    wait(timeout);
                }

                couchbaseClient = new CouchbaseClient(hosts, bucketName, BUCKET_PASSWORD);
                return couchbaseClient;
            } catch (ConfigParsingException e) {
                logger.warn(String.format("Could not connect to the newly created bucket '%s'. Will retry in %d seconds", bucketName, timeout));
                timeout *= 2;
                retryAttempt++;
            }
        }

        throw new RuntimeException("Could not obtain connection to Couchbase");
    }

    private String toggleBucketName() {
        return (currentBucket == BUCKET_ONE) ? BUCKET_TWO : BUCKET_ONE;
    }

    public void replaceCurrentWithRefresh() {
        final String oldBucketToDelete = currentBucket;

        currentBucket = toggleBucketName();

        couchbaseClient.shutdown();
        if (oldBucketExists(oldBucketToDelete)) {
            try {
                clusterManager.deleteBucket(oldBucketToDelete);
            } catch (RuntimeException e) {
                logger.error(String.format("Bucket named %s could not be deleted. See the attached error for more details.", oldBucketToDelete), e);
                throw e;
            }
        }

//        clusterManager.updateBucket(currentBucket, BUCKET_MEMORY_SIZE_MB, AuthType.SASL, NO_OF_REPLICAS, PORT, BUCKET_PASSWORD, false);
    }

    private boolean oldBucketExists(String oldBucketToDelete) {
        return oldBucketToDelete != null && clusterManager.listBuckets().contains(oldBucketToDelete);
    }
}