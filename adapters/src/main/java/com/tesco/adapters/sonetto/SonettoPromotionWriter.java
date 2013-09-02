package com.tesco.adapters.sonetto;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.tesco.adapters.core.PriceKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SonettoPromotionWriter {

    private DBCollection promotionCollection;
    private Logger logger;
    private int updateCount;

    public SonettoPromotionWriter(DBCollection promotionCollection) {
        this.promotionCollection = promotionCollection;
        this.logger = LoggerFactory.getLogger(getClass().getName());
    }

    public void write(DBObject promotion) {
        String identifierValue = promotion.get(PriceKeys.PROMOTION_OFFER_ID).toString();

        DBObject promotionExistsQuery = new BasicDBObject(PriceKeys.PROMOTION_OFFER_ID, identifierValue);
        //It won't create a new document if not found
        WriteResult writeResult = promotionCollection.update(promotionExistsQuery, new BasicDBObject( "$set", promotion), false, true);

        log(promotion, writeResult);
    }

    private void log(DBObject product, WriteResult updateResponse) {
        if (updateResponse.getError() != null) {
            String errorMessage = String.format("error on upserting entry \"%s\": %s", product.toString(), updateResponse.toString());
            logger.error(errorMessage);
        } else if (Boolean.parseBoolean(updateResponse.getField("updatedExisting").toString())) {
            logger.debug("Updated entry: " + product.toString());
            updateCount++;
        }
    }

    public int getUpdateCount() {
        return updateCount;
    }
}
