package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.tesco.services.Configuration;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CouchbaseConnectionManager {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CouchbaseConnectionManager.class);
    private final String bucketPassword;

    private List<URI> hosts=new ArrayList<>();
    private String bucketName;
    private CouchbaseClient couchbaseClient;

    public CouchbaseConnectionManager(Configuration configuration) throws URISyntaxException, IOException, InterruptedException {
        //  Changed the db server url to (Subset) of nodes in the cluster to establish a connection

        String[] nodes=configuration.getCouchbaseNodes();
        for(String node: nodes) {
            hosts.add(new URI(node));
        }
        bucketName = configuration.getDBBucketName();
        bucketPassword = configuration.getDBBucketPassword();
        try {
            couchbaseClient = new CouchbaseClient(hosts, bucketName, bucketPassword);
        } catch (Exception e) {
            final String errorMsg = String.format("Could not connect to the Couchbase bucket '%s' @ nodes : %s", bucketName, configuration.getDBServerUrl());
            logger.error(errorMsg, e);
            throw e;
        }
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
