package com.tesco.adapters.rpm;

import com.mongodb.DBObject;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.tesco.adapters.core.PriceKeys.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class RPMCSVOfferDescriptionReaderTests {

    @Test
    public void shouldReadDescriptionsFromCSV() throws IOException {
        RPMPromotionDescReader rpmPromotionDescReader = new RPMPromotionDescReader("src/test/resources/com/tesco/adapters/rpm/fixtures/PROM_DESC_EXTRACT.csv");

        DBObject promotionInfo = rpmPromotionDescReader.getNext();

        assertThat(promotionInfo.get(PROMOTION_OFFER_ID)).isEqualTo("A29721647");
        assertThat(promotionInfo.get(ZONE_ID)).isEqualTo("5");
        assertThat(promotionInfo.get(PROMOTION_CF_DESCRIPTION_1)).isEqualTo("SPECIAL PURCHASE 50p");
        assertThat(promotionInfo.get(PROMOTION_CF_DESCRIPTION_2)).isEqualTo("3 LIONS FLAG");
    }
}
