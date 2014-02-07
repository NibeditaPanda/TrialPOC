package com.tesco.services.core;

import com.google.common.base.Optional;

import java.io.Serializable;

public class Store implements Serializable {
    private int storeId;
    private int priceZoneId;
    private int promoZoneId;
    private String currency;

    public Store(int storeId, Optional<Integer> priceZoneId, Optional<Integer> promoZoneId, String currency) {
        this.storeId = storeId;
        setPriceZoneId(priceZoneId);
        setPromoZoneId(promoZoneId);
        this.currency = currency;
    }

    public void setPriceZoneId(Optional<Integer> priceZoneId) {
        this.priceZoneId = priceZoneId.or(-1);
    }

    public void setPromoZoneId(Optional<Integer> promoZoneId) {
        this.promoZoneId = promoZoneId.or(-1);
    }

    public Optional<Integer> getPriceZoneId() {
        return (priceZoneId == -1) ? Optional.<Integer>absent() : Optional.of(priceZoneId) ;
    }

    public Optional<Integer> getPromoZoneId() {
        return (promoZoneId == -1) ? Optional.<Integer>absent() : Optional.of(promoZoneId) ;
    }

    public int getStoreId() {
        return storeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Store store = (Store) o;

        if (!currency.equals(store.currency)) return false;
        if (storeId != store.storeId) return false;
        if (priceZoneId != store.priceZoneId) return false;
        if (promoZoneId != store.promoZoneId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = storeId;
        result = 31 * result + priceZoneId;
        result = 31 * result + promoZoneId;
        result = 31 * result + currency.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Store{" +
                "storeId='" + storeId + '\'' +
                ", priceZoneId=" + priceZoneId +
                ", promoZoneId=" + promoZoneId +
                ", currency='" + currency + '\'' +
                '}';
    }
}
