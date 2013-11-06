package com.tesco.adapters.rpm.writers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.tesco.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.adapters.rpm.readers.*;
import com.tesco.adapters.sonetto.SonettoPromotionXMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

import static com.tesco.core.PriceKeys.*;
import static com.tesco.adapters.rpm.readers.RPMPriceZoneCSVFileReader.PRICE_ZONE_FORMAT;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;

public class RPMWriter {
    private Logger logger = LoggerFactory.getLogger("RPM Import");

    private DBCollection promotionCollection;
    private DBCollection priceCollection;
    private DBCollection storeCollection;
    private String sonettoPromotionsXMLFilePath;

    private RPMPriceZoneCSVFileReader rpmPriceZoneCSVFileReader;
    private RPMPromotionCSVFileReader rpmPromotionCSVFileReader;
    private RPMPromotionDescriptionCSVFileReader rpmPromotionDescriptionCSVFileReader;
    private RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader;

    private SonettoPromotionXMLReader sonettoPromotionXMLReader;

    private int insertCount;
    private int updateCount;

    public RPMWriter(DBCollection priceCollection,
                     DBCollection storeCollection,
                     DBCollection promotionCollection,
                     String sonettoPromotionsXMLFilePath,
                     RPMPriceZoneCSVFileReader rpmPriceZoneCSVFileReader,
                     RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader,
                     RPMPromotionCSVFileReader rpmPromotionCSVFileReader,
                     RPMPromotionDescriptionCSVFileReader rpmPromotionDescriptionCSVFileReader,
                     SonettoPromotionXMLReader sonettoPromotionXMLReader) throws IOException, ColumnNotFoundException {

        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.promotionCollection = promotionCollection;
        this.sonettoPromotionsXMLFilePath = sonettoPromotionsXMLFilePath;
        this.rpmPriceZoneCSVFileReader = rpmPriceZoneCSVFileReader;
        this.rpmStoreZoneCSVFileReader = rpmStoreZoneCSVFileReader;
        this.rpmPromotionCSVFileReader = rpmPromotionCSVFileReader;
        this.rpmPromotionDescriptionCSVFileReader = rpmPromotionDescriptionCSVFileReader;
        this.sonettoPromotionXMLReader = sonettoPromotionXMLReader;

        insertCount = 0;
        updateCount = 0;
    }

    public void write() throws IOException, ParserConfigurationException, JAXBException, ColumnNotFoundException, SAXException {
        logger.info("Importing from Price Zone...");
        writeToCollection(priceCollection, ITEM_NUMBER, rpmPriceZoneCSVFileReader);
        logger.info("Importing from Store Zone...");
        writeToCollection(storeCollection, STORE_ID, rpmStoreZoneCSVFileReader);
        logger.info("Importing Promotions...");
        writePromotionsToPricesCollection();
        logger.info("Update Promotions with CF Descriptions...");
        writePromotionsDescription();
        logger.info("Update Promotions with Shelf Talker Image...");
        updatePromotionsWithShelfTalker();
    }

    private void writeToCollection(DBCollection collection, String identifierKey, RPMCSVFileReader reader) throws IOException {
        DBObject next;
        while ((next = reader.getNext()) != null) {
            DBObject existedDbObject = new BasicDBObject(identifierKey, next.get(identifierKey));
            upsert(collection, existedDbObject, new BasicDBObject("$set", next));
        }
        logUpsertCounts(collection);
    }

    private void upsert(DBCollection collection, DBObject existedDbObject, DBObject dbObjectToUpdate) {
        WriteResult updateResponse = collection.update(existedDbObject, dbObjectToUpdate, true, true);
        logUpsert(existedDbObject, updateResponse);
    }

    private void writePromotionsDescription() throws IOException, ColumnNotFoundException {

        DBObject next;
        while ((next = rpmPromotionDescriptionCSVFileReader.getNext()) != null) {
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

    private void updatePromotionsWithShelfTalker() throws ParserConfigurationException, IOException, JAXBException, SAXException {
        sonettoPromotionXMLReader.handle(sonettoPromotionsXMLFilePath);
    }

    private void writePromotionsToPricesCollection() throws IOException, ColumnNotFoundException {
        DBObject nextPromotion;

        while ((nextPromotion = rpmPromotionCSVFileReader.getNext()) != null) {
            addPromotion(nextPromotion);
            appendPromotionToPrice(nextPromotion);
        }
        logUpsertCounts(priceCollection);
    }

    private void addPromotion(DBObject nextPromotion) {
        promotionCollection.insert(nextPromotion);
    }

    private void appendPromotionToPrice(DBObject nextPromotion) {
        String itemNumber = nextPromotion.removeField(ITEM_NUMBER).toString();
        String zone = nextPromotion.removeField(ZONE_ID).toString();
        nextPromotion.removeField("_id");

        BasicDBObject existingProductQuery = new BasicDBObject(ITEM_NUMBER, itemNumber);
        existingProductQuery.put(format("%s.%s", ZONES, zone), new BasicDBObject("$exists", true));
        List<DBObject> existingProductResult = priceCollection.find(existingProductQuery).toArray();

        if (existingProductResult.size() > 0) {
            DBObject query = new BasicDBObject(ITEM_NUMBER, itemNumber);
            String promotionKey = format(PRICE_ZONE_FORMAT, ZONES, zone, PROMOTIONS);
            BasicDBObject attributeToAddToSet = new BasicDBObject(promotionKey, nextPromotion);

            upsert(priceCollection, query, new BasicDBObject("$addToSet", attributeToAddToSet));
        } else {
            logger.warn(format("Item number %s with zone %s does not exist. Promotion for this product not imported", itemNumber, zone));
        }
    }

    private void logUpsert(DBObject product, WriteResult updateResponse) {
        if (updateResponse.getError() != null) {
            String errorMessage = format("error on upserting entry \"%s\": %s", product.toString(), updateResponse.toString());
            logger.error(errorMessage);
        } else if (parseBoolean(updateResponse.getField("updatedExisting").toString())) {
            logger.debug("Updated entry: " + product.toString());
            updateCount++;
        } else {
            logger.debug("Inserted new entry: " + product.toString());
            insertCount++;
        }
    }

    private void logUpsertCounts(DBCollection collection) {
        logger.info(format("Inserted %s entries in %s collection", insertCount, collection.getName()));
        logger.info(format("Updated %s entries in %s collection", updateCount, collection.getName()));
        insertCount = 0;
        updateCount = 0;
    }
}
