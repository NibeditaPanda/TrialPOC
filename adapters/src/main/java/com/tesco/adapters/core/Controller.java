package com.tesco.adapters.core;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.tesco.adapters.rpm.RPMWriter;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
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
    private String sonettoPromotionsXMLFilePath;
    private String RPMPromotionDescCSVUrl;

    public Controller(DBCollection priceCollection, DBCollection storeCollection, DBCollection promotionCollection, String RPMPriceZoneCsvFilePath, String RPMStoreZoneCsvFilePath, String RPMPromotionCsvFilePath, String sonettoPromotionsXMLFilePath, String rpmPromotionDescCSVUrl) {
        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.promotionCollection = promotionCollection;
        this.RPMPriceZoneCsvFilePath = RPMPriceZoneCsvFilePath;
        this.RPMStoreZoneCsvFilePath = RPMStoreZoneCsvFilePath;
        this.RPMPromotionCsvFilePath = RPMPromotionCsvFilePath;
        this.sonettoPromotionsXMLFilePath = sonettoPromotionsXMLFilePath;
        this.RPMPromotionDescCSVUrl = rpmPromotionDescCSVUrl;
    }

    public static void main(String[] args) {
        logger.info("Firing up...");

        DBCollection tempPriceCollection = DBFactory.getCollection(getTempCollectionName(PRICE_COLLECTION));
        DBCollection tempStoreCollection = DBFactory.getCollection(getTempCollectionName(STORE_COLLECTION));
        DBCollection tempPromotionCollection = DBFactory.getCollection(getTempCollectionName(PROMOTION_COLLECTION));

        try {
            Controller controller = new Controller(tempPriceCollection, tempStoreCollection,
                    tempPromotionCollection, Configuration.getRPMPriceDataPath(), Configuration.getRPMStoreDataPath(),
                                            Configuration.getRPMPromotionDataPath(), Configuration.getSonettoPromotionsXMLDataPath(), Configuration.getRPMPromotionDescCSVUrl());

            controller.fetchAndSavePriceDetails();

            logger.info("Renaming Price collection....");
            tempPriceCollection.rename(PRICE_COLLECTION, true);

            logger.info("Renaming Store collection....");
            tempStoreCollection.rename(STORE_COLLECTION, true);

            logger.info("Renaming Promotion collection....");
            tempPromotionCollection.rename(PROMOTION_COLLECTION, true);

            logger.info("Successfully imported data for " + new Date());

        } catch (Exception exception) {
            logger.error("Error importing data", exception);
            throw new RuntimeException(exception);
        }

    }

    private static String getTempCollectionName(String baseCollectionName) {
        return String.format("%s%s", baseCollectionName, new SimpleDateFormat("yyyyMMdd").format(new Date()));
    }


    public void fetchAndSavePriceDetails() throws IOException, ParserConfigurationException, SAXException, ConfigurationException, JAXBException {
        indexMongo();
        logger.info("Importing data from RPM....");
        new RPMWriter(priceCollection, storeCollection, promotionCollection, RPMPriceZoneCsvFilePath, RPMStoreZoneCsvFilePath, RPMPromotionCsvFilePath, sonettoPromotionsXMLFilePath, Configuration.getSonettoShelfImageUrl(), RPMPromotionDescCSVUrl).write();
    }

    private void indexMongo() {
        logger.info("Creating indexes....");
        priceCollection.ensureIndex(new BasicDBObject(ITEM_NUMBER, 1));
        storeCollection.ensureIndex(new BasicDBObject(STORE_ID, 1));
    }
}
