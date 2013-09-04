package com.tesco.adapters.rpm;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.tesco.adapters.sonetto.SonettoPromotionHandler;
import com.tesco.adapters.sonetto.SonettoPromotionWriter;
import com.tesco.adapters.sonetto.SonettoPromotionXMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;

public class RPMWriter {
    private DBCollection promotionCollection;
    private DBCollection priceCollection;
    private DBCollection storeCollection;
    private String RPMPriceZoneCsvFile;
    private String RPMStoreZoneCsvFilePath;
    private String RPMPromotionCsvFilePath;
    private String sonettoPromotionsXMLFilePath;
    private String sonettoShelfImageUrl;
    private String RPMPromotionDescCSVUrl;
    private Logger logger;
    private int insertCount;
    private int updateCount;

    public RPMWriter(DBCollection priceCollection, DBCollection storeCollection, DBCollection promotionCollection, String RPMPriceZoneCsvFile, String RPMStoreZoneCsvFilePath, String RPMPromotionCsvFilePath, String sonettoPromotionsXMLFilePath, String sonettoShelfImageUrl, String RPMPromotionDescCSVUrl) {
        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.promotionCollection = promotionCollection;
        this.RPMPriceZoneCsvFile = RPMPriceZoneCsvFile;
        this.RPMStoreZoneCsvFilePath = RPMStoreZoneCsvFilePath;
        this.RPMPromotionCsvFilePath = RPMPromotionCsvFilePath;
        this.sonettoPromotionsXMLFilePath = sonettoPromotionsXMLFilePath;
        this.sonettoShelfImageUrl = sonettoShelfImageUrl;
        this.RPMPromotionDescCSVUrl = RPMPromotionDescCSVUrl;
        logger = LoggerFactory.getLogger("RPM Import");
        insertCount = 0;
        updateCount = 0;
    }

    public void write() throws IOException, ParserConfigurationException, SAXException {
        logger.info("Importing from Price Zone...");
        writeToCollection(priceCollection, ITEM_NUMBER, new RPMPriceCSVFileReader(RPMPriceZoneCsvFile));
        logger.info("Importing from Store Zone...");
        writeToCollection(storeCollection, STORE_ID, new RPMStoreCSVFileReader(RPMStoreZoneCsvFilePath));
        logger.info("Importing Promotions...");
        writePromotionsToPricesCollection();
        logger.info("Update Promotions with CF Descriptions...");
        writePromotionsDescription();
        logger.info("Update Promotions with Shelf Talker Image...");
        updatePromotionsWithShelfTalker();
    }

    private void writePromotionsDescription() throws IOException {


        RPMPromotionDescReader reader = new RPMPromotionDescReader(RPMPromotionDescCSVUrl);
        DBObject next;
        while ((next = reader.getNext()) != null) {
            BasicDBObject key = new BasicDBObject();
            key.put(PROMOTION_OFFER_ID, next.get(PROMOTION_OFFER_ID));
            key.put(ZONE_ID, next.get(ZONE_ID));

            BasicDBObject values = new BasicDBObject();
            values.put(PROMOTION_CF_DESCRIPTION_1, next.get(PROMOTION_CF_DESCRIPTION_1));
            values.put(PROMOTION_CF_DESCRIPTION_2, next.get(PROMOTION_CF_DESCRIPTION_2));

            DBObject query = new BasicDBObject(key);
            upsert(promotionCollection, query, new BasicDBObject("$set", values));
        }
        logUpsertCounts(promotionCollection);
    }

    private void updatePromotionsWithShelfTalker() throws ParserConfigurationException, SAXException, IOException {
        SonettoPromotionXMLReader reader = new SonettoPromotionXMLReader(this.sonettoPromotionsXMLFilePath, new SonettoPromotionHandler(new SonettoPromotionWriter(promotionCollection), sonettoShelfImageUrl));

        reader.read();
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
            AddPromotion(nextPromotion);
            AppendPromotionToPrice(nextPromotion);
        }
        logUpsertCounts(priceCollection);
    }

    private void AddPromotion(DBObject nextPromotion) {
        promotionCollection.insert(nextPromotion);
    }

    private void AppendPromotionToPrice(DBObject nextPromotion) {
        String itemNumber = nextPromotion.removeField(ITEM_NUMBER).toString();
        String zone = nextPromotion.removeField(ZONE_ID).toString();
        nextPromotion.removeField("_id");

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
