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
    private final DBCollection storeCollection;

    public PriceDAO(Configuration configuration) throws UnknownHostException {
        DBFactory dbFactory = new DBFactory(configuration);
        priceCollection = dbFactory.getCollection("prices");
        storeCollection = dbFactory.getCollection("stores");
    }

    public List<DBObject> getPriceBy(String key, String value) {
        DBObject query = QueryBuilder.start(key).is(value).get();
        return priceCollection.find(query, new BasicDBObject("_id", 0)).toArray();
    }

    public List<DBObject> getPriceByZone(String zone) {
        DBObject query = new BasicDBObject(String.format("zones.%s.price", zone), new BasicDBObject("$exists", true));
        return priceCollection.find(query, new BasicDBObject("_id", 0)).toArray();
    }

    public List<DBObject> getPriceByStore(String store) {
        DBObject query = new BasicDBObject("storeId", store);
        DBObject storeResult = storeCollection.find(query, new BasicDBObject("_id", 0)).toArray().get(0);
        String zoneId = (String) storeResult.get("zoneId");
        return getPriceByZone(zoneId);
    }
}
