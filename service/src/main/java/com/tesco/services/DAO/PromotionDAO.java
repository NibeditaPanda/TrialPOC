package com.tesco.services.DAO;

import com.google.common.base.Optional;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tesco.services.Configuration;
import com.tesco.services.DBFactory;
import com.tesco.services.Exceptions.ItemNotFoundException;

import java.util.List;

import static com.tesco.services.DAO.PriceKeys.PROMOTION_COLLECTION;
import static com.tesco.services.DAO.PriceKeys.PROMOTION_OFFER_ID;

public class PromotionDAO {

    private static final String PROMOTION_NOT_FOUND = "Promotion not found";
    DBCollection promotions;


    public PromotionDAO(Configuration config) {
        promotions = new DBFactory(config).getCollection(PROMOTION_COLLECTION);
    }

    public DBObject getOfferBy(String offerId) throws ItemNotFoundException {
        Optional<DBObject> item = Query.on(promotions).findOne(PROMOTION_OFFER_ID, offerId);
        if (!item.isPresent()) throw new ItemNotFoundException(PROMOTION_NOT_FOUND);

        return item.get();
    }

    public List<DBObject> findOffersForTheseIds(List<String> ids) throws ItemNotFoundException {
        Optional<List<DBObject>> items = Query.on(promotions).findMany(PROMOTION_OFFER_ID, ids);
        if (!items.isPresent()) throw new ItemNotFoundException(PROMOTION_NOT_FOUND);

        return items.get();
    }
}
