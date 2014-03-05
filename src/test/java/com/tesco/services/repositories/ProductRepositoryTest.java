package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.tesco.services.IntegrationTest;
import com.tesco.services.core.Product;
import com.tesco.services.resources.TestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class ProductRepositoryTest extends IntegrationTest {
    private String tpnb = "123455";
    private Product product;
    private ProductRepository productRepository;

    @Before
    public void setUp() throws Exception {
        productRepository = new ProductRepository(new CouchbaseConnectionManager(new TestConfiguration()).getCouchbaseClient());
        product = new Product(tpnb);
    }

    @Test
    public void shouldCacheProductByTPNB() throws Exception {
        productRepository.put(product);
        assertThat(productRepository.getByTPNB(tpnb).get()).isEqualTo(product);
    }

    @Test
    public void shouldReturnNullObjectWhenProductIsNotFound() throws Exception {
        assertThat(productRepository.getByTPNB("12345").isPresent()).isFalse();
    }

    @Test
    public void shouldNamespacePrefixKey() {
        final CouchbaseClient couchbaseClientMock = mock(CouchbaseClient.class);
        productRepository = new ProductRepository(couchbaseClientMock);
        final InOrder inOrder = inOrder(couchbaseClientMock);

        productRepository.put(product);
        productRepository.getByTPNB(tpnb);
        inOrder.verify(couchbaseClientMock).set("PRODUCT_" + tpnb, product);
        inOrder.verify(couchbaseClientMock).get("PRODUCT_" + tpnb);
    }
}
