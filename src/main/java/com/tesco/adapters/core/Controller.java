package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import com.tesco.adapters.rpm.RPMPricetWriter;

import java.io.IOException;

public class Controller {

    private DBCollection priceCollection;
    private DBCollection storeCollection;
    private String RPMPriceZoneCsvFilePath;
    private String RPMStoreZoneCsvFilePath;

    public Controller(DBCollection priceCollection, DBCollection storeCollection, String RPMPriceZoneCsvFilePath, String RPMStoreZoneCsvFilePath) {
        this.priceCollection = priceCollection;
        this.storeCollection = storeCollection;
        this.RPMPriceZoneCsvFilePath = RPMPriceZoneCsvFilePath;
        this.RPMStoreZoneCsvFilePath = RPMStoreZoneCsvFilePath;
    }

    public static void main(String[] args) {

    }

    public void fetchAndSavePriceDetails() throws IOException {
        new RPMPricetWriter(priceCollection, storeCollection, RPMPriceZoneCsvFilePath, RPMStoreZoneCsvFilePath).write();
    }
}
