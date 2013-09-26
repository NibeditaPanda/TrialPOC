package com.tesco.adapters.core;

import com.mongodb.DBCollection;

public class DBFactory {

    public static DBCollection getCollection(String collectionName) {
        return MongoResource.INSTANCE.getMongoClient().getCollection(collectionName);
    }

}
