package com.tesco.adapters.rpm;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tesco.adapters.core.PriceKeys;

import java.io.IOException;

public class RPMPricetWriter {
    private DBCollection priceCollection;
    private DBCollection storeCollection;
    private String RPMPriceZoneCsvFile;
    private String RPMStoreZoneCsvFilePath;

    public RPMPricetWriter(DBCollection priceCollection, DBCollection storeCollection, String RPMPriceZoneCsvFile, String RPMStoreZoneCsvFilePath) {
        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.RPMPriceZoneCsvFile = RPMPriceZoneCsvFile;
        this.RPMStoreZoneCsvFilePath = RPMStoreZoneCsvFilePath;
    }

    public void write() throws IOException {
        writeToPricesCollection();
        writeToStoresCollection();

    }

    private void writeToStoresCollection() throws IOException {
        DBObject nextStore;
        RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader = new RPMStoreZoneCSVFileReader(RPMStoreZoneCsvFilePath);
        while ((nextStore = rpmStoreZoneCSVFileReader.getNext()) != null) {
            DBObject query = new BasicDBObject(PriceKeys.STORE_ID, nextStore.get(PriceKeys.STORE_ID));
            storeCollection.update(query, new BasicDBObject("$set", nextStore), true, false);
        }
    }

    private void writeToPricesCollection() throws IOException {
        DBObject nextPrice;
        RPMPriceCSVFileReader rpmPriceCSVFileReader = new RPMPriceCSVFileReader(RPMPriceZoneCsvFile);
        while ((nextPrice = rpmPriceCSVFileReader.getNext()) != null) {
            DBObject query = new BasicDBObject(PriceKeys.ITEM_NUMBER, nextPrice.get(PriceKeys.ITEM_NUMBER));
            priceCollection.update(query, new BasicDBObject("$set", nextPrice), true, false);
        }
    }
}
