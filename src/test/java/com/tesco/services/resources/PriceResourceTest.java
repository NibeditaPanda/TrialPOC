package com.tesco.services.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.testutils.AsyncCouchbaseWrapperStub;
import com.tesco.couchbase.testutils.BucketTool;
import com.tesco.couchbase.testutils.CouchbaseTestManager;
import com.tesco.couchbase.testutils.CouchbaseWrapperStub;
import com.tesco.services.Configuration;
import com.tesco.services.IntegrationTest;
import com.tesco.services.builder.PromotionBuilder;
import com.tesco.services.core.Product;
import com.tesco.services.resources.model.ProductPriceBuilder;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.SaleInfo;
import com.tesco.services.core.Store;
import com.tesco.services.exceptions.ItemNotFoundException;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.repositories.ProductRepository;
import com.tesco.services.repositories.StoreRepository;
import com.yammer.dropwizard.testing.ResourceTest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PriceResourceTest extends ResourceTest {

    private static Configuration testConfiguration ;
    private CouchbaseConnectionManager couchbaseConnectionManager;
    private CouchbaseTestManager couchbaseTestManager;
   private CouchbaseWrapper couchbaseWrapper;
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;
    private ObjectMapper mapper ;
    private ProductRepository productRepository;
    private StoreRepository storeRepository;

    @Override
    protected void setUpResources() throws Exception {
       // couchbaseConnectionManager = new CouchbaseConnectionManager(testConfiguration);
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
        storeRepository = new StoreRepository(this.couchbaseWrapper,asyncCouchbaseWrapper, mapper);

        PriceResource priceResource = new PriceResource(couchbaseWrapper,asyncCouchbaseWrapper,mapper);
        addResource(priceResource);
    }

    /*@BeforeClass
    public static void setUp() throws IOException, URISyntaxException, InterruptedException {
      IntegrationTest.init();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        IntegrationTest.destroy();
    }*/

    // Couchbase tests
    // ==============
    @Test
    public void shouldReturnNationalPricesForMultipleItemsWhenStoreIdIsNotSpecified() throws IOException, ItemNotFoundException {
        /*ProductRepository productRepository = new ProductRepository(couchbaseConnectionManager.getCouchbaseClient());*/
        //ProductRepository productRepository = getProductRepository();
        String tpnb = "050925811";
        String tpnc1 = "266072275";
        String tpnc2 = "266072276";
        Product product = createProductWithVariants(tpnb, tpnc1, tpnc2);
        productRepository.put(product);

        WebResource resource = client().resource(String.format("/price/B/%s", tpnb));

        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
        Map actualProductPriceInfo = resource.get(Map.class);

        compareResponseMaps(actualProductPriceInfo, expectedProductPriceInfo(tpnb, tpnc1, tpnc2));
    }

    private void compareResponseMaps(Map actualProductPriceInfo, Map<String, Object> expectedProductPriceInfo) {
        assertThat(actualProductPriceInfo.size()).isEqualTo(expectedProductPriceInfo.size());
        assertThat(actualProductPriceInfo.get(ProductPriceBuilder.TPNB)).isEqualTo(expectedProductPriceInfo.get(ProductPriceBuilder.TPNB));

        final Set actualVariants = new HashSet((Collection) actualProductPriceInfo.get(ProductPriceBuilder.VARIANTS));
        final Set expectedVariants = new HashSet((Collection) expectedProductPriceInfo.get(ProductPriceBuilder.VARIANTS));
        assertThat(actualVariants).isEqualTo(expectedVariants);
    }

    @Test
    public void shouldReturnPricesWhenStoreIdIsSpecified() throws IOException, ItemNotFoundException {
      //  ProductRepository productRepository = new ProductRepository(couchbaseConnectionManager.getCouchbaseClient());

        String tpnb = "050925811";
        String tpnc1 = "266072275";
        String tpnc2 = "266072276";
        Product product = createProductWithVariants(tpnb, tpnc1, tpnc2);
        productRepository.put(product);

        String storeId = "2002";
        storeRepository.put(new Store(storeId, Optional.of(6), Optional.of(14), "EUR"));

        WebResource resource = client().resource(String.format("/price/B/%s?store=%s", tpnb, storeId));

        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
        Map actualProductPriceInfo = resource.get(Map.class);

        ArrayList<Map<String, Object>> variants = new ArrayList<>();
        variants.add(getVariantInfo(tpnc1, "EUR", null, "1.10", false));
        variants.add(getVariantInfo(tpnc2, "EUR", "1.38", null, false));

        compareResponseMaps(actualProductPriceInfo, getProductPriceMap(tpnb, variants));
    }

    @Test
    public void shouldReturn404WhenItemIsNotFound() throws ItemNotFoundException {
        WebResource resource = client().resource("/price/B/non_existent_item");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Product not found");
    }

    @Test
    public void shouldReturn404WhenStoreIsNotFound() throws Exception {
       // ProductRepository productRepository = new ProductRepository(couchbaseConnectionManager.getCouchbaseClient());
        productRepository.put(createProductWithVariants("050925811", "266072275", "266072276"));

        WebResource resource = client().resource("/price/B/050925811?store=2099");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Store not found");
    }

    @Test
    public void shouldReturn404WhenStoreIsInvalid() throws Exception {
      //  ProductRepository productRepository = new ProductRepository(couchbaseConnectionManager.getCouchbaseClient());
        productRepository.put(createProductWithVariants("050925811", "266072275", "266072276"));

        WebResource resource = client().resource("/price/B/050925811?store=invalidstore");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Store not found");
    }

    @Test
    public void shouldReturn400WhenIncorrectQueryParamIsGiven() throws Exception {
        WebResource resource = client().resource("/price/B/050925811?storee=store");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).contains("Invalid request");
    }

    private Product createProductWithVariants(String tpnb, String tpnc1, String tpnc2) {
        ProductVariant productVariant1 = new ProductVariant(tpnc1);
        productVariant1.addSaleInfo(new SaleInfo(1, "1.40"));
        SaleInfo promoSaleInfo = new SaleInfo(5, "1.20");
        promoSaleInfo.addPromotion(createPromotion("A30718670"));
        productVariant1.addSaleInfo(promoSaleInfo);
        productVariant1.addSaleInfo(new SaleInfo(14, "1.10"));

        ProductVariant productVariant2 = new ProductVariant(tpnc2);
        productVariant2.addSaleInfo(new SaleInfo(1, "1.39"));
        productVariant2.addSaleInfo(new SaleInfo(6, "1.38"));

        Product product = new Product(tpnb);
        product.addProductVariant(productVariant1);
        product.addProductVariant(productVariant2);

        return product;
    }

    private Promotion createPromotion(String offerId) {
        return new PromotionBuilder().
                offerId(offerId).
                offerName("Test Offer Name " + offerId).
                startDate("20130729").
                endDate("20130819").
                description1("Test Description 1 " + offerId).
                description2("Test Description 2 " + offerId).
                createPromotion();
    }

    private Map<String, Object> expectedProductPriceInfo(String tpnb, String tpnc1, String tpnc2) {
        ArrayList<Map<String, Object>> variants = new ArrayList<>();
        variants.add(getVariantInfo(tpnc1, "GBP", "1.40", "1.20", true));
        variants.add(getVariantInfo(tpnc2, "GBP", "1.39", null, true));

        return getProductPriceMap(tpnb, variants);
    }

    private Map<String, Object> getProductPriceMap(String tpnb, ArrayList<Map<String, Object>> variants) {
        Map<String, Object> productPriceMap = new LinkedHashMap<>();
        productPriceMap.put("tpnb", tpnb);
        productPriceMap.put("variants", variants);
        return productPriceMap;
    }

    private Map<String, Object> getVariantInfo(String tpnc, String currency, String price, String promoPrice, boolean shouldAddPromotionInfo) {
        Map<String, Object> variantInfo1 = new LinkedHashMap<>();
        variantInfo1.put("tpnc", tpnc);
        variantInfo1.put("currency", currency);
        if (price != null) variantInfo1.put("price", price);
        if (promoPrice != null) {
            variantInfo1.put("promoPrice", promoPrice);
            if (shouldAddPromotionInfo) {
                ArrayList<Object> promotions = new ArrayList<>();
                promotions.add(createPromotionInfo("A30718670"));
                variantInfo1.put("promotions", promotions);
            }
        }
        return variantInfo1;
    }

    private Map<String, String> createPromotionInfo(String offerId) {
        Promotion promotion = createPromotion(offerId);
        Map<String, String> promotionInfo = new LinkedHashMap<>();
        promotionInfo.put("offerName", promotion.getOfferName());
        promotionInfo.put("effectiveDate", promotion.getEffectiveDate());
        promotionInfo.put("endDate", promotion.getEndDate());
        promotionInfo.put("customerFriendlyDescription1", promotion.getCFDescription1());
        promotionInfo.put("customerFriendlyDescription2", promotion.getCFDescription2());
        return promotionInfo;
    }


}
