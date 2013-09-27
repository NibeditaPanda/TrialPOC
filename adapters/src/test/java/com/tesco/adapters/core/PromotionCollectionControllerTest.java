package com.tesco.adapters.core;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.mongodb.QueryBuilder.start;
import static com.tesco.adapters.core.PriceKeys.*;
import static java.lang.String.format;
import static org.fest.assertions.api.Assertions.assertThat;

public class PromotionCollectionControllerTest extends ControllerIntegrationTest {

    @Test
    public void shouldImportListOfPromotionsPerPriceZone() throws IOException {
        List<DBObject> promotionFromZone6 = getPromotionsFromZone("070918248", "6");
        List<DBObject> promotionFromZone7 = getPromotionsFromZone("070918248", "7");

        assertThat(promotionFromZone6.size()).isEqualTo(2);
        assertThat(promotionFromZone6.get(0).get(PROMOTION_OFFER_ID)).isEqualTo("A29721688");
        assertThat(promotionFromZone6.get(1).get(PROMOTION_OFFER_ID)).isEqualTo("A29721689");

        assertThat(promotionFromZone7.size()).isEqualTo(1);
        assertThat(promotionFromZone7.get(0).get(PROMOTION_OFFER_ID)).isEqualTo("A29721690");
        assertThat(promotionFromZone7.get(0).get(PROMOTION_OFFER_NAME)).isEqualTo("3 LIONS KICK & TRICK BALL 3.00 SPECIAL PURCHASE");
        assertThat(promotionFromZone7.get(0).get(PROMOTION_START_DATE)).isEqualTo("31-Jun-12");
        assertThat(promotionFromZone7.get(0).get(PROMOTION_END_DATE)).isEqualTo("04-Jul-13");
        assertThat(promotionFromZone7.get(0).keySet()).doesNotContain(ZONE_ID);
        assertThat(promotionFromZone7.get(0).keySet()).doesNotContain(ITEM_NUMBER);
    }

    @Test
    public void shouldNotImportDuplicatePromotions() throws IOException {
        List<DBObject> promotionFromZone12 = getPromotionsFromZone("066367922", "12");

        assertThat(promotionFromZone12.size()).isEqualTo(1);
    }

    @Test
    public void shouldNotContainPromotionAttributeIfPromotionIsNotInPromotionExtract() throws IOException {
        DBObject zone = findPricesFromZone("050940579", "5");

        assertThat(zone.keySet()).doesNotContain(PROMOTIONS);
    }

    @Test
    public void shouldNotImportPromotionIfUnableToFindProduct() throws IOException {
        List<DBObject> result = priceCollection.find((DBObject) JSON.parse(format("{\"%s\": \"0123456\"}", ITEM_NUMBER))).toArray();
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void shouldNotImportPromotionIfUnableToFindZoneInProduct() throws IOException {
        DBObject itemNumber = new BasicDBObject(ITEM_NUMBER, "070918248");
        DBObject priceZones = priceCollection.find(itemNumber).toArray().get(0);
        DBObject zones = (DBObject) priceZones.get(ZONES);

        assertThat(zones.keySet()).doesNotContain("9");
    }

    @Test
    public void shouldImportPromotionsIntoPromotionTable() throws IOException {
        DBObject query = start(PROMOTION_OFFER_ID).is("A29721690").and(ZONE_ID).is("7").get();
        DBObject aPromotion = promotionCollection.find(query).toArray().get(0);

        assertThat(aPromotion.get(PROMOTION_OFFER_ID)).isEqualTo("A29721690");
        assertThat(aPromotion.get(ZONE_ID)).isEqualTo("7");
        assertThat(aPromotion.get(PROMOTION_START_DATE)).isEqualTo("31-Jun-12");
        assertThat(aPromotion.get(PROMOTION_END_DATE)).isEqualTo("04-Jul-13");
        assertThat(aPromotion.get(PROMOTION_OFFER_NAME)).isEqualTo("3 LIONS KICK & TRICK BALL 3.00 SPECIAL PURCHASE");
    }

    @Test
    public void shouldImportInternetOnlyPromotions() {
        BasicDBObject promotion1 = new BasicDBObject(PROMOTION_OFFER_ID, "S00001030");
        BasicDBObject promotion2 = new BasicDBObject(PROMOTION_OFFER_ID, "S00001028");

        DBObject promotionA = promotionCollection.find(promotion1).toArray().get(0);
        DBObject promotionB = promotionCollection.find(promotion2).toArray().get(0);

        assertThat(promotionA.get(PriceKeys.PROMOTION_OFFER_ID)).isEqualTo("S00001030");
        assertThat(promotionA.get(PriceKeys.PROMOTION_OFFER_TEXT)).isEqualTo("£10 off your first shop");
        assertThat(promotionA.get(PriceKeys.PROMOTION_START_DATE)).isEqualTo("2009-11-03");
        assertThat(promotionA.get(PriceKeys.PROMOTION_END_DATE)).isEqualTo("2015-12-31");

        assertThat(promotionB.get(PriceKeys.PROMOTION_OFFER_ID)).isEqualTo("S00001028");
        assertThat(promotionB.get(PriceKeys.PROMOTION_OFFER_TEXT)).isEqualTo("£10 off your first shop");
        assertThat(promotionB.get(PriceKeys.PROMOTION_START_DATE)).isEqualTo("2009-11-03");
        assertThat(promotionB.get(PriceKeys.PROMOTION_END_DATE)).isEqualTo("2015-12-31");
    }

    private List<DBObject> getPromotionsFromZone(String itemNumber, String zoneId) {
        DBObject itemNumberDbObject = new BasicDBObject(ITEM_NUMBER, itemNumber);
        DBObject priceZone = priceCollection.find(itemNumberDbObject).toArray().get(0);
        List<DBObject> promotionsFromZone = (List<DBObject>) ((DBObject) ((DBObject) priceZone.get(ZONES)).get(zoneId)).get(PROMOTIONS);

        return promotionsFromZone;
    }
}
