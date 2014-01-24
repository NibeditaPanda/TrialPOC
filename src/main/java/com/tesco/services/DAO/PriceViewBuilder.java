package com.tesco.services.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.services.exceptions.ItemNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static com.tesco.core.PriceKeys.*;

public class PriceViewBuilder {

    private DBObject storeInfo = new BasicDBObject();
    private List<DBObject> pricesInfo = new ArrayList<>();

    public PriceViewBuilder withPrices(List<DBObject> pricesInfo){
        this.pricesInfo = pricesInfo;
        return this;
    }

    public PriceViewBuilder withStore(DBObject storeInfo){
        this.storeInfo = storeInfo;
        return this;
    }

    public List<DBObject> build() throws ItemNotFoundException {
        ArrayList<DBObject> response = new ArrayList<>();

        for (DBObject price : pricesInfo) {
            BasicDBObject mappedPrice = mapPriceToDBObject(price);
            if(mappedPrice != null){
                response.add(mappedPrice);
            }
        }

        // TODO this isn't an exceptional circumstance we should probably use result objects not exceptions for this.
        if(response.size() == 0){
            throw new ItemNotFoundException("Product not found");
        }

        return response;
    }

    private BasicDBObject mapPriceToDBObject(DBObject priceInfoObj) {
        BasicDBObject responseObject = new BasicDBObject();

        String zoneId = storeInfo.get(PROMOTION_ZONE_ID) != null ? storeInfo.get(PROMOTION_ZONE_ID).toString() : NATIONAL_ZONE;
        DBObject zone = (DBObject) ((DBObject) priceInfoObj.get(ZONES)).get(zoneId);

        if (zone != null){
            Object currency = priceInfoObj.get(CURRENCY) != null ? priceInfoObj.get(CURRENCY) : DEFAULT_CURRENCY;
            responseObject.put(ITEM_NUMBER, priceInfoObj.get(ITEM_NUMBER));
            responseObject.put(CURRENCY, currency);
            responseObject.put(PRICE, zone.get(PRICE).toString());
            responseObject.put(PROMO_PRICE, zone.get(PROMO_PRICE).toString());
            if(zone.get(PROMOTIONS) != null) responseObject.put(PROMOTIONS, zone.get(PROMOTIONS));

            return responseObject;
        }

        return null;
    }
}