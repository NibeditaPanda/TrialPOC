package com.tesco.services.adapters.rpm.writers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.listeners.Listener;
import com.tesco.couchbase.testutils.AsyncCouchbaseWrapperStub;
import com.tesco.couchbase.testutils.BucketTool;
import com.tesco.couchbase.testutils.CouchbaseTestManager;
import com.tesco.couchbase.testutils.CouchbaseWrapperStub;
import com.tesco.services.Configuration;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.adapters.rpm.comparators.RPMComparator;
import com.tesco.services.adapters.rpm.readers.PriceServiceCSVReader;
import com.tesco.services.adapters.sonetto.SonettoPromotionXMLReader;
import com.tesco.services.builder.PromotionBuilder;
import com.tesco.services.core.Product;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.SaleInfo;
import com.tesco.services.core.Store;
import com.tesco.services.repositories.*;
import com.tesco.services.resources.TestConfiguration;
import net.spy.memcached.internal.OperationFuture;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.internal.matchers.CapturingMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.util.Lists.newArrayList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RPMWriterTest {
    private RPMWriter rpmWriter;

    @Mock
    private SonettoPromotionXMLReader sonettoPromotionXMLReader;

    @Mock
    private PriceServiceCSVReader rpmPriceReader;

    @Mock
    private PriceServiceCSVReader rpmPromoPriceReader;

    @Mock
    private PriceServiceCSVReader storeZoneReader;

    @Mock
    private PriceServiceCSVReader rpmPromotionReader;

    @Mock
    private PriceServiceCSVReader rpmPromotionDescReader;

    @Mock
    private UUIDGenerator uuidGenerator;

    @Mock
    public PromotionRepository promotionRepository;

    @Mock
    public ProductRepository productRepository;

    @Mock
    public StoreRepository storeRepository;

    @Mock
    public AsyncCouchbaseWrapper asyncCouchbaseWrapper;

    @Mock
    public CouchbaseWrapper couchbaseWrapper;

    @Mock
    public OperationFuture<?> operationFuture;
    @Mock
    private ObjectMapper mapper;
    private int zoneId = 1;

    @Before
    public void setUp() throws Exception {
        rpmWriter = new RPMWriter("./src/test/java/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml",
                sonettoPromotionXMLReader,
                promotionRepository,
                productRepository,
                storeRepository,
                rpmPriceReader,
                rpmPromoPriceReader,
                storeZoneReader,
                rpmPromotionReader,
                rpmPromotionDescReader);
        when(rpmPriceReader.getNext()).thenReturn(null);
        when(rpmPromoPriceReader.getNext()).thenReturn(null);
        when(storeZoneReader.getNext()).thenReturn(null);
        when(rpmPromotionReader.getNext()).thenReturn(null);
        when(rpmPromotionDescReader.getNext()).thenReturn(null);

        when(uuidGenerator.getUUID()).thenReturn("uuid");

        when(this.promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("promotionOfferId", "itemNumber", zoneId)).thenReturn(newArrayList(aPromotionWithDescriptions()));
    }

    @Test
    public void shouldInsertPriceZonePrice() throws Exception {
        String tpnb = "059428124";
        String tpnc = "284347092";
        ProductVariant productVariant = new ProductVariant(tpnc);
        int zoneId = 1;
        String price = "2.4";
        productVariant.addSaleInfo(new SaleInfo(zoneId, price));

       final Product product = createProduct(tpnb, productVariant);
        //final Product product = createProductWithVariant(tpnb,tpnc);
        mockAsyncProductInsert();
        Map<String, String> productInfoMap = productInfoMap(tpnb,tpnc, zoneId, price);
        when(rpmPriceReader.getNext()).thenReturn(productInfoMap).thenReturn(null);
        when(productRepository.getByTPNB(tpnb)).thenReturn(Optional.<Product>absent());
        this.rpmWriter.write();
        verify(productRepository).insertProduct(argThat(new CapturingMatcher<Product>() {
            @Override
            public boolean matches(Object o) {
                Product prod = (Product) o;
                return new RPMComparator().compare(prod,product);
            }
        }),any(Listener.class));
    }

    @Test
    public void shouldInsertMultiplePriceZonePricesForAVariant() throws Exception {
        String itemNumber = "0123";
        String tpnc = "284347092";

        when(rpmPriceReader.getNext()).thenReturn(productInfoMap(itemNumber, tpnc, 2, "2.4"))
                .thenReturn(productInfoMap(itemNumber,tpnc, 4, "4.4"))
                .thenReturn(null);

        Product product = createProductWithVariant(itemNumber, tpnc);
        mockAsyncProductInsert();
//Changes made By Surya for PS-120 . The JUnit should pass for the Code which will Create a new Product for the first time and then amend Multiple Price zones- Start
       // when(productRepository.getByTPNB(itemNumber)).thenReturn(Optional.<Product>absent()).thenReturn(Optional.of(product));
        when(productRepository.getByTPNB(itemNumber)).thenReturn(Optional.of(product));
 //Changes made By Surya for PS-120 . The JUnit should pass for the Code which will Create a new Product for the first time and then amend Multiple Price zones - End

        mockAsyncProductInsert();
        this.rpmWriter.write();

        InOrder inOrder = inOrder(productRepository);

        ProductVariant expectedProductVariant = createProductVariant(tpnc, 2, "2.4", null);
       final Product expectedProduct = createProduct(itemNumber, expectedProductVariant);

        inOrder.verify(productRepository).insertProduct(argThat(new CapturingMatcher<Product>() {
            @Override
            public boolean matches(Object o) {
                Product prod = (Product) o;
                return new RPMComparator().compare(prod,expectedProduct);
            }
        }),any(Listener.class));
      //  inOrder.verify(productRepository).put(expectedProduct);

        expectedProductVariant.addSaleInfo(new SaleInfo(4, "4.4"));
        inOrder.verify(productRepository).insertProduct(argThat(new CapturingMatcher<Product>() {
            @Override
            public boolean matches(Object o) {
                Product prod = (Product) o;
                return new RPMComparator().compare(prod,expectedProduct);
            }
        }),any(Listener.class));
       // inOrder.verify(productRepository).put(expectedProduct);
    }

    @Test
    @Ignore
    public void shouldInsertPriceZonePricesForMultipleVariants() throws Exception {
        String tpnb = "1123";
        String tpnc = "284347092";
        String tpnc2 = "304347092";
        String itemNumber = String.format("%s-001", tpnb);
        String itemNumber2 = String.format("%s-002", tpnb);
        mockAsyncProductInsert();

        when(rpmPriceReader.getNext()).thenReturn(productInfoMap(tpnb, tpnc, 2, "2.4"))
                .thenReturn(productInfoMap(itemNumber2,tpnc, 3, "3.0"))
                .thenReturn(null);

        Product product = createProductWithVariant(tpnb, tpnc);

        when(productRepository.getByTPNB(tpnb)).thenReturn(Optional.<Product>absent()).thenReturn(Optional.of(product));

        this.rpmWriter.write();

        InOrder inOrder = inOrder(productRepository);

      final  Product expectedProduct = createProductWithVariant(tpnb, tpnc);
        inOrder.verify(productRepository).insertProduct(argThat(new CapturingMatcher<Product>() {
            @Override
            public boolean matches(Object o) {
                Product prod = (Product) o;
                return new RPMComparator().compare(prod,expectedProduct);
            }
        }),any(Listener.class));
       // inOrder.verify(productRepository).put(expectedProduct);

        ProductVariant expectedProductVariant2 = createProductVariant(tpnc, 2, "2.4", null);
        expectedProduct.addProductVariant(expectedProductVariant2);
        inOrder.verify(productRepository).insertProduct(argThat(new CapturingMatcher<Product>() {
            @Override
            public boolean matches(Object o) {
                Product prod = (Product) o;
                return new RPMComparator().compare(prod,expectedProduct);
            }
        }),any(Listener.class));
        //inOrder.verify(productRepository).put(expectedProduct);
    }

    private Map<String, String> productInfoMap(String itemNumber, String tpnc, int zoneId, String price) {
        Map<String, String> productInfoMap = new HashMap<>();
        productInfoMap.put(CSVHeaders.Price.ITEM, itemNumber);
        productInfoMap.put(CSVHeaders.Price.TPNC, tpnc);
        productInfoMap.put(CSVHeaders.Price.PRICE_ZONE_ID, String.valueOf(zoneId));
        productInfoMap.put(CSVHeaders.Price.PRICE_ZONE_PRICE, price);

        return productInfoMap;
    }

    private Map<String, String> productPromoInfoMap(String itemNumber,String tpnc, int zoneId, String price) {
        Map<String, String> productInfoMap = new HashMap<>();
        productInfoMap.put(CSVHeaders.Price.ITEM, itemNumber);
        productInfoMap.put(CSVHeaders.Price.TPNC, tpnc);
        productInfoMap.put(CSVHeaders.Price.PROMO_ZONE_ID, String.valueOf(zoneId));
        productInfoMap.put(CSVHeaders.Price.PROMO_ZONE_PRICE, price);

        return productInfoMap;
    }

    @Test
    public void shouldInsertPromoZonePrice() throws Exception {
        final String tpnc = "284347092"; // This will change when TPNC story is played
        int priceZoneId = 2;
        String price = "2.3";
        ProductVariant productVariant = createProductVariant(tpnc, priceZoneId, price, null);

        final String tpnb = "059428124";
        Product product = createProduct(tpnb, productVariant);

        int promoZoneId = 5;
        String promoPrice = "2.0";
        Map<String, String> productInfoMap = productPromoInfoMap(tpnb,tpnc, promoZoneId, promoPrice);
        mockAsyncProductInsert();

        when(productRepository.getByTPNB(tpnb)).thenReturn(Optional.of(product));
        when(rpmPromoPriceReader.getNext()).thenReturn(productInfoMap).thenReturn(null);

        this.rpmWriter.write();

        ProductVariant expectedProductVariant = createProductVariant(tpnc, priceZoneId, price, null);
        expectedProductVariant.addSaleInfo(new SaleInfo(promoZoneId, promoPrice));

       final Product expectedProduct = createProduct(tpnb, expectedProductVariant);
        verify(productRepository).insertProduct(argThat(new CapturingMatcher<Product>() {
            @Override
            public boolean matches(Object o) {
                Product prod = (Product) o;
                return new RPMComparator().compare(prod,expectedProduct);
            }
        }),any(Listener.class));
       // verify(productRepository).put(expectedProduct);
    }

    @Test
    public void shouldInsertPromotionIntoProductPriceRepository() throws Exception {
        final String tpnc = "284347092"; // This will change when TPNC story is played
        int zoneId = 5;
        String price = "2.3";

        String tpnb = "070461113";
        String offerId = "A01";
        String offerName = "Test Offer Name";
        String startDate = "20130729";
        String endDate = "20130819";
        String description1 = "description1";
        String description2 = "description2";

        when(rpmPromotionReader.getNext()).thenReturn(promotionInfoMap(tpnb,tpnc, zoneId, offerId, offerName, startDate, endDate)).thenReturn(null);
        when(rpmPromotionDescReader.getNext()).thenReturn(promotionDescInfoMap(tpnb, zoneId, offerId, description1, description2)).thenReturn(null);
        mockAsyncProductInsert();

        ProductVariant productVariant = createProductVariant(tpnc, zoneId, price, null);
        ProductVariant productVariant2 = createProductVariant(tpnc, zoneId, price, createPromotion(offerId,zoneId, offerName, startDate, endDate));
        when(productRepository.getByTPNB(tpnb)).thenReturn(Optional.of(createProduct(tpnb, productVariant)), Optional.of(createProduct(tpnb, productVariant2)));
        when(productRepository.getProductTPNC(tpnb)).thenReturn(getTPNCForTPNB(tpnc));
       /* if(!productRepository.isSpaceOrNull(tpnb))
        {
            couchbaseWrapper.set(tpnb,tpnc);
        }*/
        this.rpmWriter.write();

         ArgumentCaptor<Product> arguments = ArgumentCaptor.forClass(Product.class);
         verify(productRepository, atLeastOnce()).insertProduct(arguments.capture(),any(Listener.class));

        Promotion expectedPromotion = createPromotion(offerId,zoneId, offerName, startDate, endDate);
        ProductVariant expectedProductVariant = createProductVariant(tpnc, zoneId, price, expectedPromotion);

        Product expectedProduct = createProduct(tpnb, expectedProductVariant);

        List<Product> actualProducts = arguments.getAllValues();
        assertThat(actualProducts.get(0)).isEqualTo(expectedProduct);

        expectedPromotion.setCFDescription1(description1);
        expectedPromotion.setCFDescription2(description2);
        assertThat(actualProducts.get(1)).isEqualTo(expectedProduct);
    }

    private Promotion createPromotion(String offerId,int zoneId, String offerName, String startDate, String endDate) {
        return new PromotionBuilder()
                .offerId(offerId)
                .zoneId(zoneId)
                .offerName(offerName)
                .startDate(startDate)
                .endDate(endDate)
                .description1(null)
                .description2(null)
                .createPromotion();
    }

    private Map<String,String> promotionDescInfoMap(String tpnb, int zoneId, String offerId, String description1, String description2) {
        Map<String, String> promotionInfoMap = new HashMap<>();
        promotionInfoMap.put(CSVHeaders.PromoDescExtract.ITEM, tpnb);
        promotionInfoMap.put(CSVHeaders.PromoDescExtract.ZONE_ID, String.valueOf(zoneId));
        promotionInfoMap.put(CSVHeaders.PromoDescExtract.OFFER_ID, offerId);
        promotionInfoMap.put(CSVHeaders.PromoDescExtract.DESC1, description1);
        promotionInfoMap.put(CSVHeaders.PromoDescExtract.DESC2, description2);
        return promotionInfoMap;

    }

    private Map<String, String> promotionInfoMap(String tpnb,String tpnc, int zoneId, String offerId, String offerName, String startDate, String endDate) {
        Map<String, String> promotionInfoMap = new HashMap<>();
        promotionInfoMap.put(CSVHeaders.PromoExtract.ITEM, tpnb);
        promotionInfoMap.put(CSVHeaders.PromoExtract.TPNC, tpnc);
        promotionInfoMap.put(CSVHeaders.PromoExtract.ZONE_ID, String.valueOf(zoneId));
        promotionInfoMap.put(CSVHeaders.PromoExtract.OFFER_ID, offerId);
        promotionInfoMap.put(CSVHeaders.PromoExtract.OFFER_NAME, offerName);
        promotionInfoMap.put(CSVHeaders.PromoExtract.START_DATE, startDate);
        promotionInfoMap.put(CSVHeaders.PromoExtract.END_DATE, endDate);
        return promotionInfoMap;
    }

    @Test
    public void shouldInsertStorePriceZones() throws Exception {
        String firstStoreId = "2002";
        String secondStoreId = "2003";
        mockAsyncStoreInsert();
        when(storeZoneReader.getNext()).thenReturn(getStoreInfoMap(firstStoreId, 1, 1, "GBP")).thenReturn(getStoreInfoMap(secondStoreId, 2, 1, "EUR")).thenReturn(null);
        when(storeRepository.getByStoreId(String.valueOf(firstStoreId))).thenReturn(Optional.<Store>absent());
        when(storeRepository.getByStoreId(String.valueOf(secondStoreId))).thenReturn(Optional.<Store>absent());
        this.rpmWriter.write();

      final  Store firstStore = new Store(firstStoreId, Optional.of(1), Optional.<Integer>absent(), "GBP");
      final  Store secondStore = new Store(secondStoreId, Optional.of(2), Optional.<Integer>absent(), "EUR");

        InOrder inOrder = inOrder(storeRepository);
        inOrder.verify(storeRepository).insertStore(argThat(new CapturingMatcher<Store>() {
            @Override
            public boolean matches(Object o) {
                Store store = (Store) o;
                return new RPMComparator().compare(store,firstStore);
            }
        }),any(Listener.class));
        inOrder.verify(storeRepository).insertStore(argThat(new CapturingMatcher<Store>() {
            @Override
            public boolean matches(Object o) {
                Store store = (Store) o;
                return new RPMComparator().compare(store,secondStore);
            }
        }),any(Listener.class));
       // inOrder.verify(storeRepository).put(new Store(firstStoreId, Optional.of(1), Optional.<Integer>absent(), "GBP"));
        //inOrder.verify(storeRepository).put(new Store(secondStoreId, Optional.of(2), Optional.<Integer>absent(), "EUR"));
    }

    @Test
    public void shouldInsertStorePriceAndPromoZones() throws Exception {
        String storeId = "2002";

        when(storeZoneReader.getNext()).thenReturn(getStoreInfoMap(storeId, 1, 1, "GBP")).thenReturn(getStoreInfoMap(storeId, 5, 2, "GBP")).thenReturn(null);
        Store store = new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP");
        mockAsyncStoreInsert();
        when(storeRepository.getByStoreId(String.valueOf(storeId))).thenReturn(Optional.<Store>absent()).thenReturn(Optional.of(store));
        final  Store firstStore = new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP");
        final  Store secondStore = new Store(storeId, Optional.of(1), Optional.of(5), "GBP");
        this.rpmWriter.write();

        InOrder inOrder = inOrder(storeRepository);
        inOrder.verify(storeRepository).insertStore(argThat(new CapturingMatcher<Store>() {
            @Override
            public boolean matches(Object o) {
                Store store = (Store) o;
                return new RPMComparator().compare(store,firstStore);
            }
        }),any(Listener.class));
        inOrder.verify(storeRepository).insertStore(argThat(new CapturingMatcher<Store>() {
            @Override
            public boolean matches(Object o) {
                Store store = (Store) o;
                return new RPMComparator().compare(store,secondStore);
            }
        }),any(Listener.class));
      //  inOrder.verify(storeRepository).put(new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP"));
       // inOrder.verify(storeRepository).put(new Store(storeId, Optional.of(1), Optional.of(5), "GBP"));
    }

    private Map<String, String> getStoreInfoMap(String firstStoreId, int zoneId, int zoneType, String currency) {
        Map<String, String> storeInfoMap = new HashMap<>();
        storeInfoMap.put(CSVHeaders.StoreZone.STORE_ID, firstStoreId);
        storeInfoMap.put(CSVHeaders.StoreZone.ZONE_ID, String.valueOf(zoneId));
        storeInfoMap.put(CSVHeaders.StoreZone.ZONE_TYPE, String.valueOf(zoneType));
        storeInfoMap.put(CSVHeaders.StoreZone.CURRENCY_CODE, currency);

        return storeInfoMap;
    }

    private ProductVariant createProductVariant(String tpnc, int zoneId, String price, Promotion promotion) {
        ProductVariant productVariant = new ProductVariant(tpnc);
        SaleInfo saleInfo = new SaleInfo(zoneId, price);
        if (promotion != null) saleInfo.addPromotion(promotion);
        productVariant.addSaleInfo(saleInfo);
        return productVariant;
    }

    private Product createProductWithVariant(String tpnb, String tpnc) {
        ProductVariant productVariant = createProductVariant(tpnc, 2, "2.4", null);
        Product product = createProduct(tpnb, productVariant);

        return product;
    }

    private Product createProduct(String tpnb, ProductVariant productVariant) {
        Product product = new Product(tpnb);
        product.addProductVariant(productVariant);
        return product;
    }

    private String getTPNCForTPNB(String tpnc) {

        return tpnc;
    }

    private Promotion aPromotionWithDescriptions() {
        Promotion promotion = new Promotion();
        promotion.setUniqueKey("uuid");
        promotion.setItemNumber("itemNumber");
        promotion.setZoneId(zoneId);
        promotion.setOfferId("promotionOfferId");
        promotion.setOfferName("promotionOfferName");
        promotion.setEffectiveDate("promotionStartDate");
        promotion.setEndDate("promotionEndDate");
        promotion.setCFDescription1("promotionCfDesc1");
        promotion.setCFDescription2("promotionCfDesc2");
        return promotion;
    }
    private void mockAsyncProductInsert() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Listener<Void,Exception>) invocation.getArguments()[1]).onComplete(null);
                return null;
            }
        }).when(productRepository).insertProduct(any(Product.class), any(Listener.class));
    }
    private void mockAsyncStoreInsert() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Listener<Void,Exception>) invocation.getArguments()[1]).onComplete(null);
                return null;
            }
        }).when(storeRepository).insertStore(any(Store.class), any(Listener.class));
    }

}
