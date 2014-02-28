package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.google.common.base.Optional;
import com.tesco.services.core.Store;
import com.tesco.services.resources.TestConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class StoreRepositoryTest {
    private CouchbaseConnectionManager couchbaseConnectionManager;
    private CouchbaseClient couchbaseClient;

    @Before
    public void setUp() throws Exception {
        couchbaseConnectionManager = new CouchbaseConnectionManager(new TestConfiguration());
        couchbaseClient = couchbaseConnectionManager.getCouchbaseClient();
        couchbaseClient.flush();
    }

    @Test
    public void shouldCacheStoreByStoreId() throws Exception {
        StoreRepository storeRepository = new StoreRepository(couchbaseClient);
        String storeId = "2002";
        Store store = new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP");

        storeRepository.put(store);

        assertThat(storeRepository.getByStoreId(String.valueOf(storeId))).isEqualTo(Optional.of(store));
    }

    @Test
    public void shouldReturnNullObjectWhenProductIsNotFound() throws Exception {
        StoreRepository storeRepository = new StoreRepository(couchbaseClient);
        int storeId = 1234;
        assertThat(storeRepository.getByStoreId(String.valueOf(storeId)).isPresent()).isFalse();
    }

}
