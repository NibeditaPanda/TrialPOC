package com.tesco.services.dao;

import com.google.common.base.Optional;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tesco.core.Configuration;
import com.tesco.core.DBFactory;
import com.tesco.services.exceptions.ItemNotFoundException;
import com.tesco.services.processor.PriceViewBuilder;

import java.net.UnknownHostException;
import java.util.List;

import static com.tesco.core.PriceKeys.ITEM_NUMBER;
import static com.tesco.core.PriceKeys.STORE_ID;

public class PriceDAO {

    private static final String PRODUCT_NOT_FOUND = "Price cannot be retrieved because product not found";
    private static final String STORE_NOT_FOUND = "Store not found";

    public final DBCollection priceCollection;
    public final DBCollection storeCollection;


    public PriceDAO(Configuration configuration) throws UnknownHostException {
        DBFactory dbFactory = new DBFactory(configuration);
        priceCollection = dbFactory.getCollection("prices");
        storeCollection = dbFactory.getCollection("stores");
    }

    public List<DBObject> getPricesInfo(List<String> itemNumbers) throws ItemNotFoundException {
        return new PriceViewBuilder().withPrices(getPricesBy(itemNumbers)).build();
    }

    public List<DBObject> getPriceAndStoreInfo(List<String> itemNumbers, String storeId) throws ItemNotFoundException {
        return new PriceViewBuilder().withPrices(getPricesBy(itemNumbers))
                .withStore(getStoreBy(storeId))
                .build();
    }

    private List<DBObject> getPricesBy(List<String> ids) throws ItemNotFoundException {
        Optional<List<DBObject>> items = Query.on(priceCollection).findMany(ITEM_NUMBER, ids);
        if (!items.isPresent()) throw new ItemNotFoundException(PRODUCT_NOT_FOUND);

        return items.get();
    }

    private DBObject getStoreBy(String storeId) throws ItemNotFoundException {
        Optional<DBObject> store = Query.on(storeCollection).findOne(STORE_ID, storeId);
        if (!store.isPresent()) throw new ItemNotFoundException(STORE_NOT_FOUND);

        return store.get();
    }
}
