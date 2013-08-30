package com.tesco.services.DAO;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tesco.services.Configuration;
import com.tesco.services.DBFactory;
import com.tesco.services.Exceptions.ItemNotFoundException;

public class PromotionDAO {

    DBCollection promotions;

    public void PromotionDAO(Configuration config) {
        promotions = new DBFactory(config).getCollection("promotions");
    }

    public DBObject getOfferBy(String offerId) throws ItemNotFoundException {
        return promotions.find().toArray().get(0);
    }
}
