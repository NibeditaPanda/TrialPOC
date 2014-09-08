package com.tesco.services.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Store implements Serializable {
    @JsonProperty
    private String storeId;
    @JsonProperty
    private int priceZoneId;
    @JsonProperty
    private int promoZoneId;
    @JsonProperty
    private String currency;

    public Store(String storeId, Optional<Integer> priceZoneId, Optional<Integer> promoZoneId, String currency) {
        this.storeId = storeId;
        setPriceZoneId(priceZoneId);
        setPromoZoneId(promoZoneId);
        this.currency = currency;
    }

    public Store(String storeId, String currency) {
        this(storeId, Optional.<Integer>absent(), Optional.<Integer>absent(), currency);
    }

    public Store() {

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

    public String getStoreId() {
        return storeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Store store = (Store) o;
    /*Modified by Pallavi as part of sonar start*/
        if ((!currency.equals(store.currency))||(!storeId.equals(store.storeId))||(priceZoneId != store.priceZoneId)
                ||(promoZoneId != store.promoZoneId)){
            return false;
        }
    /*Modified by Pallavi as part of sonar end*/


        return true;
    }

    @Override
    public int hashCode() {
        int result = storeId.hashCode();
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

    public String getCurrency() {
        return currency;
    }
}
