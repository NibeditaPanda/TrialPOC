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
        final String storeId = store.getStoreId();
        couchbaseClient.set(getStoreKey(storeId), store);
    }

    private String getStoreKey(String storeId) {
        return String.format("STORE_%s", storeId);
    }

    public Optional<Store> getByStoreId(String storeId) {
        final Store store = (Store)couchbaseClient.get(getStoreKey(storeId));
        return (store != null) ? Optional.of(store) : Optional.<Store>absent();
    }
}
