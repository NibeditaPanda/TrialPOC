package com.tesco.adapters.core;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.junit.Test;

import static com.tesco.adapters.core.PriceKeys.*;
import static java.lang.String.format;
import static org.fest.assertions.api.Assertions.assertThat;

public class PromotionDescriptionControllerTest extends ControllerIntegrationTest {

    @Test
    public void shouldWriteCFDescriptionsFromRPMDescCSVToMongo() {
        DBObject zone5Promotion = promotionCollection.find((DBObject) JSON.parse(format("{\"%s\": \"A29721647\", \"%s\": \"5\"}", PROMOTION_OFFER_ID, ZONE_ID))).toArray().get(0);
        DBObject zone6Promotion = promotionCollection.find((DBObject) JSON.parse(format("{\"%s\": \"A29721647\", \"%s\": \"6\"}", PROMOTION_OFFER_ID, ZONE_ID))).toArray().get(0);

        assertThat(zone5Promotion.get(PROMOTION_CF_DESCRIPTION_1)).isEqualTo("SPECIAL PURCHASE 50p");
        assertThat(zone5Promotion.get(PROMOTION_CF_DESCRIPTION_2)).isEqualTo("3 LIONS FLAG");

        assertThat(zone6Promotion.get(PROMOTION_CF_DESCRIPTION_1)).isEqualTo("SPECIAL PURCHASE 50p");
        assertThat(zone6Promotion.get(PROMOTION_CF_DESCRIPTION_2)).isEqualTo("3 LIONS FLAG");
    }
}
