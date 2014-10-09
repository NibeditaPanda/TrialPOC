package com.tesco.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class HostedGraphiteConfiguration {

    @NotEmpty
    @JsonProperty
    private String hostname;

    @NotEmpty
    @JsonProperty
    private int port;

    @NotEmpty
    @JsonProperty
    private int period;

    @NotEmpty
    @JsonProperty
    private String apikey;

    public HostedGraphiteConfiguration(){
    /*Empty Constructor*/
    }

    public HostedGraphiteConfiguration(String hostname, int port, int period, String apikey) {
        this.hostname = hostname;
        this.port = port;
        this.period = period;
        this.apikey = apikey;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public int getPeriod() {
        return period;
    }

    public String getApikey() {
        return apikey;
    }
}
