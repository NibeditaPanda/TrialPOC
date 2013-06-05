package com.tesco.adapters.core;

import com.mongodb.*;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class DBFactory {

    private static Logger logger = getLogger("Mongo Data Parsing");

    public static DBCollection getCollection(String collectionName) {
        MongoClient mongoClient;
        String databaseName;
        try {
            String server = Configuration.get().getString("mongodb.server");
            int port = Configuration.get().getInt("mongodb.port");
            databaseName = Configuration.get().getString("mongodb.dbname");
            List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
            String username = Configuration.get().getString("mongodb.username");
            String password = Configuration.get().getString("mongodb.password");
            if (!username.isEmpty()) {
                credentialsList.add(MongoCredential.createMongoCRCredential(username, databaseName, password.toCharArray()));
            }
            mongoClient = new MongoClient(new ServerAddress(server, port), credentialsList, configureMongo());
        } catch (ConfigurationException e) {
            logger.error("Error reading configuration", e);
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            logger.error("Error connecting to DB", e);
            throw new RuntimeException(e);
        }
        return mongoClient.getDB(databaseName).getCollection(collectionName);
    }

    //TODO: Tweak the driver options based on performance requirements
    //http://stackoverflow.com/questions/6520439/how-to-configure-mongodb-java-driver-mongooptions-for-production-use
    private static MongoClientOptions configureMongo() {
        return new MongoClientOptions.Builder()
                .socketKeepAlive(true)
                .build();
    }
}
