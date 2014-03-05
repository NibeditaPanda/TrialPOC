package com.tesco.services.adapters.core;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.tesco.services.core.Store;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.repositories.StoreRepository;
import com.tesco.services.resources.TestConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.mongodb.QueryBuilder.start;
import static com.tesco.services.adapters.core.TestFiles.*;
import static com.tesco.services.core.PriceKeys.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class StoreImportIntegrationTest extends ImportJobIntegrationTestBase {
    private String oldStoreId = "4002";

    @Override
    protected void preImportCallBack() {
    }

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


        ImportJob importJob = new ImportJob(
                RPM_PRICE_ZONE_CSV_FILE_PATH,
                RPM_STORE_ZONE_TO_UPDATE_CSV_FILE_PATH,
                RPM_PROMOTION_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XML_FILE_PATH,
                RPM_PROMOTION_DESC_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XSD_FILE_PATH,
                testConfiguration.getSonettoShelfImageUrl(),
                RPM_PRICE_ZONE_PRICE_CSV_FILE_PATH,
                RPM_PROMO_ZONE_PRICE_CSV_FILE_PATH,
                RPM_PROMO_EXTRACT_CSV_FILE_PATH,
                RPM_PROMO_DESC_EXTRACT_CSV_FILE_PATH,
                dbFactory,
                couchbaseConnectionManager);

        importJob.processData(priceCollection, storeCollection, promotionCollection, false);

        DBObject store = getStore("2002");

        assertThat(store.get(CURRENCY)).isEqualTo("GBP");
        assertThat(store.get(PRICE_ZONE_ID)).isEqualTo("1");
        assertThat(store.get(PROMOTION_ZONE_ID)).isEqualTo("7");
        assertThat(store.get(STORE_ID)).isEqualTo("2002");
    }

    @Test
    public void shouldImportStoreZonesToReplacedCache() throws InterruptedException, IOException, URISyntaxException {
        String storeId = "2002";
        Store store = new Store(storeId, Optional.of(1), Optional.of(5), "GBP");

        CouchbaseConnectionManager couchbaseConnectionManager = new CouchbaseConnectionManager(new TestConfiguration());
        StoreRepository storeRepository = new StoreRepository(couchbaseConnectionManager.getCouchbaseClient());
        assertThat(storeRepository.getByStoreId(storeId).get()).isEqualTo(store);
        assertThat(storeRepository.getByStoreId(oldStoreId).isPresent()).isFalse();
    }

    private DBObject getStore(String storeId) {
        return storeCollection.findOne(start(STORE_ID).is(storeId).get());
    }
}
