package com.tesco.services.core;

import com.google.common.base.Optional;

public class Store {
    private String id;
    private Optional<Integer> priceZoneId;
    private Optional<Integer> promoZoneId;
    private String currency;

    public void setPriceZoneId(Optional<Integer> priceZoneId) {
        this.priceZoneId = priceZoneId;
    }

    public void setPromoZoneId(Optional<Integer> promoZoneId) {
        this.promoZoneId = promoZoneId;
    }

    public Optional<Integer> getPriceZoneId() {
        return priceZoneId;
    }

    public Optional<Integer> getPromoZoneId() {
        return promoZoneId;
    }

    public Store(String id, Optional<Integer> priceZoneId, Optional<Integer> promoZoneId, String currency) {
        this.id = id;
        this.priceZoneId = priceZoneId;
        this.promoZoneId = promoZoneId;
        this.currency = currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Store store = (Store) o;

        if (!currency.equals(store.currency)) return false;
        if (!id.equals(store.id)) return false;
        if (!priceZoneId.equals(store.priceZoneId)) return false;
        if (!promoZoneId.equals(store.promoZoneId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + priceZoneId.hashCode();
        result = 31 * result + promoZoneId.hashCode();
        result = 31 * result + currency.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Store{" +
                "id='" + id + '\'' +
                ", priceZoneId=" + priceZoneId +
                ", promoZoneId=" + promoZoneId +
                ", currency='" + currency + '\'' +
                '}';
    }
}
