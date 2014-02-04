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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoreDTO storeDTO = (StoreDTO) o;

        if (zoneId != storeDTO.zoneId) return false;
        if (zoneType != storeDTO.zoneType) return false;
        if (!currency.equals(storeDTO.currency)) return false;
        if (!storeId.equals(storeDTO.storeId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = storeId.hashCode();
        result = 31 * result + zoneId;
        result = 31 * result + currency.hashCode();
        result = 31 * result + zoneType;
        return result;
    }

    @Override
    public String toString() {
        return "StoreDTO{" +
                "storeId='" + storeId + '\'' +
                ", zoneId=" + zoneId +
                ", currency='" + currency + '\'' +
                ", zoneType=" + zoneType +
                '}';
    }
}
