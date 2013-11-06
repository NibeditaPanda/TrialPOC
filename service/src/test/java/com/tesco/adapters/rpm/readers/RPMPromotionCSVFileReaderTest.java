package com.tesco.adapters.rpm.readers;

import com.mongodb.DBObject;
import com.tesco.adapters.core.exceptions.ColumnNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static com.tesco.core.PriceKeys.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class RPMPromotionCSVFileReaderTest {

    private RPMPromotionCSVFileReader rpmPromotionCSVFileReader;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldReadPromotionalAttributesFromCSVFile() throws Exception {
        this.rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader("src/test/resources/com/tesco/adapters/rpm/fixtures/prom_extract.csv");

        DBObject promotionInfo = this.rpmPromotionCSVFileReader.getNext();

        assertThat(promotionInfo.get(ITEM_NUMBER)).isEqualTo("070918248");
        assertThat(promotionInfo.get(PROMOTION_OFFER_ID)).isEqualTo("A29721688");
        assertThat(promotionInfo.get(PROMOTION_OFFER_NAME)).isEqualTo("3 LIONS KICK & TRICK BALL 1.00 SPECIAL PURCHASE");
        assertThat(promotionInfo.get(PROMOTION_START_DATE)).isEqualTo("31-Apr-12");
        assertThat(promotionInfo.get(PROMOTION_END_DATE)).isEqualTo("04-May-13");
    }

    @Test
    public void shouldThrowExceptionGivenItemNumberIsNotFound() throws IOException, ColumnNotFoundException {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("TPNB is not found");

        this.rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader("src/test/resources/com/tesco/adapters/rpm/readers/promotion/PROM_EXTRACT_ITEM_NUMBER_NOT_FOUND.csv");

    }

    @Test
    public void shouldThrowExceptionGivenZoneIdIsNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("ZONE_ID is not found");

        this.rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader("src/test/resources/com/tesco/adapters/rpm/readers/promotion/PROM_EXTRACT_ZONE_ID_NOT_FOUND.csv");
    }

    @Test
    public void shouldThrowExceptionGivenOfferIdIsNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("OFFER_ID is not found");

        this.rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader("src/test/resources/com/tesco/adapters/rpm/readers/promotion/PROM_EXTRACT_OFFER_ID_NOT_FOUND.csv");
    }

    @Test
    public void shouldThrowExceptionGivenOfferNameIsNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("OFFER_NAME is not found");

        this.rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader("src/test/resources/com/tesco/adapters/rpm/readers/promotion/PROM_EXTRACT_OFFER_NAME_NOT_FOUND.csv");
    }

    @Test
    public void shouldThrowExceptionGivenStartDateIsNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("START_DATE is not found");

        this.rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader("src/test/resources/com/tesco/adapters/rpm/readers/promotion/PROM_EXTRACT_START_DATE_NOT_FOUND.csv");
    }

    @Test
    public void shouldThrowExceptionGivenEndDateIsNotFound() throws Exception {
        expectedEx.expect(ColumnNotFoundException.class);
        expectedEx.expectMessage("END_DATE is not found");

        this.rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader("src/test/resources/com/tesco/adapters/rpm/readers/promotion/PROM_EXTRACT_END_DATE_NOT_FOUND.csv");
    }
}
