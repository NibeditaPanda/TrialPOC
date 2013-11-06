package com.tesco.core;

import com.mongodb.*;
import org.slf4j.Logger;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class DBFactory {
    private static final Logger logger = getLogger("Mongo Data Parsing");
    private Configuration configuration;

    public DBFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public DBCollection getCollection(String collectionName) {
        MongoClient mongoClient;
        String databaseName;
        try {
            String server = configuration.getDBHost();
            int port = configuration.getDBPort();
            databaseName = configuration.getDBName();
            List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
            String username = configuration.getUsername();
            String password = configuration.getPassword();
            if(!username.isEmpty())
                credentialsList.add(MongoCredential.createMongoCRCredential(username, databaseName, password.toCharArray()));
            mongoClient = new MongoClient(new ServerAddress(server, port), credentialsList, configureMongo());
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

