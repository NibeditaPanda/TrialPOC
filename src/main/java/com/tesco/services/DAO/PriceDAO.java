package com.tesco.services.DAO;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.tesco.services.Configuration;
import com.tesco.services.DBFactory;

import java.net.UnknownHostException;
import java.util.List;

public class PriceDAO {


    private final DBCollection priceCollection;

    public PriceDAO(Configuration configuration) throws UnknownHostException {
        DBFactory dbFactory = new DBFactory(configuration);
        priceCollection = dbFactory.getCollection("prices");
    }

    public List<DBObject> getPriceBy(String key, String value) {
        DBObject query = QueryBuilder.start(key).is(value).get();
        return priceCollection.find(query, new BasicDBObject("_id", 0)).toArray();
    }
}
