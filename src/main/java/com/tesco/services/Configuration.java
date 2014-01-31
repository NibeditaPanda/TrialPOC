package com.tesco.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.configuration.ConfigurationException;

public class Configuration extends com.yammer.dropwizard.config.Configuration {

    @JsonProperty
    private String DBHost;
    @JsonProperty
    private String DBName;
    @JsonProperty
    private String DBPort;
    @JsonProperty
    private String Password;
    @JsonProperty
    private String Username;
    @JsonProperty
    private String ImportScript;
    @JsonProperty
    private HostedGraphiteConfiguration hostedGraphite = new HostedGraphiteConfiguration();

    @JsonProperty("rpm.price.data.dump")
    private String rpmPriceDataPath ;
    @JsonProperty("rpm.promotion.data.dump")
    private String rpmPromotionDataPath;
    @JsonProperty("rpm.store.data.dump")
    private String rpmStoreDataPath;
    @JsonProperty("sonetto.promotions.data.dump")
    private String sonettoPromotionXMLDataPath;
    @JsonProperty("sonetto.promotions.xsd")
    private String sonettoPromotionXSDDataPath;
    @JsonProperty("sonetto.shelfUrl")
    private String sonettoShelfImageUrl;
    @JsonProperty("rpm.promotion_desc.data.dump")
    private String rpmPromotionDescCSVUrl;
    @JsonProperty("rpm.price.zone.data.dump")
    private String rpmPriceZoneDataPath;


    public String getUsername() {
        return Username;
    }

    public String getPassword() {
        return Password;
    }

    public String getDBHost() {
        return DBHost;
    }

    public String getDBName() {
        return DBName;
    }

    public int getDBPort() {
        return Integer.parseInt(DBPort);
    }

    public String getImportScript() {
        return ImportScript;
    }
    public HostedGraphiteConfiguration getHostedGraphiteConfig() {
        return hostedGraphite;
    }

    public String getRPMPriceDataPath() throws ConfigurationException {
        return rpmPriceDataPath;
    }

    public String getRPMPromotionDataPath() throws ConfigurationException {
        return rpmPromotionDataPath;
    }

    public String getRPMStoreDataPath() throws ConfigurationException {
        return rpmStoreDataPath;
    }

    public String getSonettoPromotionsXMLDataPath() throws ConfigurationException {
        return sonettoPromotionXMLDataPath;
    }

    public String getSonettoPromotionXSDDataPath() throws ConfigurationException {
        return sonettoPromotionXSDDataPath;
    }

    public String getSonettoShelfImageUrl() throws ConfigurationException {
        return sonettoShelfImageUrl;
    }

    public String getRPMPromotionDescCSVUrl() throws ConfigurationException {
        return rpmPromotionDescCSVUrl;
    }

    public String getRPMPriceZoneDataPath() {
        return rpmPriceZoneDataPath;
    }
}
