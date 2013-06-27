package com.tesco.services.processor;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;

public class PriceProcessor {

    public static final String NATIONAL_ZONE = "5";
    public static final String ZONE_ID = "zoneId";
    public static final String ZONES = "zones";
    public static final String DEFAULT_CURRENCY = "GBP";
    public static final String PRICE = "price";
    public static final String PROMO_PRICE = "promoPrice";
    public static final String CURRENCY = "currency";
    public static final String PROMOTIONS = "promotions";

    public PriceDAO priceDAO;

    public PriceProcessor(PriceDAO priceDAO) {
        this.priceDAO = priceDAO;
    }

    public Optional<DBObject> getPricesFor(String itemNumber, Optional<String> storeId) {
        if (storeId.isPresent()) return getPriceByStore(itemNumber, storeId.get());
        return getNationalPrice(itemNumber);
    }

    private Optional<DBObject> getPriceByStore(String itemNumber, String storeId) {

        Optional<DBObject> item = priceDAO.getPrice(itemNumber);
        Optional<DBObject> store = priceDAO.getStore(storeId);

        if (!item.isPresent() || !store.isPresent()) return Optional.absent();

        String zoneId = store.get().get(ZONE_ID).toString();
        String currency = store.get().get(CURRENCY).toString();

        return Optional.fromNullable(buildResponse(itemNumber, currency, zoneId, item.get()));
    }

    private Optional<DBObject> getNationalPrice(String itemNumber) {
        Optional<DBObject> item = priceDAO.getPrice(itemNumber);

        if (!item.isPresent()) return Optional.absent();

        return Optional.fromNullable(buildResponse(itemNumber, DEFAULT_CURRENCY, NATIONAL_ZONE, item.get()));
    }

    private DBObject buildResponse(String itemNumber, String currency, String zoneId, DBObject priceInfo){
        BasicDBObject responseObject = new BasicDBObject();
        responseObject.put(PriceDAO.ITEM_NUMBER, itemNumber);
        responseObject.put(CURRENCY, currency);

        DBObject zones = (DBObject) priceInfo.get(ZONES);
        DBObject zone = (DBObject) zones.get(zoneId);

        responseObject.put(PRICE, zone.get(PRICE).toString());
        responseObject.put(PROMO_PRICE, zone.get(PROMO_PRICE).toString());
        responseObject.put(PROMOTIONS, zone.get(PROMOTIONS));
        return responseObject;
    }
}