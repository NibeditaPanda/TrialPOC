package com.tesco.adapters.core;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.tesco.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.adapters.rpm.readers.RPMPriceZoneCSVFileReader;
import com.tesco.adapters.rpm.readers.RPMPromotionCSVFileReader;
import com.tesco.adapters.rpm.readers.RPMPromotionDescriptionCSVFileReader;
import com.tesco.adapters.rpm.readers.RPMStoreZoneCSVFileReader;
import com.tesco.adapters.rpm.writers.RPMWriter;
import com.tesco.adapters.sonetto.SonettoPromotionWriter;
import com.tesco.adapters.sonetto.SonettoPromotionXMLReader;
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
    private String rpmPriceZoneCsvFilePath;
    private String rpmStoreZoneCsvFilePath;
    private String rpmPromotionCsvFilePath;
    private String rpmPromotionDescCSVUrl;
    private String sonettoPromotionsXMLFilePath;

    public Controller(DBCollection priceCollection, DBCollection storeCollection, DBCollection promotionCollection, String rpmPriceZoneCsvFilePath, String rpmStoreZoneCsvFilePath, String rpmPromotionCsvFilePath, String sonettoPromotionsXMLFilePath, String rpmPromotionDescCSVUrl) {
        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.promotionCollection = promotionCollection;
        this.rpmPriceZoneCsvFilePath = rpmPriceZoneCsvFilePath;
        this.rpmStoreZoneCsvFilePath = rpmStoreZoneCsvFilePath;
        this.rpmPromotionCsvFilePath = rpmPromotionCsvFilePath;
        this.sonettoPromotionsXMLFilePath = sonettoPromotionsXMLFilePath;
        this.rpmPromotionDescCSVUrl = rpmPromotionDescCSVUrl;
    }

    public static void main(String[] args) throws ConfigurationException {
        logger.info("Firing up...");

        DBCollection tempPriceCollection = DBFactory.getCollection(getTempCollectionName(PRICE_COLLECTION));
        DBCollection tempStoreCollection = DBFactory.getCollection(getTempCollectionName(STORE_COLLECTION));
        DBCollection tempPromotionCollection = DBFactory.getCollection(getTempCollectionName(PROMOTION_COLLECTION));

        Controller controller = new Controller(tempPriceCollection, tempStoreCollection,
                tempPromotionCollection, Configuration.getRPMPriceDataPath(), Configuration.getRPMStoreDataPath(),
                Configuration.getRPMPromotionDataPath(), Configuration.getSonettoPromotionsXMLDataPath(), Configuration.getRPMPromotionDescCSVUrl());


        new ControllerCoordinator().processData(controller, tempPriceCollection, tempStoreCollection, tempPromotionCollection);

    }

    public void fetchAndSavePriceDetails() throws IOException, ParserConfigurationException, SAXException, ConfigurationException, JAXBException, ColumnNotFoundException {
        indexMongo();
        logger.info("Importing data from RPM....");
        RPMPriceZoneCSVFileReader rpmPriceZoneCSVFileReader = new RPMPriceZoneCSVFileReader(rpmPriceZoneCsvFilePath);
        RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader = new RPMStoreZoneCSVFileReader(rpmStoreZoneCsvFilePath);
        RPMPromotionCSVFileReader rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader(rpmPromotionCsvFilePath);
        RPMPromotionDescriptionCSVFileReader rpmPromotionDescriptionCSVFileReader = new RPMPromotionDescriptionCSVFileReader(rpmPromotionDescCSVUrl);

        SonettoPromotionXMLReader sonettoPromotionXMLReader = new SonettoPromotionXMLReader(new SonettoPromotionWriter(promotionCollection), Configuration.getSonettoShelfImageUrl());

        new RPMWriter(priceCollection,
                storeCollection,
                promotionCollection,
                sonettoPromotionsXMLFilePath,
                rpmPriceZoneCSVFileReader, rpmStoreZoneCSVFileReader, rpmPromotionCSVFileReader, rpmPromotionDescriptionCSVFileReader, sonettoPromotionXMLReader).write();
    }


    private static String getTempCollectionName(String baseCollectionName) {
        return String.format("%s%s", baseCollectionName, new SimpleDateFormat("yyyyMMdd").format(new Date()));
    }

    private void indexMongo() {
        logger.info("Creating indexes....");
        priceCollection.ensureIndex(new BasicDBObject(ITEM_NUMBER, 1));
        storeCollection.ensureIndex(new BasicDBObject(STORE_ID, 1));
        promotionCollection.ensureIndex(new BasicDBObject(PROMOTION_OFFER_ID, 1));
    }
}
