package com.tesco.services.repositories;

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

    public Store getByStoreId(int storeId) {
        return storeCache.get(storeId);
    }
}
