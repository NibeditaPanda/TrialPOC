package com.tesco.services.repositories;

import com.google.common.base.Optional;
import com.tesco.services.Configuration;
import com.tesco.services.core.Product;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.Store;
import org.infinispan.Cache;
import org.infinispan.lifecycle.ComponentStatus;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class DataGridResourceTest {

    private DataGridResource dataGridResource;

    @Before
    public void setUp() {
        dataGridResource = new DataGridResource(new Configuration());
    }

    @Test
    public void shouldGetSamePromotionCache() {
        assertThat(dataGridResource.getPromotionCache().isEmpty()).isTrue();

        Promotion promotion = new Promotion();
        promotion.setOfferId("4334");
        String key = "453535";
        dataGridResource.getPromotionCache().put(key, promotion);

        assertThat(dataGridResource.getPromotionCache().get(key)).isEqualTo(promotion);
    }

    @Test
    public void shouldGetSameProductPriceCache() {
        assertThat(dataGridResource.getProductPriceCache().isEmpty()).isTrue();

        String tpnb = "043535353";
        Product product = new Product(tpnb);
        dataGridResource.getProductPriceCache().put(tpnb, product);

        assertThat(dataGridResource.getProductPriceCache().get(tpnb)).isEqualTo(product);
    }

    @Test
    public void shouldGetSameStoreCache() {
        assertThat(dataGridResource.getStoreCache().isEmpty()).isTrue();

        String storeId = "2002";
        Store store = new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP");
        dataGridResource.getStoreCache().put(storeId, store);

        assertThat(dataGridResource.getStoreCache().get(storeId)).isEqualTo(store);
    }

    @Test
    public void shouldGetProductPriceRefreshCache() {
        Cache<String, Product> productPriceRefreshCache = dataGridResource.getProductPriceRefreshCache();
        assertThat(productPriceRefreshCache.isEmpty()).isTrue();

        String tpnb = "043535353";
        Product product = new Product(tpnb);
        productPriceRefreshCache.put(tpnb, product);

        assertThat(productPriceRefreshCache.get(tpnb)).isEqualTo(product);

        assertThat(dataGridResource.getProductPriceRefreshCache().isEmpty()).isTrue();
    }

    @Test
    public void shouldGetPromotionRefreshCache() {
        Cache<String, Promotion> promotionRefreshCache = dataGridResource.getPromotionRefreshCache();
        assertThat(promotionRefreshCache.isEmpty()).isTrue();

        Promotion promotion = new Promotion();
        promotion.setOfferId("4334");
        String key = "453535";
        promotionRefreshCache.put(key, promotion);

        assertThat(promotionRefreshCache.get(key)).isEqualTo(promotion);

        assertThat(dataGridResource.getPromotionRefreshCache().isEmpty()).isTrue();
    }

    @Test
    public void shouldReplaceCurrentProductPriceCacheWithRefresh() throws Exception {
        dataGridResource.getPromotionCache();
        dataGridResource.getPromotionRefreshCache();

        Cache<String,Product> productPriceCache = dataGridResource.getProductPriceCache();

        Cache<String, Product> productPriceRefreshCache = dataGridResource.getProductPriceRefreshCache();
        String newTPNB = "02342424324";
        Product newProduct = new Product(newTPNB);
        productPriceRefreshCache.put(newTPNB, newProduct);

        dataGridResource.replaceCurrentWithRefresh();

        assertThat(productPriceCache.getStatus()).isEqualTo(ComponentStatus.TERMINATED);

        Cache<String, Product> newProductPriceCache = dataGridResource.getProductPriceCache();
        assertThat(newProductPriceCache.get(newTPNB)).isSameAs(newProduct);
    }

    @Test
    public void shouldReplaceCurrentPromotionCacheWithRefresh() throws Exception {
        dataGridResource.getProductPriceCache();
        dataGridResource.getProductPriceRefreshCache();

        Cache<String, Promotion> promotionCache = dataGridResource.getPromotionCache();

        Cache<String, Promotion> promotionRefreshCache = dataGridResource.getPromotionRefreshCache();
        Promotion promotion = new Promotion();
        String key = "12344";
        promotionRefreshCache.put(key, promotion);

        dataGridResource.replaceCurrentWithRefresh();

        assertThat(promotionCache.getStatus()).isEqualTo(ComponentStatus.TERMINATED);

        Cache<String, Promotion> newPromotionCache = dataGridResource.getPromotionCache();
        assertThat(newPromotionCache.get(key)).isSameAs(promotion);
    }
}
