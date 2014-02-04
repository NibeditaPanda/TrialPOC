package com.tesco.services.adapters.rpm.dto;

import com.google.common.base.Optional;

public class StoreDTO {
    public static final int PRICE_ZONE_TYPE = 1;
    public static final int PROMO_ZONE_TYPE = 2;
    private final String storeId;
    private final int zoneId;
    private final String currency;
    private final int zoneType;

    public StoreDTO(String storeId, int zoneId, int zoneType, String currency) {
        this.storeId = storeId;
        this.zoneId = zoneId;
        this.currency = currency;
        this.zoneType = zoneType;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getCurrency() {
        return currency;
    }

    public Optional<Integer> getPriceZoneId() {
        return getZoneId(PRICE_ZONE_TYPE);
    }

    public Optional<Integer> getPromoZoneId() {
        return getZoneId(PROMO_ZONE_TYPE);
    }

    private Optional<Integer> getZoneId(int requiredZoneType) {
        return (zoneType == requiredZoneType) ? Optional.of(zoneId) : Optional.<Integer>absent();
    }
}
