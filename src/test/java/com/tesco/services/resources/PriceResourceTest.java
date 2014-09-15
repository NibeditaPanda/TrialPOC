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
import com.tesco.services.utility.Dockyard;
import com.yammer.dropwizard.testing.ResourceTest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
@RunWith(MockitoJUnitRunner.class)
public class PriceResourceTest extends ResourceTest {

    private static Configuration testConfiguration ;
    private CouchbaseConnectionManager couchbaseConnectionManager;
    private CouchbaseTestManager couchbaseTestManager;
    private CouchbaseWrapper couchbaseWrapper;
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;
    private ObjectMapper mapper ;
    private ProductRepository productRepository;
    private StoreRepository storeRepository;
    private static String SELLING_UOM = "sellingUOM";
    private static String SELLING_UOM_VAL = "KG";

    @Override
    protected void setUpResources() throws Exception {
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
        storeRepository = new StoreRepository(this.couchbaseWrapper,asyncCouchbaseWrapper, mapper);

        PriceResource priceResource = new PriceResource(couchbaseWrapper,asyncCouchbaseWrapper,mapper);
        addResource(priceResource);
    }

    @Test
@Ignore
    public void shouldReturnNationalPricesForMultipleItemsWhenStoreIdIsNotSpecified() throws IOException, ItemNotFoundException {

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
    @Ignore
    public void shouldReturnPricesWhenStoreIdIsSpecified() throws IOException, ItemNotFoundException {

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

        List<Map<String, Object>> variants = new ArrayList<>();
        List<Map<String, String>> promotion = new ArrayList<>();
        /** PS-173 -salman :changed to add sellingUOM value to variant */
        variants.add(getVariantInfo(tpnc1, "EUR", null, "1.10","KG"));
        variants.add(getVariantInfo(tpnc2, "EUR", "1.38", null,"KG"));
        /* PS-118 -salman :changed to form the response according to IDL */
        promotion.add(createPromotionInfo("A30718670"));
        compareResponseMaps(actualProductPriceInfo, getProductPriceMap(tpnb, variants,promotion));
    }

    @Test
    @Ignore
    public void shouldReturn404WhenItemIsNotFound() throws ItemNotFoundException {
        WebResource resource = client().resource("/price/B/non_existent_item");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Product not found");
    }

    @Test
    @Ignore
    public void shouldReturn404WhenStoreIsNotFound() throws ItemNotFoundException {
        productRepository.put(createProductWithVariants("050925811", "266072275", "266072276"));

        WebResource resource = client().resource("/price/B/050925811?store=2099");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Store not found");
    }

    @Test
    @Ignore
    public void shouldReturn404WhenStoreIsInvalid() throws ItemNotFoundException {
        productRepository.put(createProductWithVariants("050925811", "266072275", "266072276"));

        WebResource resource = client().resource("/price/B/050925811?store=invalidstore");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Store not found");
    }

    @Test
    public void shouldReturn400WhenIncorrectQueryParamIsGiven() throws ItemNotFoundException {
        WebResource resource = client().resource("/price/B/050925811?storee=store");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).contains("Invalid request");
    }

