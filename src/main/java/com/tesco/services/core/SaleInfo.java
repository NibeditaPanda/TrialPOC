package com.tesco.services.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SaleInfo implements Serializable {
    private int zoneId;
    private String price;
    private Map<String, Promotion> promotions;

    public SaleInfo(int zoneId, String price) {
        this.zoneId = zoneId;
        this.price = price;
    }

    public void addPromotion(Promotion promotion) {
        if (promotions == null) {
            promotions = new HashMap<>();
        }

        promotions.put(promotion.getOfferId(), promotion);
    }

    public int getZoneId() {
        return zoneId;
    }

    public String getPrice() {
        return price;
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
}
