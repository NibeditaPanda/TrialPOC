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
        DBObject priceZone = priceCollection.find((DBObject) JSON.parse(format("{\"%s\": \"070918248\"}", ITEM_NUMBER))).toArray().get(0);
        List<DBObject> zone6Promotions = (List<DBObject>) ((DBObject)((DBObject)priceZone.get(ZONES)).get("6")).get(PROMOTIONS);
        List<DBObject> zone7Promotions = (List<DBObject>) ((DBObject)((DBObject)priceZone.get(ZONES)).get("7")).get(PROMOTIONS);

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

    @Test
    public void shouldNotImportDuplicatePromotions() throws IOException {
        DBObject priceZones = priceCollection.find((DBObject) JSON.parse(format("{\"%s\": \"066367922\"}", ITEM_NUMBER))).toArray().get(0);
        List<DBObject> zone12Promotions = (List<DBObject>) ((DBObject)((DBObject)priceZones.get(ZONES)).get("12")).get(PROMOTIONS);

        assertThat(zone12Promotions.size()).isEqualTo(1);
    }

    @Test
    public void shouldNotImportPromotionAttributeIfPromotionIsNotInExtract() throws IOException {
        DBObject priceZones = priceCollection.find((DBObject) JSON.parse(format("{\"%s\": \"050940579\"}", ITEM_NUMBER))).toArray().get(0);
        DBObject zone = (DBObject)((DBObject)priceZones.get(ZONES)).get("5");
        System.out.println(zone.keySet());
        assertThat(zone.keySet()).doesNotContain(PROMOTIONS);
    }


}
