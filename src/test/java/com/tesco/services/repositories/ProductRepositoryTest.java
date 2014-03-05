package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.tesco.services.IntegrationTest;
import com.tesco.services.core.Product;
import com.tesco.services.resources.TestConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ProductRepositoryTest extends IntegrationTest {
    private CouchbaseConnectionManager couchbaseConnectionManager;
    private CouchbaseClient couchbaseClient;

    @Before
    public void setUp() throws Exception {
        couchbaseConnectionManager = new CouchbaseConnectionManager(new TestConfiguration());
        couchbaseClient = couchbaseConnectionManager.getCouchbaseClient();
    }

    @Test
    public void shouldCacheProductByTPNB() throws Exception {
        ProductRepository productRepository = new ProductRepository(couchbaseClient);
        String tpnb = "123455";
        Product product = new Product(tpnb);
        productRepository.put(product);
        assertThat(productRepository.getByTPNB(tpnb).get()).isEqualTo(product);
    }

    @Test
    public void shouldReturnNullObjectWhenProductIsNotFound() throws Exception {
        ProductRepository productRepository = new ProductRepository(couchbaseClient);
        String tpnb = "12345";
        assertThat(productRepository.getByTPNB(tpnb).isPresent()).isFalse();
    }
}
