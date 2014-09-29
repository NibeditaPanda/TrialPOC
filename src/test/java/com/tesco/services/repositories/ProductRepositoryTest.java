package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.InvalidViewException;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewDesign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.testutils.*;
import com.tesco.services.Configuration;
import com.tesco.services.core.Product;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.SaleInfo;
import com.tesco.services.exceptions.ItemNotFoundException;
import com.tesco.services.resources.TestConfiguration;
import com.tesco.services.utility.Dockyard;
import net.spy.memcached.internal.OperationFuture;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class ProductRepositoryTest /*extends IntegrationTest*/{
    private static final Logger logger = LoggerFactory.getLogger(ProductRepositoryTest.class);

    private String tpnb = "123455";
    private Product product;
    private ProductRepository productRepository;

    private CouchbaseTestManager couchbaseTestManager;
    @Mock
    private CouchbaseWrapper couchbaseWrapper;
    @Mock
    private OperationFuture<?> operationFuture;
    private Optional<Integer> absent;
    @Mock
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;
    private ObjectMapper mapper;

    private Configuration testConfiguration;
    private ProductRepository productRepository1;
    private CouchbaseClient couchbaseClient;

    @Before
    public void setUp() throws IOException,URISyntaxException,InterruptedException {
        product = new Product(tpnb);

        testConfiguration = TestConfiguration.load();

        if (testConfiguration.isDummyCouchbaseMode()){
            Map<String, ImmutablePair<Long, String>> fakeBase = new HashMap<>();
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
        couchbaseClient = new CouchbaseConnectionManager(testConfiguration).getCouchbaseClient();

        absent = Optional.absent();


    }

    @Test
    public void shouldCacheProductByTPNB() throws ItemNotFoundException {
        product.setLast_updated_date("20140806");
        productRepository.put(product);
        assertThat(productRepository.getByTPNB(tpnb).get()).isEqualTo(product);
    }

    @Test
    public void shouldReturnNullObjectWhenProductIsNotFound() throws ItemNotFoundException {
        assertThat(productRepository.getByTPNB("12345").isPresent()).isFalse();
    }

    @Test
    public void shouldNamespacePrefixKey() throws JsonProcessingException{
        final CouchbaseWrapper couchbaseClientMock = mock(CouchbaseWrapper.class);
        productRepository = new ProductRepository(couchbaseClientMock,asyncCouchbaseWrapper,mapper);
        final InOrder inOrder = inOrder(couchbaseClientMock);

        productRepository.put(product);
        productRepository.getByTPNB(tpnb);

            String productJson = mapper.writeValueAsString(product);
            inOrder.verify(couchbaseClientMock).set("PRODUCT_" + tpnb, productJson);
            inOrder.verify(couchbaseClientMock).get("PRODUCT_" + tpnb);

    }

    @Test
    public void shouldCacheProductByTPNBUsingAsynCode() throws ItemNotFoundException {
        TestListener<Void, Exception> listener = new TestListener<>();
        TestListener<Product, Exception> productListner = new TestListener<>();

        productRepository.insertProduct(product, listener);
        productRepository.getProductByTPNB(tpnb, productListner);
        assertThat(productRepository.getProductIdentified().getTPNB()).isEqualTo(product.getTPNB());
    }

    @Test
    public void shouldReturnNullObjectWhenProductIsNotFoundUsingAsynCode() throws ItemNotFoundException {
        TestListener<Product, Exception> productListner = new TestListener<>();
        productRepository.getProductByTPNB("12345",productListner);
        Product identifiedProduct = productRepository.getProductIdentified();
        if(identifiedProduct == null){
            logger.info("Product not found");
        }
    }

    @Test
    public void mapTPNCtoTPNBwithAsycCode(){
        String tpnc = "271871871";
        String tpnB = "056428171";
        productRepository.mapTPNC_TPNB(tpnc,tpnB);
        String mappedTPNCtoTPNB = (String) couchbaseWrapper.get(tpnc);
        mappedTPNCtoTPNB = Dockyard.replaceOldValCharWithNewVal(mappedTPNCtoTPNB, "\"", "");
        String mappedTPNBtoTPNC = (String) couchbaseWrapper.get(tpnB);
        mappedTPNBtoTPNC = Dockyard.replaceOldValCharWithNewVal(mappedTPNBtoTPNC, "\"", "");

        assertEquals(mappedTPNCtoTPNB, tpnB);
        assertEquals(mappedTPNBtoTPNC, tpnc);


    }

    /*Added by Surya for PS-114. This Junit will Delete the elements from CB -  Start*/

    @Ignore
    @Test
    public void deleteTPNBToTPNCToVaraintDependencies() throws Exception {

        String tpnc = "271871871";
        String tpnB = "056428171";
        String variant = "056428171-001";
        String tpncForVar ="271871872";

        String variant2 = "056428171-002";
        String tpncForVar2 ="271871873";

        Product product1 = new Product(tpnB);
        ProductVariant productVariant1 = createProductVariant(tpnc,1,"1.15",null);
        ProductVariant productVariant2 = createProductVariant(tpncForVar,1,"1.18",null);
        ProductVariant productVariant3 = createProductVariant(tpncForVar2,1,"1.19",null);

        product1.addProductVariant(productVariant1);
        product1.addProductVariant(productVariant2);
        product1.addProductVariant(productVariant3);

        String lastUpdateDateKey ="";
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-(testConfiguration.getLastUpdatedPurgeDays()));
        lastUpdateDateKey = dateFormat.format(cal.getTime());
        product1.setLast_updated_date(lastUpdateDateKey);

        String productJson = mapper.writeValueAsString(product1);
        couchbaseClient.set(getProductKey(tpnB), productJson);

        couchbaseClient.set(tpnc, mapper.writeValueAsString(tpnB));
        couchbaseClient.set(tpnB, mapper.writeValueAsString(tpnc));

        couchbaseClient.set(tpncForVar, mapper.writeValueAsString(variant));
        couchbaseClient.set(variant, mapper.writeValueAsString(tpncForVar));

        couchbaseClient.set(tpncForVar2, mapper.writeValueAsString(variant2));
        couchbaseClient.set(variant2, mapper.writeValueAsString(tpncForVar2));
        checkforView(couchbaseClient,testConfiguration);

        productRepository.purgeUnUpdatedItems(couchbaseClient,testConfiguration);

        /* assertNull function checks if the object is null;
        In this case if the object is null then the document is deleted and the test passes*/

        assertNull(productRepository.getMappedTPNCorTPNBWithCouchBaseClient(tpnc,couchbaseClient));
        assertNull(productRepository.getMappedTPNCorTPNBWithCouchBaseClient(tpnB,couchbaseClient));
        assertNull(productRepository.getMappedTPNCorTPNBWithCouchBaseClient(tpncForVar,couchbaseClient));
        assertNull(productRepository.getMappedTPNCorTPNBWithCouchBaseClient(variant,couchbaseClient));
        assertNull(productRepository.getMappedTPNCorTPNBWithCouchBaseClient(tpncForVar2,couchbaseClient));
        assertNull(productRepository.getMappedTPNCorTPNBWithCouchBaseClient(variant2,couchbaseClient));

    }

    private ProductVariant createProductVariant(String tpnc, int zoneId, String price, Promotion promotion) {
        ProductVariant productVariant = new ProductVariant(tpnc);
        SaleInfo saleInfo = new SaleInfo(zoneId, price);
        if (promotion != null) saleInfo.addPromotion(promotion);
        productVariant.addSaleInfo(saleInfo);
        return productVariant;
    }
    private String getProductKey(String tpnb) {
        return String.format("PRODUCT_%s", tpnb);
    }

    /*Added by Surya for PS-114. This Junit will Delete the elements from CB -  End*/
    /*Added by Surya for View check to avoid Junit Failures - Start*/
    private void createView(Configuration configuration, CouchbaseClient couchbaseClient)throws Exception{
        final DesignDocument designDoc = new DesignDocument(configuration.getCouchBaseDesignDocName());
        designDoc.setView(new ViewDesign(configuration.getCouchBaseViewName(),
                "function (doc, meta) {\n" +
                        "  if (doc.last_updated_date && meta.type == \"json\" ) {\n" +
                        "    emit(doc.last_updated_date, {KEY: meta.id});\n" +
                        "  }\n" +
                        "}"
        ));

        couchbaseClient.createDesignDoc(designDoc);
    }
    public void checkforView(CouchbaseClient couchbaseClient, Configuration configuration) throws Exception {
        try {
            View view = couchbaseClient.getView(configuration.getCouchBaseDesignDocName(), configuration.getCouchBaseViewName());
        }catch(InvalidViewException e){
            createView(configuration, couchbaseClient);
            Thread.sleep(50);
        }
    }
    /*Added by Surya for View check to avoid Junit Failures - End*/
}
