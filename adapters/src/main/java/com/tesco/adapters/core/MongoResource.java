package com.tesco.adapters.core;

import com.mongodb.*;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public enum MongoResource {
    INSTANCE;

    private static Logger logger = getLogger(MongoResource.class);

    private DB mongoClient;

    private MongoResource() {
        if (mongoClient == null) {
            mongoClient = getClient();
        }
    }

    public DB getMongoClient() {
        return mongoClient;
    }

    private DB getClient() {
        try {
            String server = Configuration.get().getString("mongodb.server");
            int port = Configuration.get().getInt("mongodb.port");
            String databaseName = Configuration.get().getString("mongodb.dbname");
            String username = Configuration.get().getString("mongodb.username");
            String password = Configuration.get().getString("mongodb.password");

            List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();

            if (!username.isEmpty()) {
                credentialsList.add(MongoCredential.createMongoCRCredential(username, databaseName, password.toCharArray()));
            }
            return new MongoClient(new ServerAddress(server, port), credentialsList, configureMongo()).getDB(databaseName);
        } catch (ConfigurationException e) {
            logger.error("Error reading configuration", e);
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            logger.error("Error connecting to DB", e);
            throw new RuntimeException(e);
        }
    }

    //TODO: Tweak the driver options based on performance requirements
    //http://stackoverflow.com/questions/6520439/how-to-configure-mongodb-java-driver-mongooptions-for-production-use
    private static MongoClientOptions configureMongo() {
        return new MongoClientOptions.Builder()
                .socketKeepAlive(true)
                .build();
    }
}
