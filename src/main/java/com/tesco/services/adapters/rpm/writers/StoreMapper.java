package com.tesco.services.adapters.rpm.writers;

import com.google.common.base.Optional;
import com.tesco.services.core.Store;
import com.tesco.services.repositories.StoreRepository;

import java.util.Map;

public class StoreMapper {
    public static final int PRICE_ZONE_TYPE = 1;
    public static final int PROMO_ZONE_TYPE = 2;


    private StoreRepository storeRepository;

    public StoreMapper(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Store map(Map<String, String> storeInfoMap) {
        String storeId = storeInfoMap.get(CSVHeaders.StoreZone.STORE_ID);

        Store store = storeRepository.getByStoreId(String.valueOf(storeId)).or(new Store(storeId, storeInfoMap.get(CSVHeaders.StoreZone.CURRENCY_CODE)));

        int zoneId = Integer.parseInt(storeInfoMap.get(CSVHeaders.StoreZone.ZONE_ID));
        int zoneType = Integer.parseInt(storeInfoMap.get(CSVHeaders.StoreZone.ZONE_TYPE));

        if (zoneType == PRICE_ZONE_TYPE) store.setPriceZoneId(Optional.of(zoneId));
        if (zoneType == PROMO_ZONE_TYPE) store.setPromoZoneId(Optional.of(zoneId));

        return store;
    }
}
