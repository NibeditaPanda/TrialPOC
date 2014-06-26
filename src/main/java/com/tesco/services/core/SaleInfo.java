package com.tesco.services.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class SaleInfo implements Serializable {
    @JsonProperty
    private int zoneId;
    @JsonProperty
    private String price;
    @JsonProperty
    private Map<String, Promotion> promotions = new HashMap<>();

    public SaleInfo(int zoneId, String price) {
        this.zoneId = zoneId;
        this.price = price;
    }
    public SaleInfo() {

    }

    public void addPromotion(Promotion promotion) {
        promotions.put(promotion.getOfferId(), promotion);
    }

    public int getZoneId() {
        return zoneId;
    }

    public String getPrice() {
        return price;
    }

    public Promotion getPromotionByOfferId(String offerId) {
        return promotions.get(offerId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SaleInfo saleInfo = (SaleInfo) o;

        if (zoneId != saleInfo.zoneId) return false;
        if (price != null ? !price.equals(saleInfo.price) : saleInfo.price != null) return false;
        if (promotions != null ? !promotions.equals(saleInfo.promotions) : saleInfo.promotions != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = zoneId;
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (promotions != null ? promotions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SaleInfo{" +
                "zoneId=" + zoneId +
                ", price='" + price + '\'' +
                ", promotions=" + promotions +
                '}';
    }

    public Collection<Promotion> getPromotions() {
        return promotions.values();
    }
}
