package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import com.tesco.adapters.rpm.RPMProductWriter;

import java.io.IOException;

public class Controller {

    private DBCollection priceCollection;
    private String rpmPriceCsvFilePath;

    public Controller(DBCollection priceCollection, String rpmPriceCsvFilePath) {
        this.priceCollection = priceCollection;
        this.rpmPriceCsvFilePath = rpmPriceCsvFilePath;
    }

    public static void main(String[] args) {

    }

    public void fetchAndSaveBasePriceForProducts() throws IOException {
        new RPMProductWriter(priceCollection, rpmPriceCsvFilePath).write();

    }
}
