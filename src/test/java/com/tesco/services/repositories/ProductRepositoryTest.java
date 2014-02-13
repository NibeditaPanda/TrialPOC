package com.tesco.services.repositories;

import com.tesco.services.core.Product;
import com.tesco.services.resources.TestConfiguration;
import org.infinispan.Cache;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ProductRepositoryTest {
    private Cache<String,Product> productPriceCache;
    private DataGridResource dataGridResource;

    @Before
    public void setUp() throws Exception {
        dataGridResource = new DataGridResourceForTest(new TestConfiguration());
        productPriceCache = dataGridResource.getProductPriceCache();
        productPriceCache.clear();
    }

    @Test
    public void shouldCacheProductByTPNB() throws Exception {
        ProductRepository productRepository = new ProductRepository(productPriceCache);
        String tpnb = "123455";
        Product product = new Product(tpnb);
        productRepository.put(product);
        assertThat(productRepository.getByTPNB(tpnb).get()).isEqualTo(product);
    }

    @Test
    public void shouldReturnNullObjectWhenProductIsNotFound() throws Exception {
        ProductRepository productRepository = new ProductRepository(productPriceCache);
        String tpnb = "12345";
        assertThat(productRepository.getByTPNB(tpnb).isPresent()).isFalse();
    }
}
