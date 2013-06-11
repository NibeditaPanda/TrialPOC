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

    public static final String DEFAULT_CURRENCY = "GBP";
    public static final String NATIONAL_ZONE = "5";

    public static final String PRICES_COLLECTION = "prices";
    public static final String STORES_COLLECTION = "stores";

    public static final String ITEM_NUMBER = "itemNumber";
    public static final String STORE_ID = "storeId";
    public static final String ZONE_ID = "zoneId";
    public static final String ZONES = "zones";
    public static final String PRICE = "price";
    public static final String CURRENCY = "currency";

    private final DBCollection priceCollection;
    private final DBCollection storeCollection;


    public PriceDAO(Configuration configuration) throws UnknownHostException {
        DBFactory dbFactory = new DBFactory(configuration);
        priceCollection = dbFactory.getCollection(PRICES_COLLECTION);
        storeCollection = dbFactory.getCollection(STORES_COLLECTION);
    }

    public DBObject getPriceByStore(String itemNumber, String storeId) {
        List<DBObject> items = find(priceCollection, ITEM_NUMBER, itemNumber);
        List<DBObject> stores = find(storeCollection, STORE_ID, storeId);
        if (items.isEmpty() || stores.isEmpty()) return null;

        DBObject item = items.get(0);
        DBObject store = stores.get(0);

        String zoneId = store.get(ZONE_ID).toString();
        String currency = store.get(CURRENCY).toString();

        DBObject zones = (DBObject) item.get(ZONES);
        DBObject zone = (DBObject) zones.get(zoneId);
        String price = zone.get(PRICE).toString();

        return buildResponse(itemNumber, price, currency);
    }

    public DBObject getNationalPrice(String itemNumber) {
        List<DBObject> items = find(priceCollection, ITEM_NUMBER, itemNumber);
        if(items.isEmpty()) return null;

        DBObject item = items.get(0);
        String price = ((DBObject) ((DBObject) item.get(ZONES)).get(NATIONAL_ZONE)).get(PRICE).toString();
        return buildResponse(itemNumber, price, DEFAULT_CURRENCY);
    }

    private DBObject buildResponse(String itemNumber, String price, String currency) {
        BasicDBObject responseObject = new BasicDBObject();
        responseObject.put(ITEM_NUMBER, itemNumber);
        responseObject.put(PRICE, price);
        responseObject.put(CURRENCY, currency);
        return responseObject;
    }

    private List<DBObject> find(DBCollection collection, String key, String value) {
        DBObject query = QueryBuilder.start(key).is(value).get();
        return collection.find(query, new BasicDBObject("_id", 0)).toArray();
    }
}
