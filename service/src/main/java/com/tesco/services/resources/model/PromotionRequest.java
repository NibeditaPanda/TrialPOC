package com.tesco.services.resources.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel
public class PromotionRequest {

    @JsonProperty
    @ApiModelProperty(required = true)
    private String offerId;

    @JsonProperty
    @ApiModelProperty(required = true)
    private String itemNumber;

    @JsonProperty
    @ApiModelProperty(required = true)
    private String zoneId;

    public String getOfferId() {
        return offerId;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public String getZoneId() {
        return zoneId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PromotionRequest that = (PromotionRequest) o;

        if (itemNumber != null ? !itemNumber.equals(that.itemNumber) : that.itemNumber != null) return false;
        if (offerId != null ? !offerId.equals(that.offerId) : that.offerId != null) return false;
        if (zoneId != null ? !zoneId.equals(that.zoneId) : that.zoneId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = offerId != null ? offerId.hashCode() : 0;
        result = 31 * result + (itemNumber != null ? itemNumber.hashCode() : 0);
        result = 31 * result + (zoneId != null ? zoneId.hashCode() : 0);
        return result;
    }
}
