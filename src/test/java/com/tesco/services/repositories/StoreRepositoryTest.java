package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.google.common.base.Optional;
import com.tesco.services.IntegrationTest;
import com.tesco.services.core.Store;
import com.tesco.services.resources.TestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class StoreRepositoryTest extends IntegrationTest {
    private String storeId = "2002";
    private Store store;
    private StoreRepository storeRepository;

    @Before
    public void setUp() throws Exception {
        storeRepository = new StoreRepository(new CouchbaseConnectionManager(new TestConfiguration()).getCouchbaseClient());
        store = new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP");
    }

    @Test
    public void shouldCacheStoreByStoreId() throws Exception {
        storeRepository.put(store);
        assertThat(storeRepository.getByStoreId(storeId)).isEqualTo(Optional.of(store));
    }

    @Test
    public void shouldReturnNullObjectWhenProductIsNotFound() throws Exception {
        int storeId = 1234;
        assertThat(storeRepository.getByStoreId(String.valueOf(storeId)).isPresent()).isFalse();
    }

    @Test
    public void shouldNamespacePrefixKey() {
        final CouchbaseClient couchbaseClientMock = mock(CouchbaseClient.class);
        storeRepository = new StoreRepository(couchbaseClientMock);
        final InOrder inOrder = inOrder(couchbaseClientMock);

        storeRepository.put(store);
        storeRepository.getByStoreId(storeId);

        inOrder.verify(couchbaseClientMock).set("STORE_" + storeId, store);
        inOrder.verify(couchbaseClientMock).get("STORE_" + storeId);
    }
}
