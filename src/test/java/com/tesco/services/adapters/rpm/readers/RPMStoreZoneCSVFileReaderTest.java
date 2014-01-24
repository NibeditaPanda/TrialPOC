package com.tesco.services.adapters.rpm.readers;

import com.mongodb.DBObject;
import com.tesco.services.core.PriceKeys;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

public class RPMStoreZoneCSVFileReaderTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private RPMStoreZoneCSVFileReader rpmStoreZoneReader;

    @Test
    public void shouldReadInStoreZoneFile() throws IOException, ColumnNotFoundException {
        rpmStoreZoneReader = new RPMStoreZoneCSVFileReader("src/test/resources/com/tesco/services/adapters/rpm/fixtures/store_zone.csv");

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

    @Test
    public void shouldThrowExceptionGivenStoreNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("STORE is not found");

        rpmStoreZoneReader = new RPMStoreZoneCSVFileReader("src/test/resources/com/tesco/services/adapters/rpm/readers/store_zone/STORE_ZONE_STORE_NOT_FOUND.csv");
    }

    @Test
    public void shouldThrowExceptionGivenZoneIdNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("ZONE_ID is not found");

        rpmStoreZoneReader = new RPMStoreZoneCSVFileReader("src/test/resources/com/tesco/services/adapters/rpm/readers/store_zone/STORE_ZONE_ZONE_ID_NOT_FOUND.csv");
    }

    @Test
    public void shouldThrowExceptionGivenCurrencyNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("CURRENCY_CODE is not found");

        rpmStoreZoneReader = new RPMStoreZoneCSVFileReader("src/test/resources/com/tesco/services/adapters/rpm/readers/store_zone/STORE_ZONE_CURRENCY_CODE_NOT_FOUND.csv");
    }

    @Test
    public void shouldThrowExceptionGivenZoneTypeNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("ZONE_TYPE is not found");

        rpmStoreZoneReader = new RPMStoreZoneCSVFileReader("src/test/resources/com/tesco/services/adapters/rpm/readers/store_zone/STORE_ZONE_ZONE_TYPE_NOT_FOUND.csv");
    }
}
