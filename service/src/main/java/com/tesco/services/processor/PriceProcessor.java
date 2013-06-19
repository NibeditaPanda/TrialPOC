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

        DBObject zones = (DBObject) item.get().get(ZONES);
        DBObject zone = (DBObject) zones.get(zoneId);
        String price = zone.get(PRICE).toString();
        String promoPrice = zone.get(PROMO_PRICE).toString();

        return Optional.fromNullable(buildPriceResponse(itemNumber, price, promoPrice, currency));
    }

    private Optional<DBObject> getNationalPrice(String itemNumber) {
        Optional<DBObject> item = priceDAO.getPrice(itemNumber);
        if (item.isPresent()) {
            String price = ((DBObject) ((DBObject) item.get().get(ZONES)).get(NATIONAL_ZONE)).get(PRICE).toString();
            String promoPrice = ((DBObject) ((DBObject) item.get().get(ZONES)).get(NATIONAL_ZONE)).get(PROMO_PRICE).toString();
            return Optional.fromNullable(buildPriceResponse(itemNumber, price, promoPrice, DEFAULT_CURRENCY));
        }
        return item;
    }

    private DBObject buildPriceResponse(String itemNumber, String price, String promoPrice, String currency) {
        BasicDBObject responseObject = new BasicDBObject();
        responseObject.put(PriceDAO.ITEM_NUMBER, itemNumber);
        responseObject.put(PRICE, price);
        responseObject.put(PROMO_PRICE, promoPrice);
        responseObject.put(CURRENCY, currency);
        return responseObject;
    }
}