package com.tesco.services.DAO;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.tesco.services.Configuration;
import com.tesco.services.DBFactory;

import java.net.UnknownHostException;
import java.util.List;

public class PriceDAO {

    public static final String ITEM_NUMBER = "itemNumber";
    public static final String STORE_ID = "storeId";

    public final DBCollection priceCollection;
    public final DBCollection storeCollection;


    public PriceDAO(Configuration configuration) throws UnknownHostException {
        DBFactory dbFactory = new DBFactory(configuration);
        priceCollection = dbFactory.getCollection("prices");
        storeCollection = dbFactory.getCollection("stores");
    }

    public Optional<DBObject> getPrice(String itemNumber) {
        List<DBObject> items = find(priceCollection, ITEM_NUMBER, itemNumber);
        return Optional.fromNullable(items.isEmpty() ? null : items.get(0));
    }

    public Optional<DBObject> getStore(String storeId) {
        List<DBObject> stores = find(storeCollection, STORE_ID, storeId);
        return Optional.fromNullable(stores.isEmpty() ? null : stores.get(0));
    }

    private List<DBObject> find(DBCollection collection, String key, String value) {
        DBObject query = QueryBuilder.start(key).is(value).get();
        return collection.find(query, new BasicDBObject("_id", 0)).toArray();
    }
}
