package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.testutils.*;
import com.tesco.services.Configuration;
import com.tesco.services.IntegrationTest;
import com.tesco.services.core.Product;
import com.tesco.services.resources.TestConfiguration;
import net.spy.memcached.internal.OperationFuture;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.HashMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class ProductRepositoryTest {
    private String tpnb = "123455";
    private Product product;
    private ProductRepository productRepository;

    private CouchbaseTestManager couchbaseTestManager;
    @Mock
    private CouchbaseWrapper couchbaseWrapper;
    @Mock
    private OperationFuture<?> operationFuture;
    private AsyncReadWriteProductRepository readWriteProductRepository;
    private Optional<Integer> absent;
    @Mock
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
       // productRepository = new ProductRepository(new CouchbaseConnectionManager(new TestConfiguration()).getCouchbaseClient());
        product = new Product(tpnb);

        String bucketName = "PriceService";//should be name.getMethodName();
        Configuration testConfiguration = TestConfiguration.load().withBucketName(bucketName);

        if (testConfiguration.isDummyCouchbaseMode()){
            HashMap<String, ImmutablePair<Long, String>> fakeBase = new HashMap<>();
            couchbaseTestManager = new CouchbaseTestManager(new CouchbaseWrapperStub(fakeBase),
                    new AsyncCouchbaseWrapperStub(fakeBase),
                    mock(BucketTool.class));
        } else {
            couchbaseTestManager = new CouchbaseTestManager(testConfiguration.getCouchbaseBucket(),
                    testConfiguration.getCouchbaseUsername(),
                    testConfiguration.getCouchbasePassword(),
                    testConfiguration.getCouchbaseNodes(),
                    testConfiguration.getCouchbaseAdminUsername(),
                    testConfiguration.getCouchbaseAdminPassword());
        }

        couchbaseWrapper = couchbaseTestManager.getCouchbaseWrapper();
        asyncCouchbaseWrapper = couchbaseTestManager.getAsyncCouchbaseWrapper();

        mapper = new ObjectMapper();
        productRepository = new ProductRepository(this.couchbaseWrapper,asyncCouchbaseWrapper, mapper);
        //readWriteProductRepository = new AsyncReadWriteProductRepository(this.asyncCouchbaseWrapper, mapper);

        absent = Optional.absent();


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

   // @Test
    public void shouldNamespacePrefixKey() {
        final CouchbaseClient couchbaseClientMock = mock(CouchbaseClient.class);
        productRepository = new ProductRepository(couchbaseClientMock);
        final InOrder inOrder = inOrder(couchbaseClientMock);

        productRepository.put(product);
        productRepository.getByTPNB(tpnb);
        inOrder.verify(couchbaseClientMock).set("PRODUCT_" + tpnb, product);
        inOrder.verify(couchbaseClientMock).get("PRODUCT_" + tpnb);
    }

   @Test
    public void shouldCacheProductByTPNBUsingAsynCode() throws Exception {
        TestListener<Void, Exception> listener = new TestListener<>();
        TestListener<Product, Exception> productListner = new TestListener<>();

        productRepository.insertProduct(product,listener);
       // productRepository.put(product);
        productRepository.getProductByTPNB(tpnb,productListner);
        assertThat(productRepository.getProductIdentified().getTPNB()).isEqualTo(product.getTPNB());
    }

    @Test
    public void shouldReturnNullObjectWhenProductIsNotFoundUsingAsynCode() throws Exception {
        TestListener<Product, Exception> productListner = new TestListener<>();
        productRepository.getProductByTPNB("12345",productListner);
        Product product = productRepository.getProductIdentified();
        if(product == null){
            System.out.println("Product not found");
        }
    }

}
