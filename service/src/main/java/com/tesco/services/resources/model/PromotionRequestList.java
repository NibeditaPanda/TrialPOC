package com.tesco.services.resources.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PromotionRequestList {

    @JsonProperty
    private List<PromotionRequest> promotions;

    public List<PromotionRequest> getPromotions() {
        return promotions;
    }
}
