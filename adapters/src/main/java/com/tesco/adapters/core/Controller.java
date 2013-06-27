package com.tesco.adapters.core;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.tesco.adapters.rpm.RPMWriter;
import org.slf4j.Logger;

import java.io.IOException;

import static com.tesco.adapters.core.PriceKeys.*;
import static org.slf4j.LoggerFactory.getLogger;

public class Controller {

    private DBCollection priceCollection;
    private DBCollection storeCollection;
    private String RPMPriceZoneCsvFilePath;
    private String RPMStoreZoneCsvFilePath;
    private String RPMPromotionCsvFilePath;
    private static final Logger logger = getLogger("Price_Controller");


    public Controller(DBCollection priceCollection, DBCollection storeCollection, String RPMPriceZoneCsvFilePath, String RPMStoreZoneCsvFilePath, String RPMPromotionCsvFilePath) {
        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.RPMPriceZoneCsvFilePath = RPMPriceZoneCsvFilePath;
        this.RPMStoreZoneCsvFilePath = RPMStoreZoneCsvFilePath;
        this.RPMPromotionCsvFilePath = RPMPromotionCsvFilePath;
    }

    public static void main(String[] args) {
        logger.info("Firing up...");
        DBCollection priceCollection = DBFactory.getCollection(PRICE_COLLECTION);
        DBCollection storeCollection = DBFactory.getCollection(STORE_COLLECTION);

        try {
            String RPMPriceZoneCsvFilePath = Configuration.get().getString("rpm.price.data.dump");
            String RPMStoreZoneCSVFilePath = Configuration.get().getString("rpm.store.data.dump");
            String RPMPromotionCsvFilePath = Configuration.get().getString("rpm.promotion.data.dump");

            Controller controller = new Controller(priceCollection, storeCollection, RPMPriceZoneCsvFilePath, RPMStoreZoneCSVFilePath, RPMPromotionCsvFilePath);
            controller.fetchAndSavePriceDetails();
        } catch (Exception exception) {
            logger.error("Error importing data", exception);
            throw new RuntimeException(exception);
        }
    }

    public void fetchAndSavePriceDetails() throws IOException {
        indexMongo();
        new RPMWriter(priceCollection, storeCollection, RPMPriceZoneCsvFilePath, RPMStoreZoneCsvFilePath, RPMPromotionCsvFilePath).write();
    }

    private void indexMongo() {
        priceCollection.ensureIndex(new BasicDBObject(ITEM_NUMBER, 1));
        storeCollection.ensureIndex(new BasicDBObject(STORE_ID, 1));

    }
}
