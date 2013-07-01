package com.tesco.services.processor;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.Exceptions.ItemNotFoundException;

public class PriceProcessor {

    public static final String NATIONAL_ZONE = "5";
    public static final String ZONE_ID = "zoneId";
    public static final String ZONES = "zones";
    public static final String DEFAULT_CURRENCY = "GBP";
    public static final String PRICE = "price";
    public static final String PROMO_PRICE = "promoPrice";
    public static final String CURRENCY = "currency";
    public static final String PROMOTIONS = "promotions";
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String STORE_NOT_FOUND = "Store not found";

    public PriceDAO priceDAO;

    public PriceProcessor(PriceDAO priceDAO) {
        this.priceDAO = priceDAO;
    }

    public DBObject getPricesFor(String itemNumber, Optional<String> storeId) throws ItemNotFoundException {
        if (storeId.isPresent()) return getPriceByStore(itemNumber, storeId.get());
        return getNationalPrice(itemNumber);
    }

    private DBObject getPriceByStore(String itemNumber, String storeId) throws ItemNotFoundException {

        Optional<DBObject> item = priceDAO.getPrice(itemNumber);
        Optional<DBObject> store = priceDAO.getStore(storeId);

        if (!item.isPresent()) throw new ItemNotFoundException(PRODUCT_NOT_FOUND);
        if (!store.isPresent()) throw new ItemNotFoundException(STORE_NOT_FOUND);

        String zoneId = store.get().get(ZONE_ID).toString();
        String currency = store.get().get(CURRENCY).toString();

        return buildResponse(itemNumber, currency, zoneId, item.get());
    }

    private DBObject getNationalPrice(String itemNumber) throws ItemNotFoundException {
        Optional<DBObject> item = priceDAO.getPrice(itemNumber);

        if (!item.isPresent()) throw new ItemNotFoundException(PRODUCT_NOT_FOUND);

        return buildResponse(itemNumber, DEFAULT_CURRENCY, NATIONAL_ZONE, item.get());
    }

    private DBObject buildResponse(String itemNumber, String currency, String zoneId, DBObject priceInfo) throws ItemNotFoundException {
        DBObject zones = (DBObject) priceInfo.get(ZONES);
        DBObject zone = (DBObject) zones.get(zoneId);
        if(zone == null) throw new ItemNotFoundException(PRODUCT_NOT_FOUND);

        BasicDBObject responseObject = new BasicDBObject();
        responseObject.put(PriceDAO.ITEM_NUMBER, itemNumber);
        responseObject.put(CURRENCY, currency);
        responseObject.put(PRICE, zone.get(PRICE).toString());
        responseObject.put(PROMO_PRICE, zone.get(PROMO_PRICE).toString());
        if(zone.get(PROMOTIONS) != null) responseObject.put(PROMOTIONS, zone.get(PROMOTIONS));
        return responseObject;
    }
}