package com.tesco.services;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration extends com.yammer.dropwizard.config.Configuration {

    @JsonProperty
    private String name;

    public String getName() {
        return name;
    }
}
