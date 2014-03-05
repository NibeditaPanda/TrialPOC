package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.tesco.services.Configuration;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class CouchbaseConnectionManager {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CouchbaseConnectionManager.class);
    private final String bucketPassword;

    private List<URI> hosts;
    private String bucketName;
    private CouchbaseClient couchbaseClient;

    public CouchbaseConnectionManager(Configuration configuration) throws URISyntaxException, IOException, InterruptedException {
        // TODO: Change the db server url to (Subset) of nodes in the cluster to establish a connection
        hosts = Arrays.asList(
                new URI(configuration.getDBServerUrl())
        );

        bucketName = configuration.getDBBucketName();
        bucketPassword = configuration.getDBBucketPassword();
        couchbaseClient = new CouchbaseClient(hosts, bucketName, bucketPassword);
    }

    public CouchbaseClient getCouchbaseClient() throws IOException {
        try {
            couchbaseClient.get("KEY_TO_CHECK_CONNECTION_IS_ALIVE");

            return couchbaseClient;
        } catch (Exception e) {
            logger.warn(String.format("Could not connect to bucket %s. Attempting to reconnect", bucketName));
            couchbaseClient = new CouchbaseClient(hosts, bucketName, bucketPassword);

            return couchbaseClient;
        }
    }
}
