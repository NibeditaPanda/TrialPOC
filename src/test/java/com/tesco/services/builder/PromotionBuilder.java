package com.tesco.services.builder;

import com.tesco.services.core.Promotion;

public class PromotionBuilder {

    private String uniqueKey = "uuid";
    private String offerId = "offerId";
    private String itemNumber = "itemNumber";
    private int zoneId = 1;
    private String description1 = "description";
    private String description2 = "description";
    private String effectiveDate = "startDate";
    private String endDate = "endDate";
    private String offerName = "offerName";
    private String shelfTalker = "shelfTalker";

    public static PromotionBuilder aPromotion() {
        return new PromotionBuilder();
    }

    public PromotionBuilder offerId(String offerId) {
        this.offerId = offerId;
        return this;
    }

    public PromotionBuilder tpnc(String itemNumber) {
        this.itemNumber = itemNumber;
        return this;
    }

    public PromotionBuilder zoneId(int zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    public PromotionBuilder description1(String description) {
        this.description1 = description;
        return this;
    }

    public PromotionBuilder description2(String description) {
        this.description2 = description;
        return this;
    }

    public PromotionBuilder uniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
        return this;
    }

    public PromotionBuilder startDate(String startDate) {
        this.effectiveDate = startDate;
        return this;
    }

    public PromotionBuilder endDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public PromotionBuilder offerName(String offerName) {
        this.offerName = offerName;
        return this;
    }

    public PromotionBuilder shelfTalker(String shelfTalker) {
        this.shelfTalker = shelfTalker;
        return this;
    }

    public Promotion build() {
        Promotion promotion = new Promotion();
        promotion.setUniqueKey(this.uniqueKey);
        promotion.setOfferId(this.offerId);
        promotion.setItemNumber(this.itemNumber);
        promotion.setZoneId(this.zoneId);
        promotion.setCFDescription1(this.description1);
        promotion.setCFDescription2(this.description2);
        promotion.setEffectiveDate(this.effectiveDate);
        promotion.setEndDate(this.endDate);
        promotion.setOfferName(this.offerName);
        promotion.setShelfTalkerImage(this.shelfTalker);

        return promotion;
    }

    public Promotion createPromotion() {
        Promotion promotion = new Promotion();
        promotion.setOfferId(this.offerId);
        promotion.setEffectiveDate(this.effectiveDate);
        promotion.setEndDate(this.endDate);
        promotion.setOfferName(this.offerName);
        promotion.setCFDescription1(this.description1);
        promotion.setCFDescription2(this.description2);

        return promotion;
    }
}
