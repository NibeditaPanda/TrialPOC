package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class ControllerIntegrationTest {

    protected DBCollection priceCollection;
    protected DBCollection storeCollection;
    private String rpmPriceZoneCsvFilePath;
    private String rpmStoreZoneCsvFilePath;

    @BeforeMethod
    public void setUp() throws IOException {
        DBFactory.getCollection(PRICE_COLLECTION).drop();
        priceCollection = DBFactory.getCollection(PRICE_COLLECTION);

        DBFactory.getCollection(STORE_COLLECTION).drop();
        storeCollection = DBFactory.getCollection(STORE_COLLECTION);

        rpmPriceZoneCsvFilePath = "../PriceAdapters/src/test/java/com/tesco/adapters/rpm/fixtures/price_zone.csv";
        rpmStoreZoneCsvFilePath = "../PriceAdapters/src/test/java/com/tesco/adapters/rpm/fixtures/store_zone.csv";
        new Controller(priceCollection, storeCollection, rpmPriceZoneCsvFilePath, rpmStoreZoneCsvFilePath).fetchAndSavePriceDetails();
    }

    @Test
    public void shouldImportAllPricesFromRPMPriceDump() throws IOException {
        List<DBObject> prices = priceCollection.find((DBObject) JSON.parse(String.format("{\"%s\": \"050925811\"}", ITEM_NUMBER))).toArray();

        DBObject productWithPrice = prices.get(0);

        assertThat(prices.size()).isEqualTo(1);
        assertThat(productWithPrice.get(ZONE_ID)).isEqualTo("5");
        assertThat(productWithPrice.get(ITEM_NUMBER)).isEqualTo("050925811");
        assertThat(productWithPrice.get(NATIONAL_PRICE)).isEqualTo("1.33");

    }

    @Test
    public void shouldImportAndUpdateAllPricesFromRPMPriceDump() throws IOException {
        String filePath = "../PriceAdapters/src/test/java/com/tesco/adapters/rpm/fixtures/price_zone_to_update.csv";
        new Controller(priceCollection, storeCollection, filePath, rpmStoreZoneCsvFilePath).fetchAndSavePriceDetails();

        List<DBObject> prices = priceCollection.find((DBObject) JSON.parse(String.format("{\"%s\": \"050925811\"}", ITEM_NUMBER))).toArray();
        DBObject productWithPrice = prices.get(0);

        assertThat(prices.size()).isEqualTo(1);
        assertThat(productWithPrice.get(ZONE_ID)).isEqualTo("5");
        assertThat(productWithPrice.get(ITEM_NUMBER)).isEqualTo("050925811");
        assertThat(productWithPrice.get(NATIONAL_PRICE)).isEqualTo("2.33");
    }

    @Test
    public void shouldImportStoreAndZoneMapping() {
        List<DBObject> stores = storeCollection.find((DBObject) JSON.parse(String.format("{\"%s\": \"2002\"}", STORE_ID))).toArray();
        DBObject productWithPrice = stores.get(0);

        assertThat(stores.size()).isEqualTo(1);
        assertThat(productWithPrice.get(ZONE_ID)).isEqualTo("5");
        assertThat(productWithPrice.get(STORE_ID)).isEqualTo("2002");
    }

    @Test
    public void shouldImportAndUpdateStoreAndZoneMapping() throws IOException {
        String filePath = "../PriceAdapters/src/test/java/com/tesco/adapters/rpm/fixtures/store_zone_to_update.csv";
        new Controller(priceCollection, storeCollection, rpmPriceZoneCsvFilePath, filePath).fetchAndSavePriceDetails();

        List<DBObject> stores = storeCollection.find((DBObject) JSON.parse(String.format("{\"%s\": \"2002\"}", STORE_ID))).toArray();
        DBObject productWithPrice = stores.get(0);

        assertThat(stores.size()).isEqualTo(1);
        assertThat(productWithPrice.get(ZONE_ID)).isEqualTo("7");
        assertThat(productWithPrice.get(STORE_ID)).isEqualTo("2002");
    }


}
