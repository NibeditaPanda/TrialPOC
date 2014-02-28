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

public class CouchbaseConnectionManager {
    public static final int BUCKET_MEMORY_SIZE_MB = 256;
    public static final int NO_OF_REPLICAS = 0;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CouchbaseConnectionManager.class);
    private static final String BUCKET_ONE = "Bucket_One";
    private static final String BUCKET_TWO = "Bucket_Two";
    public static final String BUCKET_PASSWORD = "tesco";

    private List<URI> hosts;
    private final ClusterManager clusterManager;
    private String currentBucket;
    private CouchbaseClient couchbaseClient;

    public CouchbaseConnectionManager(Configuration configuration) throws URISyntaxException, IOException, InterruptedException {
        // (Subset) of nodes in the cluster to establish a connection
        hosts = Arrays.asList(
                new URI("http://127.0.0.1:8091/pools")
        );

        clusterManager = new ClusterManager(hosts, "admin", "password");

        final List<String> buckets = clusterManager.listBuckets();
        if(buckets.contains(BUCKET_ONE)) currentBucket = BUCKET_ONE;
        if(buckets.contains(BUCKET_TWO)) currentBucket = BUCKET_TWO;
        if(currentBucket == null) {
            currentBucket = BUCKET_ONE;
            clusterManager.createNamedBucket(BucketType.COUCHBASE, currentBucket, BUCKET_MEMORY_SIZE_MB, NO_OF_REPLICAS, BUCKET_PASSWORD, false);
        }

        initCouchbaseClient(currentBucket);
    }

    private void initCouchbaseClient(String bucketName) throws IOException, InterruptedException {
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
                return;
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

    public CouchbaseClient getCouchbaseClient() throws IOException {
        try {
            couchbaseClient.get("KEY_TO_CHECK_CONNECTION_IS_ALIVE");
            return couchbaseClient;
        } catch (Exception e) { // TODO: IllegalStateException is thrown when the bucket is shutdown, but not documented. See what is the appropriate exception.
            final String alternateBucketName = toggleBucketName();
            logger.warn(String.format("Could not create CouchbaseClient for bucket %s. Switching over to alternate bucket %s.", currentBucket, alternateBucketName));
            currentBucket = alternateBucketName;

            try {
                couchbaseClient.shutdown();
            } catch (Exception e1) {
                e1.printStackTrace(); //TODO: Figure out how to gracefully shutdown the client when import adapter wants to delete the bucket after import.
            }

            couchbaseClient = new CouchbaseClient(hosts, alternateBucketName, BUCKET_PASSWORD);

            return couchbaseClient;
        }
    }


}
