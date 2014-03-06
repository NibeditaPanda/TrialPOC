package com.tesco.services.adapters.core;

import com.google.common.base.Optional;
import com.tesco.services.core.Store;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.repositories.StoreRepository;
import com.tesco.services.resources.TestConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.fest.assertions.api.Assertions.assertThat;

public class StoreImportIntegrationTest extends ImportJobIntegrationTestBase {
    private String oldStoreId = "4002";

    @Override
    protected void preImportCallBack() {
    }

    @Test
    public void shouldImportStoreZones() throws InterruptedException, IOException, URISyntaxException {
        String storeId = "2002";
        Store store = new Store(storeId, Optional.of(1), Optional.of(5), "GBP");

        CouchbaseConnectionManager couchbaseConnectionManager = new CouchbaseConnectionManager(new TestConfiguration());
        StoreRepository storeRepository = new StoreRepository(couchbaseConnectionManager.getCouchbaseClient());
        assertThat(storeRepository.getByStoreId(storeId).get()).isEqualTo(store);
        assertThat(storeRepository.getByStoreId(oldStoreId).isPresent()).isFalse();
    }
}
