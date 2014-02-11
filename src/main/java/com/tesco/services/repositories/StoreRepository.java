package com.tesco.services.repositories;

import com.google.common.base.Optional;
import com.tesco.services.core.Store;
import org.infinispan.Cache;

public class StoreRepository {
    private Cache<Integer, Store> storeCache;

    public StoreRepository(Cache<Integer, Store> storeCache) {
        this.storeCache = storeCache;
    }

    public void put(Store store) {
        storeCache.put(store.getStoreId(), store);
    }

    public Optional<Store> getByStoreId(int storeId) {
        Store store = storeCache.get(storeId);
        return (store != null) ? Optional.of(store) : Optional.<Store>absent();
    }
}
