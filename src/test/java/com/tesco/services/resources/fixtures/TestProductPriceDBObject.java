package com.tesco.services.resources.fixtures;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

public class TestProductPriceDBObject {

    private String itemNumber;
    private String price;
    private String promoPrice;
    private List<DBObject> promotions = new ArrayList<DBObject>();
    private DBObject zones = new BasicDBObject();

    public TestProductPriceDBObject(String itemNumber){
        this.itemNumber = itemNumber;
    }

    public TestProductPriceDBObject withPrice(String price){
        this.price = price;
        return this;
    }

    public TestProductPriceDBObject withPromotionPrice(String promoPrice){
        this.promoPrice = promoPrice;
        return this;
    }

    public TestProductPriceDBObject addPromotion(DBObject promotion){
        this.promotions.add(promotion);
        return this;
    }

    public TestProductPriceDBObject inZone(String zoneId){
        DBObject zone = new BasicDBObject();
        zone.put("price", price);
        zone.put("promoPrice", promoPrice);
        if (!promotions.isEmpty()) zone.put("promotions", promotions);
        zones.put(zoneId, zone);
        price = null;
        promoPrice = null;
        promotions = new ArrayList<DBObject>();
        return this;
    }

    public DBObject build(){
        DBObject price = new BasicDBObject();
        price.put("itemNumber", itemNumber);
        price.put("zones", zones);
        return price;
    }

}
