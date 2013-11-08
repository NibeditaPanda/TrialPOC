package com.tesco.core;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

import java.util.Properties;

public class DataGridResource {

    private DefaultCacheManager dgClient;

    public DataGridResource() {
        if(dgClient != null && !dgClient.isDefaultRunning()) {
            dgClient.stop();
        }

        dgClient  = new DefaultCacheManager(getConfiguration());

    }

    public Cache<String, Object> getPromotionCache() {
        return dgClient.getCache("promotions", true);
    }

    public void stop() {
        this.dgClient.stop();
    }

    private org.infinispan.configuration.cache.Configuration getConfiguration() {
        Properties properties = new Properties();
        properties.put("hibernate.search.default.directory_provider", "ram");
        properties.put("hibernate.search.default.lucene_version", "3.2.6");

        return new ConfigurationBuilder()
                .jmxStatistics().enabled(true)
                .indexing()
                .enable()
                .indexLocalOnly(true)
                .withProperties(properties)
                .build();
    }
}
