package com.tesco.services.repositories;

import com.tesco.services.Configuration;
import com.tesco.services.core.Product;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.Store;
import org.apache.commons.lang.StringUtils;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class DataGridResource {

    private static final String PROMOTIONS_CACHE = "promotions";
    private static final String PRODUCT_PRICE_CACHE = "product-price";
    private static final Logger logger = LoggerFactory.getLogger(DataGridResource.class);
    private static final String STORE_CACHE = "store";

    private DefaultCacheManager dgClient;

    private static Cache<String, String> priceCacheNameCache;

    public static String DATA_GRID_PASSIVATION_LOCATION = "cache/passivation";


    public DataGridResource(Configuration configuration) {
        dgClient = new DefaultCacheManager(getGlobalConfiguration(), getConfiguration());
        priceCacheNameCache = dgClient.getCache("priceCacheNameCache", true);
        String cacheLocation = configuration.getCacheLocation();

        if(StringUtils.isNotBlank(cacheLocation)){
            DATA_GRID_PASSIVATION_LOCATION = cacheLocation + "/passivation";
        }
    }

    public void stop() {
        this.dgClient.stop();
    }

    private org.infinispan.configuration.cache.Configuration getConfiguration() {
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

    public Cache<String, Promotion> getPromotionCache() {
        return dgClient.getCache(getCacheNameFor(PROMOTIONS_CACHE), true);
    }

    public Cache<String, Product> getProductPriceCache() {
        return dgClient.getCache(getCacheNameFor(PRODUCT_PRICE_CACHE), true);
    }

    public Cache<String, Store> getStoreCache() {
        return dgClient.getCache(getCacheNameFor(STORE_CACHE), true);
    }

    public Cache<String, Promotion> getPromotionRefreshCache() {
        return dgClient.getCache(getRefreshCacheNameFor(PROMOTIONS_CACHE), true);
    }

    public Cache<String, Product> getProductPriceRefreshCache() {
        return dgClient.getCache(getRefreshCacheNameFor(PRODUCT_PRICE_CACHE), true);
    }

    public Cache<String,Store> getStoreRefreshCache() {
        return dgClient.getCache(getRefreshCacheNameFor(STORE_CACHE), true);
    }

    public void replaceCurrentWithRefresh() {
        swapCurrentCacheWithRefreshedCache(PRODUCT_PRICE_CACHE);
        swapCurrentCacheWithRefreshedCache(PROMOTIONS_CACHE);
        swapCurrentCacheWithRefreshedCache(STORE_CACHE);
    }

    private void swapCurrentCacheWithRefreshedCache(String cache) {
        Cache<Object, Object> existingCache = dgClient.getCache(getCacheNameFor(cache), false);
        String nameOfExistingCache = existingCache.getName();

        String nameOfRefreshedCache = priceCacheNameCache.get(cache + "RefreshCacheName");

        logger.info(String.format("Replacing %s with %s", nameOfExistingCache, nameOfRefreshedCache));
        priceCacheNameCache.put(getCurrentCacheKey(cache), nameOfRefreshedCache);

        logger.info("Clearing cache - " + nameOfExistingCache + " of size " + existingCache.size());

        existingCache.clear();
        existingCache.stop();
        dgClient.removeCache(nameOfExistingCache);

        File dgFile = new File(DATA_GRID_PASSIVATION_LOCATION + "/" + nameOfExistingCache);
        logger.info("can delete dgFile " + dgFile.canWrite());
        deleteFileOrFolder(dgFile);
    }

    private String getCacheNameFor(String cache) {
        String currentCacheName = priceCacheNameCache.get(getCurrentCacheKey(cache));
        if(StringUtils.isBlank(currentCacheName)){
            currentCacheName = "Initial"+ cache + "Cache";
            logger.info("Current "+ cache + " cache name set to " + currentCacheName);
            priceCacheNameCache.put(getCurrentCacheKey(cache), currentCacheName);
        }
        logger.info("Getting " + cache + " cache - " + currentCacheName);
        return currentCacheName;
    }

    private String getRefreshCacheNameFor(String cache) {
        String refreshCacheName = String.format("%sCache-%s", cache, new UUIDGenerator().getUUID());
        logger.info(cache + "Refresh cache = " + refreshCacheName);
        priceCacheNameCache.put(cache + "RefreshCacheName", refreshCacheName);

        return refreshCacheName;
    }

    private void deleteFileOrFolder(File file) {
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File fileToDelete : files) {
                deleteFileOrFolder(fileToDelete);
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    private String getCurrentCacheKey(String cache) {
        return "Current" + cache + "CacheName";
    }
}
