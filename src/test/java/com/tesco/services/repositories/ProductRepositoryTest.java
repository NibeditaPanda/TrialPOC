package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.ViewDesign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.listeners.CreateDesignDocListener;
import com.tesco.couchbase.listeners.DeleteListener;
import com.tesco.couchbase.listeners.Listener;
import com.tesco.couchbase.testutils.*;
import com.tesco.services.Configuration;
import com.tesco.services.IntegrationTest;
import com.tesco.services.core.Product;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.SaleInfo;
import com.tesco.services.resources.TestConfiguration;
import com.tesco.services.utility.Dockyard;
import net.spy.memcached.internal.OperationFuture;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class ProductRepositoryTest /*extends IntegrationTest*/{
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

   private Configuration testConfiguration;
   private ProductRepository productRepository1;

    @Before
    public void setUp() throws Exception {
      //  productRepository = new ProductRepository(new CouchbaseConnectionManager(new TestConfiguration()).getCouchbaseClient());
        product = new Product(tpnb);

       // String bucketName = "PriceService";//should be name.getMethodName();
        testConfiguration = TestConfiguration.load();

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
        productRepository1 = new ProductRepository(testConfiguration);
        //readWriteProductRepository = new AsyncReadWriteProductRepository(this.asyncCouchbaseWrapper, mapper);

        absent = Optional.absent();


    }

    @Test
    public void shouldCacheProductByTPNB() throws Exception {
        product.setLast_updated_date("20140806");
        productRepository.put(product);
        assertThat(productRepository.getByTPNB(tpnb).get()).isEqualTo(product);
    }

    @Test
    public void shouldReturnNullObjectWhenProductIsNotFound() throws Exception {
        assertThat(productRepository.getByTPNB("12345").isPresent()).isFalse();
    }

    @Test
    public void shouldNamespacePrefixKey() {
        final CouchbaseWrapper couchbaseClientMock = mock(CouchbaseWrapper.class);
        productRepository = new ProductRepository(couchbaseClientMock,asyncCouchbaseWrapper,mapper);
        final InOrder inOrder = inOrder(couchbaseClientMock);

        productRepository.put(product);
        productRepository.getByTPNB(tpnb);
        try {
            String productJson = mapper.writeValueAsString(product);
            inOrder.verify(couchbaseClientMock).set("PRODUCT_" + tpnb, productJson);
            inOrder.verify(couchbaseClientMock).get("PRODUCT_" + tpnb);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

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
    @Test
    public void mapTPNCtpTPNB(){
        String TPNC = "271871871";
        String TPNB = "056428171";
        couchbaseWrapper.set(TPNC, TPNB);
        String mappedTPNCtoTPNB = (String) couchbaseWrapper.get(TPNC);
        assertThat(mappedTPNCtoTPNB.equals(TPNB));

    }
    @Test
    public void mapTPNC_TPNB(){
        String TPNC = "271871871";
        String TPNB = "056428171";
        couchbaseWrapper.set(TPNC, TPNB);
        String mappedTPNCtoTPNB = (String) couchbaseWrapper.get(TPNC);
        assertThat(mappedTPNCtoTPNB.equals(TPNB));

        couchbaseWrapper.set(TPNB, TPNC);
        String mappedTPNBtoTPNC = (String) couchbaseWrapper.get(TPNB);
        assertThat(mappedTPNCtoTPNB.equals(TPNC));

    }
/*Added by Surya for PS-114. This Junit will Delete the elements from CB -  Start*/
    @Test
    public void deleteTPNBToTPNCToVaraintDependencies() throws JsonProcessingException {
        String TPNC = "271871871";
        String TPNB = "056428171";
        String variant = "056428171-001";
        String TPNCForVar ="271871872";

        String variant2 = "056428171-002";
        String TPNCForVar2 ="271871873";

        Product product = new Product(TPNB);
        ProductVariant productVariant1 = createProductVariant(TPNC,1,"1.15",null);
        ProductVariant productVariant2 = createProductVariant(TPNCForVar,1,"1.18",null);
        ProductVariant productVariant3 = createProductVariant(TPNCForVar2,1,"1.19",null);

        product.addProductVariant(productVariant1);
        product.addProductVariant(productVariant2);
        product.addProductVariant(productVariant3);

        String productJson = mapper.writeValueAsString(product);
        couchbaseWrapper.set(getProductKey(TPNB), productJson);

        couchbaseWrapper.set(TPNC, TPNB);
        couchbaseWrapper.set(TPNB, TPNC);

        couchbaseWrapper.set(TPNCForVar, variant);
        couchbaseWrapper.set(variant, TPNCForVar);

        couchbaseWrapper.set(TPNCForVar2, variant2);
        couchbaseWrapper.set(variant2, TPNCForVar2);

        productRepository.delete_TPNB_TPNC_VAR(getProductKey(TPNB));

        assertThat(productRepository.getByTPNB(TPNB).isPresent()).isFalse();

        if(productRepository.getMappedTPNCorTPNB(TPNC) == null) {
            assertThat(productRepository.getMappedTPNCorTPNB(TPNC));
        }else{
            assertThat(productRepository.getMappedTPNCorTPNB(TPNC).isEmpty());
        }
        if(productRepository.getMappedTPNCorTPNB(TPNB) == null) {
            assertThat(productRepository.getMappedTPNCorTPNB(TPNB));
        }else{
            assertThat(productRepository.getMappedTPNCorTPNB(TPNB).isEmpty());
        }
        if(productRepository.getMappedTPNCorTPNB(TPNCForVar) == null) {
            assertThat(productRepository.getMappedTPNCorTPNB(TPNCForVar));
        }else{
            assertThat(productRepository.getMappedTPNCorTPNB(TPNCForVar).isEmpty());
        }
        if(productRepository.getMappedTPNCorTPNB(variant) == null) {
            assertThat(productRepository.getMappedTPNCorTPNB(variant));
        }else{
            assertThat(productRepository.getMappedTPNCorTPNB(variant).isEmpty());
        }
        if(productRepository.getMappedTPNCorTPNB(TPNCForVar2) == null) {
            assertThat(productRepository.getMappedTPNCorTPNB(TPNCForVar2));
        }else{
            assertThat(productRepository.getMappedTPNCorTPNB(TPNCForVar2).isEmpty());
        }
        if(productRepository.getMappedTPNCorTPNB(variant2) == null) {
            assertThat(productRepository.getMappedTPNCorTPNB(variant2));
        }else{
            assertThat(productRepository.getMappedTPNCorTPNB(variant2).isEmpty());
        }


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
      @Ignore
       @Test
       public void testView() throws JsonProcessingException {
            TestListener<Void, Exception> listener = new TestListener<>();
             createView(listener);

           String last_update_date_key ="";
           DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
           Calendar cal = Calendar.getInstance();
           cal.add(Calendar.DATE,-(testConfiguration.getLastUpdatedPurgeDays()));
           last_update_date_key = dateFormat.format(cal.getTime());
           Product product = new Product(tpnb);
           product.setLast_updated_date(last_update_date_key);
           couchbaseWrapper.set(getProductKey(tpnb),mapper.writeValueAsString(product));
           productRepository.getViewResult(listener);
           assertThat(productRepository.getByTPNB(tpnb).isPresent()).isFalse();


       }
    private void createView(final Listener<Void, Exception> listener) {
        final DesignDocument designDoc = new DesignDocument(testConfiguration.getCouchBaseDesignDocName());
        designDoc.setView(new ViewDesign(testConfiguration.getCouchBaseViewName(),
                "function (doc, meta) {\n" +
                        "  if (doc.last_updated_date) {\n" +
                        "    emit(doc.last_updated_date, {KEY: meta.id});\n" +
                        "  }\n" +
                        "}"
        ));

        asyncCouchbaseWrapper.createDesignDoc(designDoc, new CreateDesignDocListener(asyncCouchbaseWrapper, designDoc) {
            @Override
            public void process() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                listener.onComplete(null);
            }

            @Override
            public void onException(Exception e) {
                listener.onException(e);
            }
        });
    }
}
