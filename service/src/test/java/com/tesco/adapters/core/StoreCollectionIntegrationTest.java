package com.tesco.adapters.core;

import com.mongodb.DBObject;
import com.tesco.services.resources.TestConfiguration;
import org.junit.Test;

import static com.mongodb.QueryBuilder.start;
import static com.tesco.core.PriceKeys.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class StoreCollectionIntegrationTest extends ControllerIntegrationTest {


    @Test
    public void shouldImportStoreAndZoneMapping() throws Exception {
        DBObject store = getStore("2002");

        assertThat(store.get(CURRENCY)).isEqualTo("GBP");
        assertThat(store.get(PRICE_ZONE_ID)).isEqualTo("1");
        assertThat(store.get(PROMOTION_ZONE_ID)).isEqualTo("5");
        assertThat(store.get(STORE_ID)).isEqualTo("2002");
    }

    // TODO Vyv: This needs to change because it is relying on updating and not inserting,
    // The process data method renames collections and doesnt expect to write the the
    // currently used collection
    @Test
    public void shouldImportStoreAndZoneMappingOnRefresh() throws Exception {
        TestConfiguration testConfiguration = new TestConfiguration();

        Controller controller = new Controller(
                RPM_PRICE_ZONE_CSV_FILE_PATH,
                RPM_STORE_ZONE_TO_UPDATE_CSV_FILE_PATH,
                RPM_PROMOTION_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XML_FILE_PATH,
                RPM_PROMOTION_DESC_CSV_FILE_PATH, SONETTO_PROMOTIONS_XSD_FILE_PATH,
                testConfiguration.getSonettoShelfImageUrl(),
                dataGridResource.getPromotionCache());
        controller.processData(priceCollection, storeCollection, promotionCollection, false);

        DBObject store = getStore("2002");

        assertThat(store.get(CURRENCY)).isEqualTo("GBP");
        assertThat(store.get(PRICE_ZONE_ID)).isEqualTo("1");
        assertThat(store.get(PROMOTION_ZONE_ID)).isEqualTo("7");
        assertThat(store.get(STORE_ID)).isEqualTo("2002");
    }

    private DBObject getStore(String storeId) {
        return storeCollection.findOne(start(STORE_ID).is(storeId).get());
    }
}
