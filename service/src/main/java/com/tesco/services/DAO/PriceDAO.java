package com.tesco.services.DAO;

import com.google.common.base.Optional;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tesco.services.Configuration;
import com.tesco.services.DBFactory;
import com.tesco.services.Exceptions.ItemNotFoundException;
import com.tesco.services.processor.PriceViewBuilder;

import java.net.UnknownHostException;

import static com.tesco.services.DAO.PriceKeys.ITEM_NUMBER;
import static com.tesco.services.DAO.PriceKeys.STORE_ID;

public class PriceDAO {

    private static final String PRODUCT_NOT_FOUND = "Product not found";
    private static final String STORE_NOT_FOUND = "Store not found";

    public final DBCollection priceCollection;
    public final DBCollection storeCollection;


    public PriceDAO(Configuration configuration) throws UnknownHostException {
        DBFactory dbFactory = new DBFactory(configuration);
        priceCollection = dbFactory.getCollection("prices");
        storeCollection = dbFactory.getCollection("stores");
    }

    public DBObject getPricesInfo(String itemNumber) throws ItemNotFoundException {
        return new PriceViewBuilder().withPrice(getPriceBy(itemNumber)).build();
    }

    public DBObject getPriceAndStoreInfo(String itemNumber, String storeId) throws ItemNotFoundException {
        return new PriceViewBuilder().withPrice(getPriceBy(itemNumber))
                .withStore(getStoreBy(storeId))
                .build();
    }

    private DBObject getPriceBy(String itemNumber) throws ItemNotFoundException {
        Optional<DBObject> item = Query.on(priceCollection).findOne(ITEM_NUMBER, itemNumber);
        if (!item.isPresent()) throw new ItemNotFoundException(PRODUCT_NOT_FOUND);

        return item.get();
    }

    private DBObject getStoreBy(String storeId) throws ItemNotFoundException {
        Optional<DBObject> store = Query.on(storeCollection).findOne(STORE_ID, storeId);
        if (!store.isPresent()) throw new ItemNotFoundException(STORE_NOT_FOUND);

        return store.get();
    }
}
