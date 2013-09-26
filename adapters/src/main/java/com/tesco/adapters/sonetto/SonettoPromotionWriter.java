package com.tesco.adapters.sonetto;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tesco.adapters.core.PriceKeys.PROMOTION_OFFER_ID;

public class SonettoPromotionWriter {

    private DBCollection promotionCollection;
    private Logger logger;
    private int updateCount;

    public SonettoPromotionWriter(DBCollection promotionCollection) {
        this.promotionCollection = promotionCollection;
        this.logger = LoggerFactory.getLogger(getClass().getName());
    }

    public void updatePromotion(DBObject promotion) {
        String identifierValue = promotion.get(PROMOTION_OFFER_ID).toString();

        DBObject promotionExistsQuery = new BasicDBObject(PROMOTION_OFFER_ID, identifierValue);
        //It won't create a new document if not found
        WriteResult writeResult = promotionCollection.update(promotionExistsQuery, new BasicDBObject( "$set", promotion), false, true);

        logUpdate(promotion, writeResult);
    }

    public void createPromotion(DBObject promotion){
        WriteResult writeResult = promotionCollection.insert(promotion);
        logCreate(promotion, writeResult);
    }

    private void logCreate(DBObject promotion, WriteResult insertResponse)
    {
        if(logError(promotion, insertResponse))
        {
            logger.debug("Inserted entry: " + promotion.toString());
            updateCount++;
        }
    }

    private boolean logError(DBObject promotion, WriteResult updateResponse)
    {
        if (updateResponse.getError() != null) {
            String errorMessage = String.format("error on upserting entry \"%s\": %s", promotion.toString(), updateResponse.toString());
            logger.error(errorMessage);
            return true;
        }

        return false;
    }

    private void logUpdate(DBObject promotion, WriteResult updateResponse) {
        logError(promotion, updateResponse);
        if (Boolean.parseBoolean(updateResponse.getField("updatedExisting").toString())) {
            logger.debug("Updated entry: " + promotion.toString());
            updateCount++;
        }
    }

    public int getUpdateCount() {
        return updateCount;
    }
}
