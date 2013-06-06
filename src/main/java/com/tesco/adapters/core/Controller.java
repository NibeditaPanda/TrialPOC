package com.tesco.adapters.core;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.tesco.adapters.rpm.RPMPricetWriter;
import org.slf4j.Logger;

import java.io.IOException;

import static com.tesco.adapters.core.PriceKeys.ITEM_NUMBER;
import static com.tesco.adapters.core.PriceKeys.STORE_ID;
import static org.slf4j.LoggerFactory.getLogger;

public class Controller {

    private DBCollection priceCollection;
    private DBCollection storeCollection;
    private String RPMPriceZoneCsvFilePath;
    private String RPMStoreZoneCsvFilePath;
    private static final Logger logger = getLogger("Price_Controller");

    public Controller(DBCollection priceCollection, DBCollection storeCollection, String RPMPriceZoneCsvFilePath, String RPMStoreZoneCsvFilePath) {
        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.RPMPriceZoneCsvFilePath = RPMPriceZoneCsvFilePath;
        this.RPMStoreZoneCsvFilePath = RPMStoreZoneCsvFilePath;
    }

    public static void main(String[] args) {
        logger.info("Firing up...");
        DBCollection priceCollection = DBFactory.getCollection("price");
        DBCollection storeCollection = DBFactory.getCollection("store");

        try {
            String RPMPriceZoneCsvFilePath = Configuration.get().getString("rpm.price.data.dump");
            String RPMStoreZoneCSVFilePath = Configuration.get().getString("rpm.store.data.dump");

            Controller controller = new Controller(priceCollection, storeCollection, RPMPriceZoneCsvFilePath, RPMStoreZoneCSVFilePath);
            controller.fetchAndSavePriceDetails();
        } catch (Exception exception) {
            logger.error("Error importing data", exception);
            throw new RuntimeException(exception);
        }
    }

    public void fetchAndSavePriceDetails() throws IOException {
        indexMongo();
        new RPMPricetWriter(priceCollection, storeCollection, RPMPriceZoneCsvFilePath, RPMStoreZoneCsvFilePath).write();
    }

    private void indexMongo() {
        priceCollection.ensureIndex(new BasicDBObject(ITEM_NUMBER, 1));
        storeCollection.ensureIndex(new BasicDBObject(STORE_ID, 1));

    }
}
