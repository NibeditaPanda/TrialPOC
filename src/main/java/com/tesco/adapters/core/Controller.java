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
import com.tesco.core.UUIDGenerator;
import com.tesco.services.Promotion;
import com.tesco.services.repositories.PromotionRepository;
import org.apache.commons.configuration.ConfigurationException;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static com.tesco.core.PriceKeys.*;
import static org.slf4j.LoggerFactory.getLogger;

public class Controller {

    private static final Logger logger = getLogger("Price_Controller");

    private String rpmPriceZoneCsvFilePath;
    private String rpmStoreZoneCsvFilePath;
    private String rpmPromotionCsvFilePath;
    private String rpmPromotionDescCSVUrl;
    private String sonettoPromotionXSDDataPath;
    private Cache<String, Promotion> promotionCache;
    private String sonettoPromotionsXMLFilePath;
    private String sonettoShelfImageUrl;

    public Controller(String rpmPriceZoneCsvFilePath,
                      String rpmStoreZoneCsvFilePath,
                      String rpmPromotionCsvFilePath,
                      String sonettoPromotionsXMLFilePath,
                      String rpmPromotionDescCSVUrl,
                      String sonettoPromotionXSDDataPath,
                      String sonettoShelfImageUrl,
                      Cache<String, Promotion> promotionCache) {
        this.rpmPriceZoneCsvFilePath = rpmPriceZoneCsvFilePath;
        this.rpmStoreZoneCsvFilePath = rpmStoreZoneCsvFilePath;
        this.rpmPromotionCsvFilePath = rpmPromotionCsvFilePath;
        this.sonettoPromotionsXMLFilePath = sonettoPromotionsXMLFilePath;
        this.rpmPromotionDescCSVUrl = rpmPromotionDescCSVUrl;
        this.sonettoPromotionXSDDataPath = sonettoPromotionXSDDataPath;
        this.sonettoShelfImageUrl = sonettoShelfImageUrl;
        this.promotionCache = promotionCache;
    }

    public void processData(DBCollection tempPriceCollection, DBCollection tempStoreCollection, DBCollection tempPromotionCollection, boolean deleteFilesOnFailure) {
        try {
            logger.info("Firing up...");

            fetchAndSavePriceDetails(tempPriceCollection, tempStoreCollection, tempPromotionCollection);

            logger.info("Renaming Price collection....");
            tempPriceCollection.rename(PRICE_COLLECTION, true);

            logger.info("Renaming Store collection....");
            tempStoreCollection.rename(STORE_COLLECTION, true);

            logger.info("Renaming Promotion collection....");
            tempPromotionCollection.rename(PROMOTION_COLLECTION, true);

            logger.info("Successfully imported data for " + new Date());

        } catch (Exception exception) {
            logger.error("Error importing data", exception);

            if (deleteFilesOnFailure) {
                deleteRpmPriceZoneCsvFilePath();
                deleteRpmStoreZoneCsvFilePath();
                deleteRpmPromotionDescCSVUrl();
                deleteRpmPromotionCsvFilePath();
                deleteSonettoPromotionsXMLFilePath();
            }
        }
    }

    private void fetchAndSavePriceDetails(DBCollection priceCollection, DBCollection storeCollection, DBCollection promotionCollection) throws IOException, ParserConfigurationException, ConfigurationException, JAXBException, ColumnNotFoundException, SAXException {
        indexMongo(priceCollection, storeCollection, promotionCollection);
        logger.info("Importing data from RPM....");
        RPMPriceZoneCSVFileReader rpmPriceZoneCSVFileReader = new RPMPriceZoneCSVFileReader(rpmPriceZoneCsvFilePath);
        RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader = new RPMStoreZoneCSVFileReader(rpmStoreZoneCsvFilePath);
        RPMPromotionCSVFileReader rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader(rpmPromotionCsvFilePath);
        RPMPromotionDescriptionCSVFileReader rpmPromotionDescriptionCSVFileReader = new RPMPromotionDescriptionCSVFileReader(rpmPromotionDescCSVUrl);

        SonettoPromotionXMLReader sonettoPromotionXMLReader = new SonettoPromotionXMLReader(new SonettoPromotionWriter(promotionCollection), sonettoShelfImageUrl, sonettoPromotionXSDDataPath);

        UUIDGenerator uuidGenerator = new UUIDGenerator();
        PromotionRepository promotionRepository = new PromotionRepository(uuidGenerator, promotionCache);

        new RPMWriter(priceCollection,
                storeCollection,
                sonettoPromotionsXMLFilePath,
                rpmPriceZoneCSVFileReader,
                rpmStoreZoneCSVFileReader,
                sonettoPromotionXMLReader,
                promotionRepository,
                rpmPromotionCSVFileReader,
                rpmPromotionDescriptionCSVFileReader
        )
                .write();
    }

    private void indexMongo(DBCollection priceCollection, DBCollection storeCollection, DBCollection promotionCollection) {
        logger.info("Creating indexes....");
        priceCollection.ensureIndex(new BasicDBObject(ITEM_NUMBER, 1));
        storeCollection.ensureIndex(new BasicDBObject(STORE_ID, 1));
        promotionCollection.ensureIndex(new BasicDBObject(PROMOTION_OFFER_ID, 1));
    }

    public void deleteRpmPriceZoneCsvFilePath() {
        deleteFile(rpmPriceZoneCsvFilePath);
    }

    public void deleteRpmStoreZoneCsvFilePath() {
        deleteFile(rpmStoreZoneCsvFilePath);
    }

    public void deleteRpmPromotionCsvFilePath() {
        deleteFile(rpmPromotionCsvFilePath);
    }

    public void deleteRpmPromotionDescCSVUrl() {
        deleteFile(rpmPromotionDescCSVUrl);
    }

    public void deleteSonettoPromotionsXMLFilePath() {
        deleteFile(sonettoPromotionsXMLFilePath);
    }

    private boolean deleteFile(String file) {
        return new File(file).delete();
    }
}
