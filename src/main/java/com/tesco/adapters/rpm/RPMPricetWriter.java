package com.tesco.adapters.rpm;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

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
        DBObject nextPrice;
        RPMPriceCSVFileReader rpmPriceCSVFileReader = new RPMPriceCSVFileReader(RPMPriceZoneCsvFile);
        while ((nextPrice = rpmPriceCSVFileReader.getNext()) != null) {
            priceCollection.insert(nextPrice);
        }

        DBObject nextStore;
        RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader = new RPMStoreZoneCSVFileReader(RPMStoreZoneCsvFilePath);
        while ((nextStore = rpmStoreZoneCSVFileReader.getNext()) != null) {
            storeCollection.insert(nextStore);
        }

    }
}
