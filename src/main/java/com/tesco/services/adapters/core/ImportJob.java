package com.tesco.services.adapters.core;

import com.couchbase.client.CouchbaseClient;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.adapters.rpm.readers.PriceServiceCSVReader;
import com.tesco.services.adapters.rpm.readers.PriceServiceCSVReaderImpl;
import com.tesco.services.adapters.rpm.readers.RPMPriceZoneCSVFileReader;
import com.tesco.services.adapters.rpm.readers.RPMPromotionCSVFileReader;
import com.tesco.services.adapters.rpm.readers.RPMPromotionDescriptionCSVFileReader;
import com.tesco.services.adapters.rpm.readers.RPMStoreZoneCSVFileReader;
import com.tesco.services.adapters.rpm.writers.CSVHeaders;
import com.tesco.services.adapters.rpm.writers.RPMWriter;
import com.tesco.services.adapters.sonetto.SonettoPromotionWriter;
import com.tesco.services.adapters.sonetto.SonettoPromotionXMLReader;
import com.tesco.services.dao.DBFactory;
import com.tesco.services.repositories.ImportCouchbaseConnectionManager;
import com.tesco.services.repositories.ProductRepository;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.repositories.StoreRepository;
import com.tesco.services.repositories.UUIDGenerator;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import static com.tesco.services.core.PriceKeys.ITEM_NUMBER;
import static com.tesco.services.core.PriceKeys.PRICE_COLLECTION;
import static com.tesco.services.core.PriceKeys.PROMOTION_COLLECTION;
import static com.tesco.services.core.PriceKeys.PROMOTION_OFFER_ID;
import static com.tesco.services.core.PriceKeys.STORE_COLLECTION;
import static com.tesco.services.core.PriceKeys.STORE_ID;
import static org.slf4j.LoggerFactory.getLogger;

public class ImportJob implements Runnable {

    private static final Logger logger = getLogger("Price_ImportJob");

    private String rpmPriceZoneCsvFilePath;
    private String rpmStoreZoneCsvFilePath;
    private String rpmPromotionCsvFilePath;
    private String rpmPromotionDescCSVUrl;
    private String sonettoPromotionXSDDataPath;
    private String rpmPriceZoneDataPath;
    private String rpmPromoZoneDataPath;
    private String rpmPromoExtractDataPath;
    private String rpmPromoDescExtractDataPath;
    private com.tesco.services.dao.DBFactory dbFactory;
    private ImportCouchbaseConnectionManager couchbaseConnectionManager;
    private String sonettoPromotionsXMLFilePath;
    private String sonettoShelfImageUrl;

    public  ImportJob(String rpmPriceZoneCsvFilePath,
                      String rpmStoreZoneCsvFilePath,
                      String rpmPromotionCsvFilePath,
                      String sonettoPromotionsXMLFilePath,
                      String rpmPromotionDescCSVUrl,
                      String sonettoPromotionXSDDataPath,
                      String sonettoShelfImageUrl,
                      String rpmPriceZoneDataPath,
                      String rpmPromZoneDataPath,
                      String rpmPromoExtractDataPath,
                      String rpmPromoDescExtractDataPath,
                      DBFactory dbFactory,
                      ImportCouchbaseConnectionManager couchbaseConnectionManager) {
        this.rpmPriceZoneCsvFilePath = rpmPriceZoneCsvFilePath;
        this.rpmStoreZoneCsvFilePath = rpmStoreZoneCsvFilePath;
        this.rpmPromotionCsvFilePath = rpmPromotionCsvFilePath;
        this.sonettoPromotionsXMLFilePath = sonettoPromotionsXMLFilePath;
        this.rpmPromotionDescCSVUrl = rpmPromotionDescCSVUrl;
        this.sonettoPromotionXSDDataPath = sonettoPromotionXSDDataPath;
        this.sonettoShelfImageUrl = sonettoShelfImageUrl;
        this.rpmPriceZoneDataPath = rpmPriceZoneDataPath;
        this.rpmPromoZoneDataPath = rpmPromZoneDataPath;
        this.rpmPromoExtractDataPath = rpmPromoExtractDataPath;
        this.rpmPromoDescExtractDataPath = rpmPromoDescExtractDataPath;
        this.dbFactory = dbFactory;
        this.couchbaseConnectionManager = couchbaseConnectionManager;
    }

