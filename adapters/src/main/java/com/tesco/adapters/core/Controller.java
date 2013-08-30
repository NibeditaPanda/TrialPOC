package com.tesco.adapters.core;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.tesco.adapters.rpm.RPMWriter;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tesco.adapters.core.PriceKeys.*;
import static org.slf4j.LoggerFactory.getLogger;

public class Controller {

    private static final Logger logger = getLogger("Price_Controller");

    private DBCollection priceCollection;
    private DBCollection storeCollection;
    private DBCollection promotionCollection;
    private String RPMPriceZoneCsvFilePath;
    private String RPMStoreZoneCsvFilePath;
    private String RPMPromotionCsvFilePath;

    public Controller(DBCollection priceCollection, DBCollection storeCollection, DBCollection promotionCollection, String RPMPriceZoneCsvFilePath, String RPMStoreZoneCsvFilePath, String RPMPromotionCsvFilePath) {
        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.promotionCollection = promotionCollection;
        this.RPMPriceZoneCsvFilePath = RPMPriceZoneCsvFilePath;
        this.RPMStoreZoneCsvFilePath = RPMStoreZoneCsvFilePath;
        this.RPMPromotionCsvFilePath = RPMPromotionCsvFilePath;
    }

    public static void main(String[] args) {
        logger.info("Firing up...");

        DBCollection tempPriceCollection = DBFactory.getCollection(getTempCollectionName(PRICE_COLLECTION));
        DBCollection tempStoreCollection = DBFactory.getCollection(getTempCollectionName(STORE_COLLECTION));
        DBCollection tempPromotionCollection = DBFactory.getCollection(getTempCollectionName(PROMOTION_COLLECTION));

        try {
            Controller controller = new Controller(tempPriceCollection, tempStoreCollection,
                    tempPromotionCollection, Configuration.getRPMPriceDataPath(), Configuration.getRPMStoreDataPath(),
                                            Configuration.getRPMPromotionDataPath());

            controller.fetchAndSavePriceDetails();

            logger.info("Renaming Price collection....");
            tempPriceCollection.rename(PRICE_COLLECTION, true);

            logger.info("Renaming Store collection....");
            tempStoreCollection.rename(STORE_COLLECTION, true);

            logger.info("Renaming Promotion collection....");
            tempStoreCollection.rename(PROMOTION_COLLECTION, true);

            logger.info("Successfully imported data for " + new Date());

        } catch (Exception exception) {
            logger.error("Error importing data", exception);
            throw new RuntimeException(exception);
        }

    }

    private static String getTempCollectionName(String baseCollectionName) {
        return String.format("%s%s", baseCollectionName, new SimpleDateFormat("yyyyMMdd").format(new Date()));
    }


    public void fetchAndSavePriceDetails() throws IOException {
        indexMongo();
        logger.info("Importing data from RPM....");
        new RPMWriter(priceCollection, storeCollection, promotionCollection, RPMPriceZoneCsvFilePath, RPMStoreZoneCsvFilePath, RPMPromotionCsvFilePath).write();
    }

    private void indexMongo() {
        logger.info("Creating indexes....");
        priceCollection.ensureIndex(new BasicDBObject(ITEM_NUMBER, 1));
        storeCollection.ensureIndex(new BasicDBObject(STORE_ID, 1));
    }
}
