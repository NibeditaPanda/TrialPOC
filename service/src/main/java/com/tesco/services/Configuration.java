package com.tesco.services;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private String importScript;

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
        return importScript;
    }
}
