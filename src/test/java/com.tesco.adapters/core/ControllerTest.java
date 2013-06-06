package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import org.testng.annotations.BeforeClass;

import java.io.IOException;

import static com.tesco.adapters.core.PriceKeys.*;

public class ControllerTest {

    protected DBCollection priceCollection;
    protected DBCollection storeCollection;

    @BeforeClass
    public void setUp() throws IOException {
        DBFactory.getCollection(PRICE_COLLECTION).drop();
        priceCollection = DBFactory.getCollection(PRICE_COLLECTION);

        DBFactory.getCollection(STORE_COLLECTION).drop();
        storeCollection = DBFactory.getCollection(STORE_COLLECTION);

        String RPMPriceZoneCsvFilePath = "../PriceAdapters/src/test/java/com/tesco/adapters/rpm/fixtures/price_zone.csv";
        String RPMStoreZoneCsvFilePath = "../PriceAdapters/src/test/java/com/tesco/adapters/rpm/fixtures/store_zone.csv";
        new Controller(priceCollection, storeCollection, RPMPriceZoneCsvFilePath, RPMStoreZoneCsvFilePath).fetchAndSavePriceDetails();
    }

}
