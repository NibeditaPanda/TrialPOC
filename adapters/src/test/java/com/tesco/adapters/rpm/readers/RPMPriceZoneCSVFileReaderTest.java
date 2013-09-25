package com.tesco.adapters.rpm.readers;

import com.mongodb.DBObject;
import com.tesco.adapters.core.exceptions.ColumnNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.tesco.adapters.core.PriceKeys.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class RPMPriceZoneCSVFileReaderTest {

    private RPMPriceZoneCSVFileReader rpmPriceZoneCSVFileReader;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldReturnDBObject() throws Exception {
        this.rpmPriceZoneCSVFileReader = new RPMPriceZoneCSVFileReader("./src/test/resources/com/tesco/adapters/rpm/readers/price_zone/PRICE_ZONE_CORRECT.csv");
        DBObject dbObject = this.rpmPriceZoneCSVFileReader.getNext();

        assertThat(dbObject.get(ITEM_NUMBER)).isEqualTo("050925811");
        assertThat(dbObject.get(String.format("%s.%s.%s", ZONES, 5, PRICE))).isEqualTo("1.33");
        assertThat(dbObject.get(String.format("%s.%s.%s", ZONES, 5, PROMO_PRICE))).isEqualTo("2.33");
    }

    @Test
    public void shouldThrowExceptionGivenItemIsNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("ITEM is not found");

        this.rpmPriceZoneCSVFileReader = new RPMPriceZoneCSVFileReader("./src/test/resources/com/tesco/adapters/rpm/readers/price_zone/PRICE_ZONE_ITEM_NOT_FOUND.csv");

    }

    @Test
    public void shouldThrowExceptionGivenZoneIdIsNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("ZONE_ID is not found");

        this.rpmPriceZoneCSVFileReader = new RPMPriceZoneCSVFileReader("./src/test/resources/com/tesco/adapters/rpm/readers/price_zone/PRICE_ZONE_ZONE_ID_NOT_FOUND.csv");
    }

    @Test
    public void shouldThrowExceptionGivenSellingRetailIsNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("SELLING_RETAIL is not found");

        this.rpmPriceZoneCSVFileReader = new RPMPriceZoneCSVFileReader("./src/test/resources/com/tesco/adapters/rpm/readers/price_zone/PRICE_ZONE_SELLING_RETAIL_NOT_FOUND.csv");
    }

    @Test
    public void shouldThrowExceptionGivenSimplePromoRetailIsNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("SIMPLE_PROMO_RETAIL is not found");

        this.rpmPriceZoneCSVFileReader = new RPMPriceZoneCSVFileReader("./src/test/resources/com/tesco/adapters/rpm/readers/price_zone/PRICE_ZONE_SIMPLE_PROMO_RETAIL_NOT_FOUND.csv");
    }
}
