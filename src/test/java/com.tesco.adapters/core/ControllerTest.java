package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.tesco.adapters.core.PriceKeys.ITEM_NUMBER;
import static com.tesco.adapters.core.PriceKeys.NATIONAL_PRICE;
import static com.tesco.adapters.core.PriceKeys.ZONE_ID;
import static org.fest.assertions.api.Assertions.assertThat;

public class ControllerTest {

    private DBCollection collection;

    @BeforeClass
    public void setUp() throws IOException {
        DBFactory.getCollection("prices").drop();
        collection = DBFactory.getCollection("prices");

        String RPMPriceCsvFilePath = "../PriceAdapters/src/test/java/com/tesco/adapters/rpm/fixtures/price.csv";
        new Controller(collection, RPMPriceCsvFilePath).fetchAndSaveBasePriceForProducts();
    }


    @Test
    public void shouldImportPriceDataFromProductPriceRMSDump() {
        DBObject productWithPrice = collection.find((DBObject) JSON.parse(String.format("{\"%s\": \"050925811\"}", ITEM_NUMBER))).toArray().get(0);

        assertThat(productWithPrice.get(ZONE_ID)).isEqualTo("5");
        assertThat(productWithPrice.get(ITEM_NUMBER)).isEqualTo("050925811");
        assertThat(productWithPrice.get(NATIONAL_PRICE)).isEqualTo("1.33");
    }

}