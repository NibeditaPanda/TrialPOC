package com.tesco.services.repositories;

import com.tesco.services.core.Promotion;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

import java.util.Properties;

public class DataGridResource {

    private static final String PROMOTIONS_CACHE = "promotions";

    private DefaultCacheManager dgClient;

    public DataGridResource() {
        dgClient = new DefaultCacheManager(getGlobalConfiguration(), getConfiguration());
    }

    public Cache<String, Promotion> getPromotionCache() {
        return dgClient.getCache(PROMOTIONS_CACHE, true);
    }

    public void stop() {
        this.dgClient.stop();
    }

    private Configuration getConfiguration() {
        Properties properties = new Properties();
        properties.put("hibernate.search.default.directory_provider", "ram");
        properties.put("hibernate.search.default.lucene_version", "3.2.6");

        return new ConfigurationBuilder()
                .indexing()
                .enable()
                .indexLocalOnly(true)
                .withProperties(properties)
                .build();
    }

    private GlobalConfiguration getGlobalConfiguration() {
        return GlobalConfigurationBuilder
                .defaultClusteredBuilder()
                .globalJmxStatistics()
                .allowDuplicateDomains(true)
                .build();
    }
}
