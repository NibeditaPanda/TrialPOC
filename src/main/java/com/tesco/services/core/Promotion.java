package com.tesco.services.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

//TODO: This domain class needs refactoring to suit couchbase

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
@ApiModel(value = "A promotion is special pricing for a product")
public class Promotion implements Serializable {

    @JsonIgnore
    private String uniqueKey;

    @JsonProperty
    private String shelfTalkerImage;

    @JsonProperty
    private String offerId;

    @JsonProperty
    private String itemNumber;

    @JsonProperty
    private int zoneId;
    @JsonProperty
    private String CFDescription1;
    @JsonProperty
    private String CFDescription2;
    @JsonProperty
    private String offerName;
    @JsonProperty
    private String effectiveDate;
    @JsonProperty
    private String endDate;

    public Promotion() {
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public int getZoneId() {
        return zoneId;
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

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getShelfTalkerImage() {
        return shelfTalkerImage;
    }

    public void setShelfTalkerImage(String shelfTalkerImage) {
        this.shelfTalkerImage = shelfTalkerImage;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public void setCFDescription1(String CFDescription1) {
        this.CFDescription1 = CFDescription1;
    }

    public void setCFDescription2(String CFDescription2) {
        this.CFDescription2 = CFDescription2;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        Promotion promotion = (Promotion) o;

        if (zoneId != promotion.zoneId){
            return false;
        }
        if (CFDescription1 != null ? !CFDescription1.equals(promotion.CFDescription1) : promotion.CFDescription1 != null){
            return false;
        }
        if (CFDescription2 != null ? !CFDescription2.equals(promotion.CFDescription2) : promotion.CFDescription2 != null){
            return false;
        }
        if (endDate != null ? !endDate.equals(promotion.endDate) : promotion.endDate != null){
            return false;
        }
        if (itemNumber != null ? !itemNumber.equals(promotion.itemNumber) : promotion.itemNumber != null){
            return false;
        }
        if (offerId != null ? !offerId.equals(promotion.offerId) : promotion.offerId != null){
            return false;
        }
        if (offerName != null ? !offerName.equals(promotion.offerName) : promotion.offerName != null){
            return false;
        }
        if (shelfTalkerImage != null ? !shelfTalkerImage.equals(promotion.shelfTalkerImage) : promotion.shelfTalkerImage != null){
            return false;
        }
        if (effectiveDate != null ? !effectiveDate.equals(promotion.effectiveDate) : promotion.effectiveDate != null){
            return false;
        }
        if (uniqueKey != null ? !uniqueKey.equals(promotion.uniqueKey) : promotion.uniqueKey != null){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = uniqueKey != null ? uniqueKey.hashCode() : 0;
        result = 31 * result + (shelfTalkerImage != null ? shelfTalkerImage.hashCode() : 0);
        result = 31 * result + (offerId != null ? offerId.hashCode() : 0);
        result = 31 * result + (itemNumber != null ? itemNumber.hashCode() : 0);
        result = 31 * result + zoneId;
        result = 31 * result + (CFDescription1 != null ? CFDescription1.hashCode() : 0);
        result = 31 * result + (CFDescription2 != null ? CFDescription2.hashCode() : 0);
        result = 31 * result + (offerName != null ? offerName.hashCode() : 0);
        result = 31 * result + (effectiveDate != null ? effectiveDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Promotion{" +
                "uniqueKey='" + uniqueKey + '\'' +
                ", shelfTalkerImage='" + shelfTalkerImage + '\'' +
                ", offerId='" + offerId + '\'' +
                ", itemNumber='" + itemNumber + '\'' +
                ", zoneId=" + zoneId +
                ", CFDescription1='" + CFDescription1 + '\'' +
                ", CFDescription2='" + CFDescription2 + '\'' +
                ", offerName='" + offerName + '\'' +
                ", effectiveDate='" + effectiveDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}

