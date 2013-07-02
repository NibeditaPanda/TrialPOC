package com.tesco.adapters.rpm;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;

public class RPMWriter {
    private DBCollection priceCollection;
    private DBCollection storeCollection;
    private String RPMPriceZoneCsvFile;
    private String RPMStoreZoneCsvFilePath;
    private String RPMPromotionCsvFilePath;
    private Logger logger;
    private int insertCount;
    private int updateCount;

    public RPMWriter(DBCollection priceCollection, DBCollection storeCollection, String RPMPriceZoneCsvFile, String RPMStoreZoneCsvFilePath, String RPMPromotionCsvFilePath) {
        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.RPMPriceZoneCsvFile = RPMPriceZoneCsvFile;
        this.RPMStoreZoneCsvFilePath = RPMStoreZoneCsvFilePath;
        this.RPMPromotionCsvFilePath = RPMPromotionCsvFilePath;
        logger = LoggerFactory.getLogger("RPM Import");
        insertCount = 0;
        updateCount = 0;
    }

    public void write() throws IOException {
        logger.info("Importing from Price Zone...");
        writeToCollection(priceCollection, ITEM_NUMBER, new RPMPriceCSVFileReader(RPMPriceZoneCsvFile));
        logger.info("Importing from Store Zone...");
        writeToCollection(storeCollection, STORE_ID, new RPMStoreCSVFileReader(RPMStoreZoneCsvFilePath));
        logger.info("Importing Promotions...");
        writePromotionsToPricesCollection();
    }

    private void writeToCollection(DBCollection collection, String identifierKey, RPMCSVFileReader reader) throws IOException {
        DBObject next;
        while ((next = reader.getNext()) != null) {
            DBObject query = new BasicDBObject(identifierKey, next.get(identifierKey));
            upsert(collection, query, new BasicDBObject("$set", next));
        }
        logUpsertCounts(collection);
    }

    private void writePromotionsToPricesCollection() throws IOException {
        DBObject nextPromotion;
        RPMPromotionCSVFileReader rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader(RPMPromotionCsvFilePath);
        while ((nextPromotion = rpmPromotionCSVFileReader.getNext()) != null) {
            String itemNumber = nextPromotion.removeField(ITEM_NUMBER).toString();
            String zone = nextPromotion.removeField(ZONE_ID).toString();


            BasicDBObject existingProductQuery = new BasicDBObject(ITEM_NUMBER, itemNumber);
            existingProductQuery.put(String.format("%s.%s", ZONES, zone), new BasicDBObject("$exists", true));
            List<DBObject> existingProductResult = priceCollection.find(existingProductQuery).toArray();

            if (existingProductResult.size() > 0) {
                DBObject query = new BasicDBObject(ITEM_NUMBER, itemNumber);
                String promotionKey = String.format("%s.%s.%s", ZONES, zone, PROMOTIONS);
                BasicDBObject attributeToAddToSet = new BasicDBObject(promotionKey, nextPromotion);

                upsert(priceCollection, query, new BasicDBObject("$addToSet", attributeToAddToSet));
            } else {
                logger.warn(String.format("Item number %s with zone %s does not exist. Promotion for this product not imported", itemNumber, zone));
            }
        }
        logUpsertCounts(priceCollection);
    }

    private void upsert(DBCollection collection, DBObject existQuery, DBObject attributesToUpdate) {
        WriteResult updateResponse = collection.update(existQuery, attributesToUpdate, true, true);
        if (updateResponse.getN() > 1) {
            logger.error("Multiple documents affected by update: " + existQuery.toString());
        }
        logUpsert(existQuery, updateResponse);
    }

    private void logUpsert(DBObject product, WriteResult updateResponse) {
        if (updateResponse.getError() != null) {
            String errorMessage = String.format("error on upserting entry \"%s\": %s", product.toString(), updateResponse.toString());
            logger.error(errorMessage);
        } else if (Boolean.parseBoolean(updateResponse.getField("updatedExisting").toString())) {
            logger.debug("Updated entry: " + product.toString());
            updateCount++;
        } else {
            logger.debug("Inserted new entry: " + product.toString());
            insertCount++;
        }
    }

    private void logUpsertCounts(DBCollection collection) {
        logger.info(String.format("Inserted %s entries in %s collection", insertCount, collection.getName()));
        logger.info(String.format("Updated %s entries in %s collection", updateCount, collection.getName()));
        insertCount = 0;
        updateCount = 0;
    }
}
