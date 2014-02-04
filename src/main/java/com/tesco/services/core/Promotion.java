package com.tesco.services.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Entity
@ProvidedId
@Indexed
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
@ApiModel(value = "A promotion is special pricing for a product")

public class Promotion implements Serializable {

    @JsonIgnore
    private String uniqueKey;

    @JsonProperty
    private String shelfTalkerImage;

    @JsonProperty
    @Field(index = Index.YES, analyze = Analyze.NO, store = org.hibernate.search.annotations.Store.YES)
    private String offerId;

    @JsonProperty
    @Field(index = Index.YES, analyze = Analyze.NO, store = org.hibernate.search.annotations.Store.YES)
    private String itemNumber;

    @JsonProperty
    @Field(index = Index.YES, analyze = Analyze.NO, store = org.hibernate.search.annotations.Store.YES)
    private String zoneId;
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

    @JsonProperty
    private String offerText;

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

    public String getZoneId() {
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

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getOfferText() {
        return offerText;
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

    public void setZoneId(String zoneId) {
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

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Promotion promotion = (Promotion) o;

        if (uniqueKey != null ? !uniqueKey.equals(promotion.uniqueKey) : promotion.uniqueKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uniqueKey != null ? uniqueKey.hashCode() : 0;
    }
}

