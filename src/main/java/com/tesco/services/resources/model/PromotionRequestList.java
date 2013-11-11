package com.tesco.services.resources.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel
public class PromotionRequestList {

    @JsonProperty
    @ApiModelProperty(required = true)
    private List<PromotionRequest> promotions;

    public List<PromotionRequest> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<PromotionRequest> promotions) {
        this.promotions = promotions;
    }
}
