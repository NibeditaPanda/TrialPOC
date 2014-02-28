package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.google.common.base.Optional;
import com.tesco.services.core.Store;

public class StoreRepository {
    private CouchbaseClient couchbaseClient;

    public StoreRepository(CouchbaseClient couchbaseClient) {
        this.couchbaseClient = couchbaseClient;
    }

    public void put(Store store) {
        couchbaseClient.set(store.getStoreId(), store);
    }

    public Optional<Store> getByStoreId(String storeId) {
        final Store store = (Store)couchbaseClient.get(storeId);
        return (store != null) ? Optional.of(store) : Optional.<Store>absent();
    }
}
