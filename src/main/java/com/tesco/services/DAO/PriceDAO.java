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

    public DBObject getPriceByStore(String itemNumber, String storeId) {
        List<DBObject> items = find(priceCollection, "itemNumber", itemNumber);
        List<DBObject> stores = find(storeCollection, "storeId", storeId);
        if (items.isEmpty() || stores.isEmpty()) return null;

        DBObject item = items.get(0);
        DBObject store = stores.get(0);

        System.out.println("**** store info ****");
        System.out.println(store);
        System.out.println(item);


        String zone = store.get("zoneId").toString();
        String currency = store.get("currency").toString();
        String price = ((DBObject) ((DBObject) item.get("zones")).get(zone)).get("price").toString();

        return buildResponse(itemNumber, price, currency);
    }

    public DBObject getNationalPrice(String itemNumber) {
        List<DBObject> items = find(priceCollection, "itemNumber", itemNumber);
        if(items.isEmpty()) return null;

        DBObject item = items.get(0);
        String price = ((DBObject) ((DBObject) item.get("zones")).get("5")).get("price").toString();
        return buildResponse(itemNumber, price, "GBP");
    }

    private DBObject buildResponse(String itemNumber, String price, String currency) {
        BasicDBObject responseObject = new BasicDBObject();
        responseObject.put("itemNumber", itemNumber);
        responseObject.put("price", price);
        responseObject.put("currency", currency);
        return responseObject;
    }

    private List<DBObject> find(DBCollection collection, String key, String value) {
        DBObject query = QueryBuilder.start(key).is(value).get();
        return collection.find(query, new BasicDBObject("_id", 0)).toArray();
    }
}
