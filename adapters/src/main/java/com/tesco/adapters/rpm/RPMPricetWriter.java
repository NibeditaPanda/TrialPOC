package com.tesco.adapters.rpm;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.io.IOException;

import static com.tesco.adapters.core.PriceKeys.*;

public class RPMPricetWriter {
    private DBCollection priceCollection;
    private DBCollection storeCollection;
    private String RPMPriceZoneCsvFile;
    private String RPMStoreZoneCsvFilePath;
    private String RPMPromotionCsvFilePath;

    public RPMPricetWriter(DBCollection priceCollection, DBCollection storeCollection, String RPMPriceZoneCsvFile, String RPMStoreZoneCsvFilePath, String RPMPromotionCsvFilePath) {
        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.RPMPriceZoneCsvFile = RPMPriceZoneCsvFile;
        this.RPMStoreZoneCsvFilePath = RPMStoreZoneCsvFilePath;
        this.RPMPromotionCsvFilePath = RPMPromotionCsvFilePath;
    }

    public void write() throws IOException {
        writeToPricesCollection();
        writeToStoresCollection();
        writePromotionsToPricesCollection();
    }

    private void writeToStoresCollection() throws IOException {
        DBObject nextStore;
        RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader = new RPMStoreZoneCSVFileReader(RPMStoreZoneCsvFilePath);
        while ((nextStore = rpmStoreZoneCSVFileReader.getNext()) != null) {
            DBObject query = new BasicDBObject(STORE_ID, nextStore.get(STORE_ID));
            storeCollection.update(query, new BasicDBObject("$set", nextStore), true, false);
        }
    }

    private void writeToPricesCollection() throws IOException {
        DBObject nextPrice;
        RPMPriceCSVFileReader rpmPriceCSVFileReader = new RPMPriceCSVFileReader(RPMPriceZoneCsvFile);
        while ((nextPrice = rpmPriceCSVFileReader.getNext()) != null) {
            DBObject query = new BasicDBObject(ITEM_NUMBER, nextPrice.get(ITEM_NUMBER));
            priceCollection.update(query, new BasicDBObject("$set", nextPrice), true, false);
        }
    }

    private void writePromotionsToPricesCollection() throws IOException {
        DBObject nextPromotion;
        RPMPromotionCSVFileReader rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader(RPMPromotionCsvFilePath);
        while((nextPromotion = rpmPromotionCSVFileReader.getNext()) != null){
            String itemNumber = nextPromotion.removeField(ITEM_NUMBER).toString();
            String zone = nextPromotion.removeField(ZONE_ID).toString();

            DBObject query = new BasicDBObject(ITEM_NUMBER, itemNumber);
            String promotionKey = String.format("%s.%s.%s", ZONES, zone, PROMOTIONS);
            BasicDBObject attributeToAddToSet = new BasicDBObject(promotionKey, nextPromotion);
            priceCollection.update(query, new BasicDBObject("$addToSet", attributeToAddToSet), true, false);
        }
    }
}
