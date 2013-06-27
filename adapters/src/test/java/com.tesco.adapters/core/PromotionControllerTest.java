package com.tesco.adapters.core;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;
import static java.lang.String.format;
import static org.fest.assertions.api.Assertions.assertThat;

public class PromotionControllerTest extends ControllerIntegrationTest{

    @Test
    public void shouldImportListOfPromotionsPerPriceZone() throws IOException {
        List<DBObject> priceZones = priceCollection.find((DBObject) JSON.parse(format("{\"%s\": \"070918248\"}", ITEM_NUMBER))).toArray();
        System.out.println(((DBObject)((DBObject)priceZones.get(0).get(ZONES)).get("6")).get(PROMOTIONS));
        List<DBObject> zone6Promotions = (List<DBObject>) ((DBObject)((DBObject)priceZones.get(0).get(ZONES)).get("6")).get(PROMOTIONS);
        List<DBObject> zone7Promotions = (List<DBObject>) ((DBObject)((DBObject)priceZones.get(0).get(ZONES)).get("7")).get(PROMOTIONS);

        assertThat(zone6Promotions.size()).isEqualTo(2);
        assertThat(zone7Promotions.size()).isEqualTo(1);
        assertThat(zone7Promotions.get(0).get(PROMOTION_OFFER_NAME)).isEqualTo("3 LIONS KICK & TRICK BALL 3.00 SPECIAL PURCHASE");
        assertThat(zone7Promotions.get(0).get(PROMOTION_START_DATE)).isEqualTo("31-Jun-12");
        assertThat(zone7Promotions.get(0).get(PROMOTION_END_DATE)).isEqualTo("04-Jul-13");
        assertThat(zone7Promotions.get(0).get(PROMOTION_CF_DESCRIPTION_1)).isEqualTo("SPECIAL PURCHASE 50p");
        assertThat(zone7Promotions.get(0).get(PROMOTION_CF_DESCRIPTION_2)).isEqualTo("3 LIONS KICK|& TRICK BALL");
        assertThat(zone7Promotions.get(0).keySet()).doesNotContain(ZONE_ID);
        assertThat(zone7Promotions.get(0).keySet()).doesNotContain(ITEM_NUMBER);
    }


}
