package com.tesco.services.builder;

import com.tesco.services.Promotion;

public class PromotionBuilder {

    private String offerId = "offerId";
    private String itemNumber = "itemNumber";
    private String zoneId = "zoneId";

    public static PromotionBuilder aPromotion() {
        return new PromotionBuilder();
    }

    public PromotionBuilder offerId(String offerId) {
        this.offerId = offerId;
        return this;
    }

    public PromotionBuilder itemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
        return this;
    }

    public PromotionBuilder zoneId(String zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    public Promotion build() {
        Promotion promotion = new Promotion();
        promotion.setOfferId(this.offerId);
        promotion.setItemNumber(this.itemNumber);
        promotion.setZoneId(this.zoneId);

        return promotion;
    }

}
