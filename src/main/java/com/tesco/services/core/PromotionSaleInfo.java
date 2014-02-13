package com.tesco.services.core;

import java.util.HashMap;
import java.util.Map;

public class PromotionSaleInfo extends SaleInfo {
    private Map<String, Promotion> promotions = new HashMap<>();

    public PromotionSaleInfo(int zoneId, String price) {
        super(zoneId, price);
    }

    public void addPromotion(Promotion promotion) {
        promotions.put(promotion.getOfferId(), promotion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PromotionSaleInfo that = (PromotionSaleInfo) o;

        if (promotions != null ? !promotions.equals(that.promotions) : that.promotions != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (promotions != null ? promotions.hashCode() : 0);
        return result;
    }
}
