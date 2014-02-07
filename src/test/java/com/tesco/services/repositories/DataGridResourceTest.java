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
        dataGridResource = new DataGridResourceForTest(new Configuration());
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

        int storeId = 2002;
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
    public void shouldGetStoreRefreshCache(){
        Cache<Integer, Store> storeRefreshCache = dataGridResource.getStoreRefreshCache();
        assertThat(storeRefreshCache.isEmpty()).isTrue();

        int storeId = 2002;
        Store store = new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP");
        storeRefreshCache.put(storeId, store);

        assertThat(storeRefreshCache.get(storeId)).isEqualTo(store);

        assertThat(dataGridResource.getStoreRefreshCache().isEmpty()).isTrue();
    }

    @Test
    public void shouldReplaceCurrentProductPriceCacheWithRefresh() throws Exception {
        initPromotionCaches();
        initStoreCaches();

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
    public void shouldReplaceCurrentStoreCacheWithRefresh() throws Exception {
        initPromotionCaches();
        initProductPriceCaches();

        Cache<Integer, Store> storeCache = dataGridResource.getStoreCache();
        Cache<Integer, Store> storeRefreshCache = dataGridResource.getStoreRefreshCache();

        int storeId = 2002;
        Store store = new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP");
        storeRefreshCache.put(storeId, store);

        dataGridResource.replaceCurrentWithRefresh();

        assertThat(storeCache.getStatus()).isEqualTo(ComponentStatus.TERMINATED);

        Cache<Integer, Store> newStoreCache = dataGridResource.getStoreCache();
        assertThat(newStoreCache.get(storeId)).isSameAs(store);
    }

    @Test
    public void shouldReplaceCurrentPromotionCacheWithRefresh() throws Exception {
        initProductPriceCaches();
        initStoreCaches();

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

    private void initPromotionCaches() {
        dataGridResource.getPromotionCache();
        dataGridResource.getPromotionRefreshCache();
    }

    private void initStoreCaches() {
        dataGridResource.getStoreCache();
        dataGridResource.getStoreRefreshCache();
    }

    private void initProductPriceCaches() {
        dataGridResource.getProductPriceCache();
        dataGridResource.getProductPriceRefreshCache();
    }

//
//    @Test
//    public void dgTest() throws IOException {
//        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("temp.csv")));
//        bufferedWriter.write("==========================\n");
//        bufferedWriter.write(System.currentTimeMillis() + "\n");
//        bufferedWriter.write("==========================\n");
//
//        final Cache<String, String> temp = dataGridResource.getTemp();
//        System.out.println(temp.size());
//        final int capacity = 1000000;
//        for (int i = 0; i < capacity; i++) {
//            if (i % 10000 == 0) {
//                int mb = 1024 * 1024;
//                Runtime runtime = Runtime.getRuntime();
//                long total = runtime.totalMemory();
//                long free = runtime.freeMemory();
//
//                bufferedWriter.write("{ '" + (total - free) / mb + "MB'  }");
//
//                System.out.println("One thousands puts over");
//            }
//            temp.put(String.format("Key:%d", i), String.format("Value:%d", i));
//        }
//
//        bufferedWriter.write("==========================\n");
//        bufferedWriter.write(System.currentTimeMillis() + "\n");
//        bufferedWriter.write("==========================\n");
//
//
//        int j = 0;
//        for (int i = 0; i < capacity; i++) {
//            final String key = String.format("Key:%d", i);
//            bufferedWriter.write(String.format("%s,%s\n", key, temp.get(key)));
//
//        }
//
//        bufferedWriter.write("==========================\n");
//        bufferedWriter.write(System.currentTimeMillis() + "\n");
//        bufferedWriter.write("==========================\n");
//
//        bufferedWriter.close();
//
//    }
//
//    @Test
//    public void productPriceCacheTest() throws IOException {
//        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("product.csv")));
//
//        bufferedWriter.write("==========================\n");
//        bufferedWriter.write(System.currentTimeMillis() + "\n");
//        bufferedWriter.write("==========================\n");
//
//        final Cache<String, Product> temp = dataGridResource.getProductPriceCache();
//        final int capacity = 1000000;
//        for (int i = 0; i < capacity; i++) {
//            if (i % 10000 == 0) {
//                int mb = 1024 * 1024;
//                Runtime runtime = Runtime.getRuntime();
//                long total = runtime.totalMemory();
//                long free = runtime.freeMemory();
//
//                bufferedWriter.write("{ '" + (total - free) / mb + "MB'  }");
//
//                System.out.println("Ten thousand puts over");
//            }
//
//            final String tpnb = String.format("TPNB:%d", i);
//            final String tpnc1 = String.format("TPNC1:%d", i);
//            final String tpnc2 = String.format("TPNC2:%d", i);
//            temp.put(tpnb, createProductWithVariants(tpnb, tpnc1, tpnc2));
//        }
//
//        bufferedWriter.write("==========================\n");
//        bufferedWriter.write(System.currentTimeMillis() + "\n");
//        bufferedWriter.write("==========================\n");
//
//
//        for (int i = 0; i < capacity; i++) {
//            final String tpnb = String.format("TPNB:%d", i);
//            bufferedWriter.write(String.format("%s,%s\n", tpnb, temp.get(tpnb)));
//
//        }
//
//
//        bufferedWriter.write("==========================\n");
//        bufferedWriter.write(System.currentTimeMillis() + "\n");
//        bufferedWriter.write("==========================\n");
//
//        bufferedWriter.close();
//
//    }
}
