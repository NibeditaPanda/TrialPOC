package com.tesco.adapters.core;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.tesco.adapters.core.PriceKeys.*;
import static java.lang.String.format;
import static org.fest.assertions.api.Assertions.assertThat;

public class SonettoIntegrationTest extends  ControllerIntegrationTest {

    @Test
    public void shouldImportPromotionsIntoPromotionTable() throws IOException {
        DBObject aPromotion = promotionCollection.find((DBObject) JSON.parse(format("{\"%s\": \"A29721690\"}", PROMOTION_OFFER_ID))).toArray().get(0);

        assertThat(aPromotion.get(PROMOTION_OFFER_ID)).isEqualTo("A29721690");
        assertThat(aPromotion.get(SHELF_TALKER_IMAGE)).isEqualTo("http://ui.tescoassets.com/Groceries/UIAssets/I/Sites/Retail/Superstore/Online/Product/pos/specialpurchase.png");
    }
}
