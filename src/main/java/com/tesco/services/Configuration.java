package com.tesco.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.configuration.ConfigurationException;

public class Configuration extends com.yammer.dropwizard.config.Configuration {

    @JsonProperty
    private String ImportScript;
    @JsonProperty
    private HostedGraphiteConfiguration hostedGraphite = new HostedGraphiteConfiguration();

    @JsonProperty("rpm.store.data.dump")
    private String rpmStoreDataPath;

    @JsonProperty("sonetto.promotions.data.dump")
    private String sonettoPromotionXMLDataPath;

    @JsonProperty("sonetto.promotions.xsd")
    private String sonettoPromotionXSDDataPath;

    @JsonProperty("sonetto.shelfUrl")
    private String sonettoShelfImageUrl;

    @JsonProperty("rpm.price.zone.data.dump")
    private String rpmPriceZoneDataPath;

    @JsonProperty("rpm.promo.zone.data.dump")
    private String rpmPromoZoneDataPath;

    @JsonProperty("rpm.promo.extract.data.dump")
    private String rpmPromoExtractDataPath;

    @JsonProperty("rpm.promo.desc.extract.data.dump")
    private String rpmPromoDescExtractDataPath;

    @JsonProperty("db.bucket.name")
    private String dbBucketName;
    @JsonProperty("db.bucket.password")
    private String dbBucketPassword;
    @JsonProperty("db.server.url")
    private String dbServerUrl;

    @JsonProperty("couchbase.nodes")
    private String[] couchbaseNodes;

    @JsonProperty("couchbase.bucket")
    private String couchbaseBucket;

    @JsonProperty("couchbase.admin.username")
    private String couchbaseAdminUsername;

    @JsonProperty("couchbase.admin.password")
    private String couchbaseAdminPassword;

    @JsonProperty("couchbase.username")
    private String couchbaseUsername;

    @JsonProperty("couchbase.password")
    private String couchbasePassword;

    public boolean isDummyCouchbaseMode() {
        return dummyCouchbaseMode;
    }

    @JsonProperty("dummyCouchbaseMode")
    private boolean dummyCouchbaseMode;
    public String[] getCouchbaseNodes() {
        return couchbaseNodes;
    }

    public String getCouchbaseBucket() {
        return couchbaseBucket;
    }

    public String getCouchbaseAdminUsername() {
        return couchbaseAdminUsername;
    }

    public String getCouchbaseAdminPassword() {
        return couchbaseAdminPassword;
    }

    public String getCouchbaseUsername() {
        return couchbaseUsername;
    }

    public String getCouchbasePassword() {
        return couchbasePassword;
    }

    public String getDBBucketName() {
        return dbBucketName;
    }

    public String getDBBucketPassword() {
        return dbBucketPassword;
    }

    public String getDBServerUrl() {
        return dbServerUrl;
    }

    public String getImportScript() {
        return ImportScript;
    }

    public HostedGraphiteConfiguration getHostedGraphiteConfig() {
        return hostedGraphite;
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

    public String getRPMPriceZoneDataPath() {
        return rpmPriceZoneDataPath;
    }

    public String getRPMPromoZoneDataPath() {
        return rpmPromoZoneDataPath;
    }

    public String getRPMPromoExtractDataPath() {
        return rpmPromoExtractDataPath;
    }

    public String getRPMPromoDescExtractDataPath() {
        return rpmPromoDescExtractDataPath;
    }

    protected void setCouchbaseBucket(String couchbaseBucket){
        this.couchbaseBucket = couchbaseBucket;
    }
}