    private Product createProductWithVariants(String tpnb, String tpnc1, String tpnc2) {
        ProductVariant productVariant1 = null;
        ProductVariant productVariant2 = null;
        /** PS-173 -salman :adding sellingUOM value to variant while product is built */
        /** PS-178 - Mukund :Modified the Price - Added extra decimals */

        if(!Dockyard.isSpaceOrNull(tpnc1)) {
            productVariant1 = new ProductVariant(tpnc1);
            productVariant1.setSellingUOM(SELLING_UOM_VAL);
            productVariant1.addSaleInfo(new SaleInfo(1, "1.40"));
            SaleInfo promoSaleInfo = new SaleInfo(5, "1.20");
            promoSaleInfo.addPromotion(createPromotion("A30718670"));
            productVariant1.addSaleInfo(promoSaleInfo);
            productVariant1.addSaleInfo(new SaleInfo(14, "1.10"));
        }
        if(!Dockyard.isSpaceOrNull(tpnc2)) {
            productVariant2 = new ProductVariant(tpnc2);
            productVariant2.setSellingUOM(SELLING_UOM_VAL);
            productVariant2.addSaleInfo(new SaleInfo(1, "1.39"));
            productVariant2.addSaleInfo(new SaleInfo(6, "1.38"));
        }
        Product product = new Product(tpnb);
        if(!Dockyard.isSpaceOrNull(productVariant1)) {
            product.addProductVariant(productVariant1);
        }
        if(!Dockyard.isSpaceOrNull(productVariant2)) {
            product.addProductVariant(productVariant2);
        }
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
        List<Map<String, Object>> variants = new ArrayList<>();
        /* PS-118 -salman :changed to form the response according to IDL */
        List<Map<String, String>> promotion=new ArrayList<>();

        if(!Dockyard.isSpaceOrNull(tpnc2)){
            variants.add(getVariantInfo(tpnc2, "GBP", "1.39", null,"KG"));
        }
        if(!Dockyard.isSpaceOrNull(tpnc1)) {
            variants.add(getVariantInfo(tpnc1, "GBP", "1.40", "1.20", "KG"));
        }
        /* PS-118 -salman :changed to form the response according to IDL */
        promotion.add(createPromotionInfo("A30718670"));

        return getProductPriceMap(tpnb, variants,promotion);
    }


