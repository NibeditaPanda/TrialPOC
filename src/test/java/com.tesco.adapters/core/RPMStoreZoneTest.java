package com.tesco.adapters.core;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.testng.annotations.Test;

import static com.tesco.adapters.core.PriceKeys.STORE_ID;
import static com.tesco.adapters.core.PriceKeys.ZONE_ID;
import static org.fest.assertions.api.Assertions.assertThat;

public class RPMStoreZoneTest extends ControllerTest {

    @Test
    public void shouldImportStoreAndZoneMapping() {
        DBObject productWithPrice = storeCollection.find((DBObject) JSON.parse(String.format("{\"%s\": \"2002\"}", STORE_ID))).toArray().get(0);

        assertThat(productWithPrice.get(ZONE_ID)).isEqualTo("5");
        assertThat(productWithPrice.get(STORE_ID)).isEqualTo("2002");
    }
}
