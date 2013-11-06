package com.tesco.services.DAO;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.tesco.core.Configuration;
import com.tesco.core.DBFactory;
import com.tesco.services.resources.model.Promotion;
import org.mongojack.JacksonDBCollection;

import java.util.List;

import static com.tesco.core.PriceKeys.*;

public class PromotionDAO {

    DBCollection promotions;
    DBCollection stores;

    public PromotionDAO(Configuration config) {
        promotions = new DBFactory(config).getCollection(PROMOTION_COLLECTION);
        stores = new DBFactory(config).getCollection(STORE_COLLECTION);
    }

    public List<Promotion> findOffers(List<String> ids)
    {
        DBObject query = QueryBuilder.start(PROMOTION_OFFER_ID).in(ids).get();

        JacksonDBCollection<Promotion, String> promotionCollections = JacksonDBCollection.wrap(promotions, Promotion.class,
                String.class);

        return promotionCollections.find(query, new BasicDBObject("_id", 0)).toArray();
    }

}
