package com.tesco.adapters.rpm;

import com.mongodb.DBObject;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.tesco.adapters.core.PriceKeys.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class RPMPromotionCSVFileReaderTest {

    @Test
    public void shouldReadPromotionalAttributesFromCSVFile() throws IOException {
        RPMPromotionCSVFileReader rpmPromotionCSVFileReader = new RPMPromotionCSVFileReader("src/test/resources/com/tesco/adapters/rpm/fixtures/prom_extract.csv");

        DBObject promotionInfo = rpmPromotionCSVFileReader.getNext();

        assertThat(promotionInfo.get(ITEM_NUMBER)).isEqualTo("070918248");
        assertThat(promotionInfo.get(PROMOTION_OFFER_ID)).isEqualTo("A29721688");
        assertThat(promotionInfo.get(PROMOTION_OFFER_NAME)).isEqualTo("3 LIONS KICK & TRICK BALL 1.00 SPECIAL PURCHASE");
        assertThat(promotionInfo.get(PROMOTION_START_DATE)).isEqualTo("31-Apr-12");
        assertThat(promotionInfo.get(PROMOTION_END_DATE)).isEqualTo("04-May-13");
    }
}