    @Override
    public void run() {
        DBCollection tempPriceCollection = dbFactory.getCollection(getTempCollectionName(PRICE_COLLECTION));
        DBCollection tempStoreCollection = dbFactory.getCollection(getTempCollectionName(STORE_COLLECTION));
        DBCollection tempPromotionCollection = dbFactory.getCollection(getTempCollectionName(PROMOTION_COLLECTION));

        processData(tempPriceCollection, tempStoreCollection, tempPromotionCollection, true);
    }

    private String getTempCollectionName(String baseCollectionName) {
        return String.format("%s%s", baseCollectionName, new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()));
    }

    void processData(DBCollection tempPriceCollection, DBCollection tempStoreCollection, DBCollection tempPromotionCollection, boolean deleteFilesOnFailure) {
        try {
            logger.info("Firing up...");

            fetchAndSavePriceDetails(tempPriceCollection, tempStoreCollection, tempPromotionCollection);

            logger.info("Renaming Price collection....");
            tempPriceCollection.rename(PRICE_COLLECTION, true);

            logger.info("Renaming Store collection....");
            tempStoreCollection.rename(STORE_COLLECTION, true);

            logger.info("Renaming Promotion collection....");
            tempPromotionCollection.rename(PROMOTION_COLLECTION, true);

            couchbaseConnectionManager.replaceCurrentWithRefresh();
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

    private void fetchAndSavePriceDetails(DBCollection priceCollection, DBCollection storeCollection, DBCollection promotionCollection) throws IOException, ParserConfigurationException, ConfigurationException, JAXBException, ColumnNotFoundException, SAXException, URISyntaxException, InterruptedException {
        indexMongo(priceCollection, storeCollection, promotionCollection);
        logger.info("Importing data from RPM....");
        RPMPriceZoneCSVFileReader rpmPriceZoneCSVFileReader = new RPMPriceZoneCSVFileReader(rpmPriceZoneCsvFilePath);
        RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader = new RPMStoreZoneCSVFileReader(rpmStoreZoneCsvFilePath);
        RPMPromotionCSVFileReader rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader(rpmPromotionCsvFilePath);
        RPMPromotionDescriptionCSVFileReader rpmPromotionDescriptionCSVFileReader = new RPMPromotionDescriptionCSVFileReader(rpmPromotionDescCSVUrl);

        SonettoPromotionXMLReader sonettoPromotionXMLReader = new SonettoPromotionXMLReader(new SonettoPromotionWriter(promotionCollection), sonettoShelfImageUrl, sonettoPromotionXSDDataPath);

        UUIDGenerator uuidGenerator = new UUIDGenerator();

        final CouchbaseClient couchbaseClient = couchbaseConnectionManager.getReplacementBucketClient();
        PromotionRepository promotionRepository = new PromotionRepository(uuidGenerator, couchbaseClient);
        ProductRepository productRepository = new ProductRepository(couchbaseClient);

        StoreRepository storeRepository = new StoreRepository(couchbaseClient);

        PriceServiceCSVReader rpmPriceReader = new PriceServiceCSVReaderImpl(rpmPriceZoneDataPath, CSVHeaders.Price.PRICE_ZONE_HEADERS);
        PriceServiceCSVReader rpmPromoPriceReader = new PriceServiceCSVReaderImpl(rpmPromoZoneDataPath, CSVHeaders.Price.PROMO_ZONE_HEADERS);
        PriceServiceCSVReader storeZoneReader = new PriceServiceCSVReaderImpl(rpmStoreZoneCsvFilePath, CSVHeaders.StoreZone.HEADERS);
        PriceServiceCSVReader rpmPromotionReader = new PriceServiceCSVReaderImpl(rpmPromoExtractDataPath, CSVHeaders.PromoExtract.HEADERS);
        PriceServiceCSVReader rpmPromotionDescReader = new PriceServiceCSVReaderImpl(rpmPromoDescExtractDataPath, CSVHeaders.PromoDescExtract.HEADERS);

        new RPMWriter(priceCollection,
                storeCollection,
                sonettoPromotionsXMLFilePath,
                rpmPriceZoneCSVFileReader,
                rpmStoreZoneCSVFileReader,
                sonettoPromotionXMLReader,
                promotionRepository,
                rpmPromotionCSVFileReader,
                rpmPromotionDescriptionCSVFileReader,
                productRepository,
                storeRepository,
                rpmPriceReader,
                rpmPromoPriceReader,
                storeZoneReader,
                rpmPromotionReader,
                rpmPromotionDescReader)
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
