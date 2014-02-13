package com.tesco.services.adapters.rpm.writers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.adapters.rpm.readers.*;
import com.tesco.services.adapters.rpm.readers.PriceServiceCSVReader;
import com.tesco.services.adapters.sonetto.SonettoPromotionXMLReader;
import com.tesco.services.core.Product;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.Store;
import com.tesco.services.repositories.ProductRepository;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.repositories.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.getFirst;
import static com.tesco.services.adapters.rpm.readers.RPMPriceZoneCSVFileReader.PRICE_ZONE_FORMAT;
import static com.tesco.services.core.PriceKeys.ITEM_NUMBER;
import static com.tesco.services.core.PriceKeys.PROMOTIONS;
import static com.tesco.services.core.PriceKeys.PROMOTION_END_DATE;
import static com.tesco.services.core.PriceKeys.PROMOTION_OFFER_ID;
import static com.tesco.services.core.PriceKeys.PROMOTION_OFFER_NAME;
import static com.tesco.services.core.PriceKeys.PROMOTION_START_DATE;
import static com.tesco.services.core.PriceKeys.STORE_ID;
import static com.tesco.services.core.PriceKeys.ZONES;
import static com.tesco.services.core.PriceKeys.ZONE_ID;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;

public class RPMWriter {
    private Logger logger = LoggerFactory.getLogger("RPM Import");

    private DBCollection priceCollection;
    private DBCollection storeCollection;
    private String sonettoPromotionsXMLFilePath;

    private RPMPriceZoneCSVFileReader rpmPriceZoneCSVFileReader;
    private RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader;

    private RPMPromotionCSVFileReader rpmPromotionCSVFileReader;

    private RPMPromotionDescriptionCSVFileReader rpmPromotionDescriptionCSVFileReader;
    private ProductRepository productRepository;
    private StoreRepository storeRepository;
    private PriceServiceCSVReader rpmPriceReader;
    private PriceServiceCSVReader rpmPromoPriceReader;
    private PriceServiceCSVReader storeZoneReader;
    private PriceServiceCSVReader rpmPromotionReader;

    private SonettoPromotionXMLReader sonettoPromotionXMLReader;

    private PromotionRepository promotionRepository;

    private int insertCount;
    private int updateCount;

    public RPMWriter(DBCollection priceCollection,
                     DBCollection storeCollection,
                     String sonettoPromotionsXMLFilePath,
                     RPMPriceZoneCSVFileReader rpmPriceZoneCSVFileReader,
                     RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader,
                     SonettoPromotionXMLReader sonettoPromotionXMLReader,
                     PromotionRepository promotionRepository,
                     RPMPromotionCSVFileReader rpmPromotionCSVFileReader,
                     RPMPromotionDescriptionCSVFileReader rpmPromotionDescriptionCSVFileReader,
                     ProductRepository productRepository,
                     StoreRepository storeRepository,
                     PriceServiceCSVReader rpmPriceReader,
                     PriceServiceCSVReader rpmPromoPriceReader,
                     PriceServiceCSVReader storeZoneReader,
                     PriceServiceCSVReader rpmPromotionReader) throws IOException, ColumnNotFoundException {

        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.sonettoPromotionsXMLFilePath = sonettoPromotionsXMLFilePath;
        this.rpmPriceZoneCSVFileReader = rpmPriceZoneCSVFileReader;
        this.rpmStoreZoneCSVFileReader = rpmStoreZoneCSVFileReader;
        this.sonettoPromotionXMLReader = sonettoPromotionXMLReader;
        this.promotionRepository = promotionRepository;
        this.rpmPromotionCSVFileReader = rpmPromotionCSVFileReader;
        this.rpmPromotionDescriptionCSVFileReader = rpmPromotionDescriptionCSVFileReader;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.rpmPriceReader = rpmPriceReader;
        this.rpmPromoPriceReader = rpmPromoPriceReader;
        this.storeZoneReader = storeZoneReader;
        this.rpmPromotionReader = rpmPromotionReader;

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

        // Using DataGrid
        // ===============
        logger.info("Importing price zone prices into DataGrid");
        writePriceZonePrices();
        writePromoZonePrices();
        writePromotions();
        writeStoreZones();
    }

    private void writeStoreZones() throws IOException {
        Map<String, String> storeInfoMap;
        StoreMapper storeMapper = new StoreMapper(storeRepository);

        while((storeInfoMap = storeZoneReader.getNext()) !=  null) {
            final Store store = storeMapper.map(storeInfoMap);
            storeRepository.put(store);
        }
    }

    private void writePriceZonePrices() throws IOException {
        Map<String, String> productInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((productInfoMap = rpmPriceReader.getNext()) !=  null) {
            final Product product = productMapper.mapPriceZonePrice(productInfoMap);
            productRepository.put(product);
        }
    }

    private void writePromoZonePrices() throws IOException {
        Map<String, String> productInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((productInfoMap = rpmPromoPriceReader.getNext()) !=  null) {
            final Product product = productMapper.mapPromoZonePrice(productInfoMap);
            productRepository.put(product);
        }
    }

    private void writePromotions() throws IOException {
        Map<String, String> promotionInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((promotionInfoMap = rpmPromotionReader.getNext()) !=  null) {
            final Product product = productMapper.mapPromotion(promotionInfoMap);
            productRepository.put(product);
        }

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

    private void writePromotionsDescription() throws IOException {
        Promotion next;

        while ((next = rpmPromotionDescriptionCSVFileReader.getNextDG()) != null) {
            List<Promotion> promotions = this.promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber(next.getOfferId(), next.getItemNumber(), next.getZoneId());
            // TODO: Need to talk to Shiraz if this is ok.

            Promotion promotion = getFirst(promotions, null);
            if (promotion != null) {
                promotion.setCFDescription1(next.getCFDescription1());
                promotion.setCFDescription2(next.getCFDescription2());
                this.promotionRepository.updatePromotion(promotion.getUniqueKey(), promotion);
            }
        }
    }

    private void updatePromotionsWithShelfTalker() throws ParserConfigurationException, IOException, JAXBException, SAXException {
        sonettoPromotionXMLReader.handle(sonettoPromotionsXMLFilePath);
    }

    private void writePromotionsToPricesCollection() throws IOException, ColumnNotFoundException {
        Promotion nextPromotionDG;

        while ((nextPromotionDG = rpmPromotionCSVFileReader.getNextDG()) != null) {
            addPromotion(nextPromotionDG);

            /**
             * this is just to satisfy MongoDB. It will be deleted once JDG is in place.
             */
            BasicDBObject promotion = new BasicDBObject();
            promotion.put(ITEM_NUMBER, nextPromotionDG.getItemNumber());
            promotion.put(ZONE_ID, nextPromotionDG.getZoneId());
            promotion.put(PROMOTION_OFFER_ID, nextPromotionDG.getOfferId());
            promotion.put(PROMOTION_OFFER_NAME, nextPromotionDG.getOfferName());
            promotion.put(PROMOTION_START_DATE, nextPromotionDG.getStartDate());
            promotion.put(PROMOTION_END_DATE, nextPromotionDG.getEndDate());

            appendPromotionToPrice(promotion);
        }

        logUpsertCounts(priceCollection);
    }

    private void addPromotion(Promotion nextPromotion) {
        this.promotionRepository.addPromotion(nextPromotion);
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
