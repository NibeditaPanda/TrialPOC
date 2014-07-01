package com.tesco.services.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.tesco.services.Configuration;
import com.tesco.services.HostedGraphiteConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class TestConfiguration extends Configuration {

    private Map<String,Object> configuration = null;

    public TestConfiguration(){
        Yaml yamlConfiguration = new Yaml();
        try {
            String app_env = System.getProperty("environment");
            String filename = "ci".equalsIgnoreCase(app_env) ? "ci.yml" : "test.yml";
            configuration = (Map<String,Object>) yamlConfiguration.load(new String(Files.readAllBytes(Paths.get(filename))));
        } catch (IOException exception) {
            LoggerFactory.getLogger(TestConfiguration.class).error(exception.getMessage());
        }
    }

    public TestConfiguration withBucketName(String bucketName){
        this.setCouchbaseBucket(bucketName);
        return this;
    }

    public static TestConfiguration load() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            String config;
            String environment = System.getProperty("environment");
            if(StringUtils.isEmpty(environment)) {
                config = "test.yml";
            } else {
                config = "ci.yml";
            }
            return objectMapper.readValue(new File(config), TestConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getDBBucketName() {
        return (String) configuration.get("db.bucket.name");
    }

    @Override
    public String getDBBucketPassword() {
        return (String) configuration.get("db.bucket.password");
    }

    @Override
    public String getDBServerUrl() {
        return (String) configuration.get("db.server.url");
    }

    /*public String getDBAdminUsername() {
        return (String) configuration.get("db.server.admin.username");
      public String getDBAdminPassword() {
        return (String) configuration.get("db.server.admin.password");
    }
    }*/
    //rohan
    public String getCouchbaseAdminUsername() {
        return (String) configuration.get("couchbase.admin.username");
    }
    public String getCouchbaseAdminPassword() {
        return (String) configuration.get("couchbase.admin.password");
    }
    public String getCouchbaseBucket() {
        return (String) configuration.get("couchbase.bucket");
    }
    public String getCouchbasePassword() {
        return (String) configuration.get("couchbase.password");
    }

    @Override
    public HostedGraphiteConfiguration getHostedGraphiteConfig () {
        return new HostedGraphiteConfiguration("carbon.hostedgraphite.com",2003,5,"");
    }

    public String getSonettoShelfImageUrl(){
        return "http://ui.tescoassets.com/Groceries/UIAssets/I/Sites/Retail/Superstore/Online/Product/pos/%s.png";
    }
}
