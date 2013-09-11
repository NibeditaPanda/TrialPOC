package com.tesco.services.DAO;

import com.google.common.base.Optional;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tesco.services.Configuration;
import com.tesco.services.DBFactory;
import com.tesco.services.Exceptions.ItemNotFoundException;

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

    public Result<DBObject> findTheseOffersAndFilterBy(List<String> ids, String tpnb, String storeId) {

        Optional<DBObject> store = Query.on(stores).findOne(STORE_ID, storeId);
        if (store.isPresent()) {
            String promotionZone = (String) store.get().get(PROMOTION_ZONE_ID);

            List<DBObject> promotions = findOffersForTheseIds(ids).items();
            List<DBObject> promotionThatMatch = new ArrayList<>();
            for(int i = 0; i < promotions.size(); i++) {
                DBObject promotion = promotions.get(i);
                if(promotion.get(ITEM_NUMBER).equals(tpnb) && promotion.get(PROMOTION_ZONE_ID).equals(promotionZone)){
                    promotionThatMatch.add(promotion);
                }
            }
            return new Result<>(promotionThatMatch);
        } else {
            return Result.empty();
        }

    }
}
