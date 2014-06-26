package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.services.core.Promotion;

import java.util.Collections;
import java.util.List;

public class PromotionRepository {
    private UUIDGenerator uuidGenerator;
    private CouchbaseClient couchbaseClient;
    private CouchbaseWrapper couchbaseWrapper;

   /* public PromotionRepository(UUIDGenerator uuidGenerator, CouchbaseClient couchbaseClient) {
        this.uuidGenerator = uuidGenerator;
        this.couchbaseClient = couchbaseClient;
    }*/
   public PromotionRepository(UUIDGenerator uuidGenerator, CouchbaseWrapper couchbaseWrapper) {
       this.uuidGenerator = uuidGenerator;
       this.couchbaseWrapper = couchbaseWrapper;
   }

    public List<Promotion> getPromotionsByOfferIdZoneIdAndItemNumber(String offerId, String itemNumber, int zoneId) {
        //TODO: Get from couchbase here
        return Collections.EMPTY_LIST;
    }

    public void addPromotion(Promotion promotion) {
        String uniqueKey = uuidGenerator.getUUID();
        promotion.setUniqueKey(uniqueKey);

        //TODO: Store into couchbase here
    }

    public void updatePromotion(String uniqueKey, Promotion promotion) {
        //TODO: Store into couchbase here
    }
}
