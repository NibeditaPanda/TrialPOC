package com.tesco.services.resources.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;

import java.util.List;

@ApiModel
public class PromotionRequestList {

    @JsonProperty
    private List<PromotionRequest> promotions;

    public List<PromotionRequest> getPromotions() {
        return promotions;
    }
}
