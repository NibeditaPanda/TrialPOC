package com.tesco.services.resources.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Promotion {

    @JsonProperty
    private String offerId;

    @JsonProperty
    private String itemNumber;

    @JsonProperty
    private String zoneID;

    @JsonProperty
    private String CFDescription1;

    @JsonProperty
    private String CFDescription2;

    @JsonProperty
    private String offerName;

    @JsonProperty
    private String startDate;

    @JsonProperty
    private String endDate;

    public String getOfferId() {
        return offerId;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public String getZoneID() {
        return zoneID;
    }

    public String getCFDescription1() {
        return CFDescription1;
    }

    public String getCFDescription2() {
        return CFDescription2;
    }

    public String getOfferName() {
        return offerName;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Promotion(String offerId, String itemNumber, String zoneID) {
        this.offerId = offerId;
        this.itemNumber = itemNumber;
        this.zoneID = zoneID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Promotion promotion = (Promotion) o;

        if (itemNumber != null ? !itemNumber.equals(promotion.itemNumber) : promotion.itemNumber != null) return false;
        if (offerId != null ? !offerId.equals(promotion.offerId) : promotion.offerId != null) return false;
        if (zoneID != null ? !zoneID.equals(promotion.zoneID) : promotion.zoneID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = offerId != null ? offerId.hashCode() : 0;
        result = 31 * result + (itemNumber != null ? itemNumber.hashCode() : 0);
        result = 31 * result + (zoneID != null ? zoneID.hashCode() : 0);
        return result;
    }

    public Integer hash(){
        return hashCode();
    }
}
