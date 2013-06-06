package com.tesco.adapters.core;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.testng.annotations.Test;

import static com.tesco.adapters.core.PriceKeys.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class RPMPriceTest extends ControllerTest {

    @Test
    public void shouldImportAllRelevantDetailsFromRPMDumps() {
        DBObject productWithPrice = priceCollection.find((DBObject) JSON.parse(String.format("{\"%s\": \"050925811\"}", ITEM_NUMBER))).toArray().get(0);

        assertThat(productWithPrice.get(ZONE_ID)).isEqualTo("5");
        assertThat(productWithPrice.get(ITEM_NUMBER)).isEqualTo("050925811");
        assertThat(productWithPrice.get(NATIONAL_PRICE)).isEqualTo("1.33");
    }

}