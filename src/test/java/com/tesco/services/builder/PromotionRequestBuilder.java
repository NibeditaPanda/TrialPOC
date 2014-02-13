package com.tesco.services.builder;

import com.tesco.services.resources.model.PromotionRequest;

public class PromotionRequestBuilder {
    private int zoneId;
    private String itemNumber;
    private String offerId;

    public static PromotionRequestBuilder aPromotionRequest() {
        return new PromotionRequestBuilder();
    }

    public PromotionRequestBuilder zoneId(int zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    public PromotionRequestBuilder itemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
        return this;
    }

    public PromotionRequestBuilder offerId(String offerId) {
        this.offerId = offerId;
        return this;
    }

    public PromotionRequest build() {
        PromotionRequest promotionRequest = new PromotionRequest();
        promotionRequest.setZoneId(zoneId);
        promotionRequest.setItemNumber(itemNumber);
        promotionRequest.setOfferId(offerId);

        return promotionRequest;
    }
}
