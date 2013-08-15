package com.tesco.services.resources.fixtures;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class TestStoreDBObject {

    private String storeId;
    private String zoneId = "5";
    private String currency = "GBP";

    public TestStoreDBObject(String storeId) {
        this.storeId = storeId;
    }

    public TestStoreDBObject withZoneId(String zoneId){
        this.zoneId = zoneId;
        return this;
    }

    public TestStoreDBObject withCurrency(String currency){
        this.currency = currency;
        return this;
    }

    public DBObject build(){
        DBObject store = new BasicDBObject();
        store.put("storeId", storeId);
        store.put("zoneId", zoneId);
        store.put("currency", currency);
        return store;
    }
}
