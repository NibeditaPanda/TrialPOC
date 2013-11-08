package com.tesco.services.builder;

import com.tesco.services.Promotion;

public class PromotionBuilder {

    private String offerId = "offerId";

    public static PromotionBuilder aPromotion() {
        return new PromotionBuilder();
    }

    public PromotionBuilder offerId(String offerId) {
        this.offerId = offerId;
        return this;
    }

    public Promotion build() {
        Promotion promotion = new Promotion();
        promotion.setOfferId(this.offerId);

        return promotion;
    }
}
