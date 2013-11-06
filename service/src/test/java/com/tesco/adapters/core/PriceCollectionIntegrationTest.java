package com.tesco.adapters.core;

import com.mongodb.DBObject;
import com.tesco.services.resources.TestConfiguration;
import org.junit.Test;

import java.io.IOException;

import static com.tesco.core.PriceKeys.PRICE;
import static com.tesco.core.PriceKeys.PROMO_PRICE;
import static org.fest.assertions.api.Assertions.assertThat;

public class PriceCollectionIntegrationTest extends ControllerIntegrationTest {

    @Test
    public void shouldFindPriceFromOneZone() throws IOException {
        DBObject prices = findPricesFromZone("050925811", "5");

        assertThat(prices.get(PRICE)).isEqualTo("1.33");
        assertThat(prices.get(PROMO_PRICE)).isEqualTo("2.33");
    }

    @Test
    public void shouldFindPricesGivenMultipleZones() throws IOException {

        DBObject pricesFromZoneFive = findPricesFromZone("050940579", "5");
        assertThat(pricesFromZoneFive.get(PRICE)).isEqualTo("5.33");
        assertThat(pricesFromZoneFive.get(PROMO_PRICE)).isEqualTo("5.33");

        DBObject pricesFromZoneThree = findPricesFromZone("050940579", "3");
        assertThat(pricesFromZoneThree.get(PRICE)).isEqualTo("2.33");
        assertThat(pricesFromZoneThree.get(PROMO_PRICE)).isEqualTo("2.33");
    }

    @Test
    public void shouldUpdatePrices() throws Exception {
        TestConfiguration testConfiguration = new TestConfiguration();
        Controller controller = new Controller(
                RPM_PRICE_ZONE_TO_UPDATE_CSV_FILE_PATH,
                RPM_STORE_ZONE_CSV_FILE_PATH,
                RPM_PROMOTION_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XML_FILE_PATH,
                RPM_PROMOTION_DESC_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XSD_FILE_PATH, testConfiguration.getSonettoShelfImageUrl());
        controller.processData(tempPriceCollection, tempStoreCollection, tempPromotionCollection, false);

        DBObject prices = findPricesFromZone("050925811", "5");

        assertThat(prices.get(PRICE)).isEqualTo("20.33");
        assertThat(prices.get(PROMO_PRICE)).isEqualTo("12.33");
    }

}
