package com.tesco.adapters.sonetto.Elements;

public class PromotionBuilder {
    private String offerId;
    private String image;
    private String offerText;
    private String startDate;
    private String endDate;

    public PromotionBuilder withSonettoId(String offerId) {
        this.offerId = offerId;
        return this;
    }

    public PromotionBuilder withShelfTalkerImage(String image) {
        this.image = image;
        return this;
    }

    public Promotion buildStorePromotion() {
        return new Promotion(offerId, image, false);
    }

    public Promotion buildInternetPromotion() {
        return new Promotion(offerId, "", true, startDate, endDate, offerText);
    }

    public PromotionBuilder withOfferText(String offerText) {
        this.offerText = offerText;
        return this;
    }

    public PromotionBuilder withStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public PromotionBuilder withEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }
}
