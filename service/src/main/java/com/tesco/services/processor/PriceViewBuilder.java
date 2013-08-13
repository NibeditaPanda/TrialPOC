package com.tesco.services.processor;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.services.Exceptions.ItemNotFoundException;

import static com.tesco.services.DAO.PriceKeys.*;

public class PriceViewBuilder {

    private DBObject storeInfo = new BasicDBObject();
    private DBObject priceInfo = new BasicDBObject();

    public PriceViewBuilder withPrice(DBObject priceInfo){
        this.priceInfo = priceInfo;
        return this;
    }

    public PriceViewBuilder withStore(DBObject storeInfo){
        this.storeInfo = storeInfo;
        return this;
    }

    public DBObject build() throws ItemNotFoundException {
        BasicDBObject responseObject = new BasicDBObject();
        String zoneId = storeInfo.get(ZONE_ID) != null ? storeInfo.get(ZONE_ID).toString() : NATIONAL_ZONE;
        DBObject zone = (DBObject) ((DBObject) priceInfo.get(ZONES)).get(zoneId);
        if (zone == null) throw new ItemNotFoundException("Product not found");
        Object currency = priceInfo.get(CURRENCY) != null ? priceInfo.get(CURRENCY) : DEFAULT_CURRENCY;

        responseObject.put(ITEM_NUMBER, priceInfo.get(ITEM_NUMBER));
        responseObject.put(CURRENCY, currency);
        responseObject.put(PRICE, zone.get(PRICE).toString());
        responseObject.put(PROMO_PRICE, zone.get(PROMO_PRICE).toString());
        if(zone.get(PROMOTIONS) != null) responseObject.put(PROMOTIONS, zone.get(PROMOTIONS));
        return responseObject;
    }
}