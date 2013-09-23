package com.tesco.services.DAO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.mongodb.*;
import com.tesco.services.Configuration;
import com.tesco.services.DBFactory;
import com.tesco.services.Exceptions.ItemNotFoundException;
import com.tesco.services.resources.model.Promotion;
import org.mongojack.*;
import org.mongojack.DBCursor;

import java.util.ArrayList;
import java.util.List;

import static com.tesco.services.DAO.PriceKeys.*;

public class PromotionDAO {

    private static final String PROMOTION_NOT_FOUND = "Promotion not found";
    DBCollection promotions;
    DBCollection stores;


    public PromotionDAO(Configuration config) {
        promotions = new DBFactory(config).getCollection(PROMOTION_COLLECTION);
        stores = new DBFactory(config).getCollection(STORE_COLLECTION);
    }

    public Result<DBObject> findOffersForTheseIds(List<String> ids) {
        return new Result<>(Query.on(promotions).findMany(PROMOTION_OFFER_ID, ids));
    }

    public List<Promotion> findOffers(List<String> ids)
    {
        DBObject query = QueryBuilder.start(PROMOTION_OFFER_ID).in(ids).get();

        JacksonDBCollection<Promotion, String> promotionCollections = JacksonDBCollection.wrap(promotions, Promotion.class,
                String.class);

        return promotionCollections.find(query, new BasicDBObject("_id", 0)).toArray();
    }

    public Result<DBObject> findTheseOffersAndFilterBy(List<String> ids, String tpnb, String storeId) {

        Optional<DBObject> store = Query.on(stores).findOne(STORE_ID, storeId);
        if (store.isPresent()) {
            String promotionZone = (String) store.get().get(PROMOTION_ZONE_ID);

            List<DBObject> promotions = findOffersForTheseIds(ids).items();
            List<DBObject> promotionThatMatch = new ArrayList<>();
            for(int i = 0; i < promotions.size(); i++) {
                DBObject promotion = promotions.get(i);
                if(promotion.get(ITEM_NUMBER).equals(tpnb) && promotion.get(ZONE_ID).equals(promotionZone)){
                    promotionThatMatch.add(promotion);
                }
            }
            return new Result<>(promotionThatMatch);
        } else {
            return Result.empty();
        }

    }
}
