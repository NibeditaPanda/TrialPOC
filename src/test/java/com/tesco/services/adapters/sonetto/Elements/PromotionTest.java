package com.tesco.services.adapters.sonetto.Elements;

import com.mongodb.DBObject;
import com.tesco.core.PriceKeys;
import org.junit.Test;

import static java.lang.String.format;
import static org.fest.assertions.api.Assertions.assertThat;


public class PromotionTest {

    @Test
    public void createsDBObjectForStorePromotions(){
        String sonettoId = "A123456789";
        String image = "IMAGE";
        Promotion promotion = new PromotionBuilder().withSonettoId(sonettoId).withShelfTalkerImage(image).buildStorePromotion();

        String someUrl = "some %s";
        DBObject actualPromotionDbObject = promotion.buildStoreDBObject(someUrl);
        assertThat(actualPromotionDbObject.get(PriceKeys.PROMOTION_OFFER_ID)).isEqualTo(sonettoId);
        assertThat(actualPromotionDbObject.get(PriceKeys.SHELF_TALKER_IMAGE)).isEqualTo(format(someUrl, image));
    }

    @Test
    public void createDBObjectForInternetPromotions()
    {
        String sonettoId = "A123456789";
        String offerText = "some offer text";
        String startDate = "2013-09-05";
        String endDate = "2014-09-05";

        Promotion promotion = new PromotionBuilder().withSonettoId(sonettoId).withOfferText(offerText).withStartDate(startDate).withEndDate(endDate).buildInternetPromotion();

        DBObject actualPromotionObject = promotion.buildInternetDBObject();

        assertThat(actualPromotionObject.get(PriceKeys.PROMOTION_OFFER_ID)).isEqualTo(sonettoId);
        assertThat(actualPromotionObject.get(PriceKeys.PROMOTION_OFFER_TEXT)).isEqualTo(offerText);
        assertThat(actualPromotionObject.get(PriceKeys.PROMOTION_START_DATE)).isEqualTo(startDate);
        assertThat(actualPromotionObject.get(PriceKeys.PROMOTION_END_DATE)).isEqualTo(endDate);
    }

}
