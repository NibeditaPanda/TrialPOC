package com.tesco.adapters.rpm;

import com.mongodb.DBObject;
import com.tesco.adapters.core.PriceKeys;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

public class PRMStoreZoneCSVFileReaderTests {

    @Test
    public void shouldReadInStoreZoneFile() throws IOException {
        RPMStoreCSVFileReader rpmStoreZoneReader = new RPMStoreCSVFileReader("src/test/java/com/tesco/adapters/rpm/fixtures/store_zone.csv");

        DBObject aStore = rpmStoreZoneReader.getNext();

        assertThat(aStore.get(PriceKeys.PRICE_ZONE_ID)).isEqualTo("1");
        assertThat(aStore.get(PriceKeys.STORE_ID)).isEqualTo("2002");
        assertThat(aStore.get(PriceKeys.CURRENCY)).isEqualTo("GBP");
        assertThat(aStore.get(PriceKeys.PROMOTION_ZONE_ID)).isNull();

        DBObject nextStore = rpmStoreZoneReader.getNext();
        assertThat(nextStore.get(PriceKeys.PRICE_ZONE_ID)).isNull();
        assertThat(nextStore.get(PriceKeys.STORE_ID)).isEqualTo("2002");
        assertThat(nextStore.get(PriceKeys.CURRENCY)).isEqualTo("GBP");
        assertThat(nextStore.get(PriceKeys.PROMOTION_ZONE_ID)).isEqualTo("5");

    }
}