    private Map<String, Object> getProductPriceMap(String tpnb, List<Map<String, Object>> variants, List<Map<String, String>> promotion) {
        Map<String, Object> productPriceMap = new LinkedHashMap<>();
        productPriceMap.put("tpnb", tpnb);
        productPriceMap.put("variants", variants);
        /* PS-118 -salman :changed to form the response according to IDL */
        productPriceMap.put("promotions", promotion);
        return productPriceMap;
    }
    /** PS-173 -salman :Added function parameter to  incorporate sellingUOM value */
    private Map<String, Object> getVariantInfo(String tpnc, String currency, String price, String promoPrice,String sellinguom) {
        Map<String, Object> variantInfo1 = new LinkedHashMap<>();
        variantInfo1.put("tpnc", tpnc);
        variantInfo1.put("currency",currency);
        variantInfo1.put(SELLING_UOM,sellinguom);
        if (price != null){
            variantInfo1.put("price", price);
        }
        variantInfo1.put("promoprice", promoPrice);
        /* PS-118 -salman :changed to form the response according to IDL */
        //salman deleted old code of adding promotion

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

    @Test
    @Ignore
    public void shouldReturnNationalPricesForMultipleItemsWhenStoreIdIsNotSpecifiedwithTPNC() throws IOException, ItemNotFoundException {
        String tpnb = "070461113";
        String tpnc = "284347092";
        String tpnc2 = null;
        Product product = createProductWithVariants(tpnb, tpnc, tpnc2);
        productRepository.put(product);
        if(!Dockyard.isSpaceOrNull(tpnb) && !Dockyard.isSpaceOrNull(tpnc)) {
            couchbaseWrapper.set(tpnb, tpnc);
            couchbaseWrapper.set(tpnc, tpnb);
        }
        if(!Dockyard.isSpaceOrNull(tpnb) && !Dockyard.isSpaceOrNull(tpnc2)) {
            couchbaseWrapper.set(tpnb, tpnc2);
            couchbaseWrapper.set(tpnc2, tpnb);
        }
        WebResource resource = client().resource(String.format("/price/C/%s", tpnc));

        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
        Map actualProductPriceInfo = resource.get(Map.class);

        compareResponseMaps(actualProductPriceInfo, expectedProductPriceInfo(tpnb, tpnc, tpnc2));
    }
    @Test
@Ignore
    public void shouldReturnNationalPricesForMultipleItemsWhenStoreIdIsNotSpecifiedwithTPNCToSTORE() throws IOException, ItemNotFoundException {
        String tpnb = "070461113";
        String tpnc1 = "284347092";
        String tpnc2 = null;
        Product product = createProductWithVariants(tpnb, tpnc1, tpnc2);
        productRepository.put(product);

        String storeId = "2002";
        storeRepository.put(new Store(storeId, Optional.of(5), Optional.of(14), "EUR"));

        if(!Dockyard.isSpaceOrNull(tpnb) && !Dockyard.isSpaceOrNull(tpnc1)) {
            couchbaseWrapper.set(tpnb, tpnc1);
            couchbaseWrapper.set(tpnc1, tpnb);
        }
        if(!Dockyard.isSpaceOrNull(tpnb) && !Dockyard.isSpaceOrNull(tpnc2)) {
            couchbaseWrapper.set(tpnb, tpnc2);
            couchbaseWrapper.set(tpnc2, tpnb);
        }
        WebResource resource = client().resource(String.format("/price/C/%s?store=%s", tpnc1, storeId));

        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
        Map actualProductPriceInfo = resource.get(Map.class);

        List<Map<String, Object>> variants = new ArrayList<>();
        /* PS-118 -salman :changed to form the response according to IDL */
        List<Map<String, String>> promotion = new ArrayList<>();

        if(!Dockyard.isSpaceOrNull(tpnc1)) {
            variants.add(getVariantInfo(tpnc1, "EUR", "1.20", "1.10", "KG"));
        }
        if(!Dockyard.isSpaceOrNull(tpnc2)) {
            variants.add(getVariantInfo(tpnc2, "EUR", "1.38", null, "KG"));
        }
        /* PS-118 -salman :changed to form the response according to IDL */
        promotion.add(createPromotionInfo("A30718670"));

        compareResponseMaps(actualProductPriceInfo, getProductPriceMap(tpnb, variants,promotion));
    }
    @Test
    @Ignore
    public void shouldReturn404WhenItemIsNotFoundGivenTPNC() throws ItemNotFoundException {

        WebResource resource = client().resource("/price/C/non_existent_item");

        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Product not found");
    }

    @Test
    @Ignore
    public void shouldReturn404WhenStoreIsNotFoundGivenTPNC() throws ItemNotFoundException {
        String tpnb = "070461113";
        String tpnc1 = "284347092";
        String tpnc2 = null;
        productRepository.put(createProductWithVariants(tpnb,tpnc1,tpnc2));
        if(!Dockyard.isSpaceOrNull(tpnb) && !Dockyard.isSpaceOrNull(tpnc1)) {
            couchbaseWrapper.set(tpnb, tpnc1);
            couchbaseWrapper.set(tpnc1, tpnb);
        }
        if(!Dockyard.isSpaceOrNull(tpnb) && !Dockyard.isSpaceOrNull(tpnc2)) {
            couchbaseWrapper.set(tpnb, tpnc2);
            couchbaseWrapper.set(tpnc2, tpnb);
        }
        WebResource resource = client().resource("/price/C/284347092?store=2099");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Store not found");
    }

    @Test
    @Ignore
    public void shouldReturn404WhenStoreIsInvalidGivenTPNC() throws ItemNotFoundException {
        String tpnb = "070461113";
        String tpnc1 = "284347092";
        String tpnc2 = null;
        productRepository.put(createProductWithVariants(tpnb,tpnc1,tpnc2));
        if(!Dockyard.isSpaceOrNull(tpnb) && !Dockyard.isSpaceOrNull(tpnc1)) {
            couchbaseWrapper.set(tpnb, tpnc1);
            couchbaseWrapper.set(tpnc1, tpnb);
        }
        if(!Dockyard.isSpaceOrNull(tpnb) && !Dockyard.isSpaceOrNull(tpnc2)) {
            couchbaseWrapper.set(tpnb, tpnc2);
            couchbaseWrapper.set(tpnc2, tpnb);
        }
        WebResource resource = client().resource("/price/C/284347092?store=invalidstore");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Store not found");
    }

    @Test
    public void shouldReturn400WhenIncorrectQueryParamIsGivenTPNC() throws ItemNotFoundException {
        WebResource resource = client().resource("/price/C/284347092?storee=store");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).contains("Invalid request");
    }
    public String getTPNBForTPNC(String tpnb) {
        return tpnb;
    }


}
