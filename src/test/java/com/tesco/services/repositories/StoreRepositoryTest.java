package com.tesco.services.repositories;

import com.google.common.base.Optional;
import com.tesco.services.core.Store;
import com.tesco.services.resources.TestConfiguration;
import org.infinispan.Cache;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class StoreRepositoryTest {
    private Cache<String,Store> storeCache;
    private DataGridResource dataGridResource;

    @Before
    public void setUp() throws Exception {
        dataGridResource = new DataGridResourceForTest(new TestConfiguration());
        storeCache = dataGridResource.getStoreCache();
    }

    @Test
    public void shouldCacheStoreByStoreId() throws Exception {
        StoreRepository storeRepository = new StoreRepository(storeCache);
        String storeId = "2002";
        Store store = new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP");

        storeRepository.put(store);

        assertThat(storeRepository.getByStoreId(storeId)).isEqualTo(store);
    }

}
