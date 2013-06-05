package com.tesco.adapters.rpm;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.io.IOException;

public class RPMProductWriter {
    private DBCollection priceCollection;
    private String rpmPriceCsvFile;

    public RPMProductWriter(DBCollection priceCollection, String rpmPriceCsvFile) {
        this.priceCollection = priceCollection;
        this.rpmPriceCsvFile = rpmPriceCsvFile;
    }

    public void write() throws IOException {
        DBObject nextPrice ;
        RPMPriceCSVFileReader rpmPriceCSVFileReader = new RPMPriceCSVFileReader(rpmPriceCsvFile);
        while((nextPrice = rpmPriceCSVFileReader.getNext()) != null){
            priceCollection.insert(nextPrice);
        };
    }
}
