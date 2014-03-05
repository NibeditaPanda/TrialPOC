package com.tesco.services.adapters.rpm.writers;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;
import com.tesco.services.adapters.rpm.readers.*;
import com.tesco.services.adapters.rpm.readers.PriceServiceCSVReader;
import com.tesco.services.adapters.sonetto.SonettoPromotionXMLReader;
import com.tesco.services.builder.PromotionBuilder;
import com.tesco.services.core.*;
import com.tesco.services.repositories.ProductRepository;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.repositories.StoreRepository;
import com.tesco.services.repositories.UUIDGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tesco.services.core.PriceKeys.ITEM_NUMBER;
import static com.tesco.services.core.PriceKeys.STORE_ID;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.util.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RPMWriterTest {
    private RPMWriter rpmWriter;

    @Mock
    private DBCollection priceCollection;

    @Mock
    private DBCollection storeCollection;

    @Mock
    private RPMPriceZoneCSVFileReader rpmPriceZoneCSVFileReader;

    @Mock
    private RPMStoreZoneCSVFileReader rpmStoreZoneCSVFileReader;

    @Mock
    private RPMPromotionCSVFileReader rpmPromotionCSVFileReader;

    @Mock
    private RPMPromotionDescriptionCSVFileReader rpmPromotionDescriptionCSVFileReader;

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
    private WriteResult writeResult;

    @Mock
    private DBCursor dbCursor;

    @Mock
    private UUIDGenerator uuidGenerator;
    private BasicDBObject newPrice;

    private BasicDBObject newStore;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreRepository storeRepository;
    private int zoneId = 1;

    @Before
    public void setUp() throws Exception {
        rpmWriter = new RPMWriter(priceCollection,
                storeCollection,
                "./src/test/java/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml",
                rpmPriceZoneCSVFileReader,
                rpmStoreZoneCSVFileReader,
                sonettoPromotionXMLReader,
                promotionRepository,
                rpmPromotionCSVFileReader,
                rpmPromotionDescriptionCSVFileReader,
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

        BasicDBObject existingPrice = new BasicDBObject(aPrice());
        newPrice = new BasicDBObject("$set", existingPrice);

        when(rpmPriceZoneCSVFileReader.getNext()).thenReturn(existingPrice).thenReturn(null);
        when(priceCollection.update(any(BasicDBObject.class), any(BasicDBObject.class), anyBoolean(), anyBoolean())).thenReturn(writeResult);
        when(priceCollection.find(any(BasicDBObject.class))).thenReturn(dbCursor);

        BasicDBObject existingStore = new BasicDBObject(aStore());
        newStore = new BasicDBObject("$set", existingStore);

        when(rpmStoreZoneCSVFileReader.getNext()).thenReturn(existingStore).thenReturn(null);
        when(storeCollection.update(any(BasicDBObject.class), any(BasicDBObject.class), anyBoolean(), anyBoolean())).thenReturn(writeResult);

        when(rpmPromotionCSVFileReader.getNextDG()).thenReturn(aPromotion()).thenReturn(null);

        when(storeCollection.update(any(BasicDBObject.class), any(BasicDBObject.class), anyBoolean(), anyBoolean())).thenReturn(writeResult);

        when(rpmPromotionDescriptionCSVFileReader.getNextDG()).thenReturn(aPromotionWithDescriptions()).thenReturn(null);

        when(this.promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("promotionOfferId", "itemNumber", zoneId)).thenReturn(newArrayList(aPromotionWithDescriptions()));

        when(writeResult.getN()).thenReturn(0);
        when(writeResult.getField("updatedExisting")).thenReturn("false");
    }

    @Test
    public void shouldInsertToCollections() throws Exception {
        this.rpmWriter.write();

        Map<String, String> priceId = new HashMap();
        priceId.put(ITEM_NUMBER, ITEM_NUMBER);

        Map<String, String> storeId = new HashMap();
        storeId.put(STORE_ID, STORE_ID);

        verify(this.priceCollection).update(new BasicDBObject(priceId), newPrice, true, true);
        verify(this.storeCollection).update(new BasicDBObject(storeId), newStore, true, true);

//        verify(this.promotionRepository).addPromotion(aPromotion());
//        verify(this.promotionRepository).updatePromotion("uuid", aPromotionWithDescriptions());

    }

    @Test
    public void shouldInsertPriceZonePrice() throws Exception {
        String tpnb = "059428124";
        ProductVariant productVariant = new ProductVariant(tpnb);
        int zoneId = 1;
        String price = "2.4";
        productVariant.addSaleInfo(new SaleInfo(zoneId, price));

        Product product = createProduct(tpnb, productVariant);

        Map<String, String> productInfoMap = productInfoMap(tpnb, zoneId, price);
        when(rpmPriceReader.getNext()).thenReturn(productInfoMap).thenReturn(null);

        when(productRepository.getByTPNB(tpnb)).thenReturn(Optional.<Product>absent());
        this.rpmWriter.write();

        verify(productRepository).put(product);
    }

    @Test
    public void shouldInsertMultiplePriceZonePricesForAVariant() throws Exception {
        String itemNumber = "0123";

        when(rpmPriceReader.getNext()).thenReturn(productInfoMap(itemNumber, 2, "2.4"))
                .thenReturn(productInfoMap(itemNumber, 4, "4.4"))
                .thenReturn(null);

        Product product = createProductWithVariant(itemNumber, itemNumber);

        when(productRepository.getByTPNB(itemNumber)).thenReturn(Optional.<Product>absent()).thenReturn(Optional.of(product));

        this.rpmWriter.write();

        InOrder inOrder = inOrder(productRepository);

        ProductVariant expectedProductVariant = createProductVariant(itemNumber, 2, "2.4", null);
        Product expectedProduct = createProduct(itemNumber, expectedProductVariant);

        inOrder.verify(productRepository).put(expectedProduct);

        expectedProductVariant.addSaleInfo(new SaleInfo(4, "4.4"));

        inOrder.verify(productRepository).put(expectedProduct);
    }

    @Test
    public void shouldInsertPriceZonePricesForMultipleVariants() throws Exception {
        String tpnb = "1123";
        String itemNumber = String.format("%s-001", tpnb);
        String itemNumber2 = String.format("%s-002", tpnb);

        when(rpmPriceReader.getNext()).thenReturn(productInfoMap(itemNumber, 2, "2.4"))
                .thenReturn(productInfoMap(itemNumber2, 3, "3.0"))
                .thenReturn(null);

        Product product = createProductWithVariant(tpnb, itemNumber);

        when(productRepository.getByTPNB(tpnb)).thenReturn(Optional.<Product>absent()).thenReturn(Optional.of(product));

        this.rpmWriter.write();

        InOrder inOrder = inOrder(productRepository);

        Product expectedProduct = createProductWithVariant(tpnb, itemNumber);

        inOrder.verify(productRepository).put(expectedProduct);

        ProductVariant expectedProductVariant2 = createProductVariant(itemNumber2, 3, "3.0", null);
        expectedProduct.addProductVariant(expectedProductVariant2);

        inOrder.verify(productRepository).put(expectedProduct);
    }

    private Map<String, String> productInfoMap(String itemNumber, int zoneId, String price) {
        Map<String, String> productInfoMap = new HashMap<>();
        productInfoMap.put(CSVHeaders.Price.TPNB, itemNumber);
        productInfoMap.put(CSVHeaders.Price.PRICE_ZONE_ID, String.valueOf(zoneId));
        productInfoMap.put(CSVHeaders.Price.PRICE_ZONE_PRICE, price);

        return productInfoMap;
    }

    private Map<String, String> productPromoInfoMap(String itemNumber, int zoneId, String price) {
        Map<String, String> productInfoMap = new HashMap<>();
        productInfoMap.put(CSVHeaders.Price.TPNB, itemNumber);
        productInfoMap.put(CSVHeaders.Price.PROMO_ZONE_ID, String.valueOf(zoneId));
        productInfoMap.put(CSVHeaders.Price.PROMO_ZONE_PRICE, price);

        return productInfoMap;
    }

    @Test
    public void shouldInsertPromoZonePrice() throws Exception {
        final String tpnc = "059428124"; // This will change when TPNC story is played
        int priceZoneId = 2;
        String price = "2.3";
        ProductVariant productVariant = createProductVariant(tpnc, priceZoneId, price, null);

        final String tpnb = "059428124";
        Product product = createProduct(tpnb, productVariant);

        int promoZoneId = 5;
        String promoPrice = "2.0";
        Map<String, String> productInfoMap = productPromoInfoMap(tpnc, promoZoneId, promoPrice);

        when(productRepository.getByTPNB(tpnb)).thenReturn(Optional.of(product));
        when(rpmPromoPriceReader.getNext()).thenReturn(productInfoMap).thenReturn(null);

        this.rpmWriter.write();

        ProductVariant expectedProductVariant = createProductVariant(tpnc, priceZoneId, price, null);
        expectedProductVariant.addSaleInfo(new SaleInfo(promoZoneId, promoPrice));

        Product expectedProduct = createProduct(tpnb, expectedProductVariant);

        verify(productRepository).put(expectedProduct);
    }

    @Test
    public void shouldInsertPromotionIntoProductPriceRepository() throws Exception {
        final String tpnc = "059428124-001"; // This will change when TPNC story is played
        int zoneId = 5;
        String price = "2.3";

        String tpnb = "059428124";
        String offerId = "A01";
        String offerName = "Test Offer Name";
        String startDate = "20130729";
        String endDate = "20130819";
        String description1 = "description1";
        String description2 = "description2";

        when(rpmPromotionReader.getNext()).thenReturn(promotionInfoMap(tpnc, zoneId, offerId, offerName, startDate, endDate)).thenReturn(null);
        when(rpmPromotionDescReader.getNext()).thenReturn(promotionDescInfoMap(tpnc, zoneId, offerId, description1, description2)).thenReturn(null);

        ProductVariant productVariant = createProductVariant(tpnc, zoneId, price, null);
        ProductVariant productVariant2 = createProductVariant(tpnc, zoneId, price, createPromotion(offerId, offerName, startDate, endDate));
        when(productRepository.getByTPNB(tpnb)).thenReturn(Optional.of(createProduct(tpnb, productVariant)), Optional.of(createProduct(tpnb, productVariant2)));

        this.rpmWriter.write();

        ArgumentCaptor<Product> arguments = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, atLeastOnce()).put(arguments.capture());

        Promotion expectedPromotion = createPromotion(offerId, offerName, startDate, endDate);
        ProductVariant expectedProductVariant = createProductVariant(tpnc, zoneId, price, expectedPromotion);

        Product expectedProduct = createProduct(tpnb, expectedProductVariant);

        List<Product> actualProducts = arguments.getAllValues();
        assertThat(actualProducts.get(0)).isEqualTo(expectedProduct);

        expectedPromotion.setCFDescription1(description1);
        expectedPromotion.setCFDescription2(description2);
        assertThat(actualProducts.get(1)).isEqualTo(expectedProduct);
    }

    private Promotion createPromotion(String offerId, String offerName, String startDate, String endDate) {
        return new PromotionBuilder()
                .offerId(offerId)
                .offerName(offerName)
                .startDate(startDate)
                .endDate(endDate)
                .description1(null)
                .description2(null)
                .createPromotion();
    }

    private Map<String,String> promotionDescInfoMap(String tpnb, int zoneId, String offerId, String description1, String description2) {
        Map<String, String> promotionInfoMap = new HashMap<>();
        promotionInfoMap.put(CSVHeaders.PromoDescExtract.TPNB, tpnb);
        promotionInfoMap.put(CSVHeaders.PromoDescExtract.ZONE_ID, String.valueOf(zoneId));
        promotionInfoMap.put(CSVHeaders.PromoDescExtract.OFFER_ID, offerId);
        promotionInfoMap.put(CSVHeaders.PromoDescExtract.DESC1, description1);
        promotionInfoMap.put(CSVHeaders.PromoDescExtract.DESC2, description2);
        return promotionInfoMap;

    }

    private Map<String, String> promotionInfoMap(String tpnb, int zoneId, String offerId, String offerName, String startDate, String endDate) {
        Map<String, String> promotionInfoMap = new HashMap<>();
        promotionInfoMap.put(CSVHeaders.PromoExtract.TPNB, tpnb);
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

        when(storeZoneReader.getNext()).thenReturn(getStoreInfoMap(firstStoreId, 1, 1, "GBP")).thenReturn(getStoreInfoMap(secondStoreId, 2, 1, "EUR")).thenReturn(null);
        when(storeRepository.getByStoreId(String.valueOf(firstStoreId))).thenReturn(Optional.<Store>absent());
        when(storeRepository.getByStoreId(String.valueOf(secondStoreId))).thenReturn(Optional.<Store>absent());
        this.rpmWriter.write();

        InOrder inOrder = inOrder(storeRepository);
        inOrder.verify(storeRepository).put(new Store(firstStoreId, Optional.of(1), Optional.<Integer>absent(), "GBP"));
        inOrder.verify(storeRepository).put(new Store(secondStoreId, Optional.of(2), Optional.<Integer>absent(), "EUR"));
    }

    @Test
    public void shouldInsertStorePriceAndPromoZones() throws Exception {
        String storeId = "2002";

        when(storeZoneReader.getNext()).thenReturn(getStoreInfoMap(storeId, 1, 1, "GBP")).thenReturn(getStoreInfoMap(storeId, 5, 2, "GBP")).thenReturn(null);
        Store store = new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP");
        when(storeRepository.getByStoreId(String.valueOf(storeId))).thenReturn(Optional.<Store>absent()).thenReturn(Optional.of(store));

        this.rpmWriter.write();

        InOrder inOrder = inOrder(storeRepository);
        inOrder.verify(storeRepository).put(new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP"));
        inOrder.verify(storeRepository).put(new Store(storeId, Optional.of(1), Optional.of(5), "GBP"));
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

    private HashMap<String, String> aStore() {
        HashMap<String, String> store = new HashMap<>();
        store.put(PriceKeys.STORE_ID, "storeId");
        store.put(PriceKeys.PRICE_ZONE_ID, "priceZoneId");
        store.put(PriceKeys.PROMOTION_ZONE_ID, "promotionZoneId");
        store.put(PriceKeys.CURRENCY, "GBP");
        return store;
    }

    private Map<String, String> aPrice() {
        Map<String, String> price = new HashMap<>();
        price.put(ITEM_NUMBER, "itemNumber");
        price.put("zones.5.price", "1.33");
        price.put("zones.5.promoPrice", "3.33");
        return price;
    }

    private Promotion aPromotion() {
        Promotion promotion = new Promotion();
        promotion.setUniqueKey("uuid");
        promotion.setItemNumber("itemNumber");
        promotion.setZoneId(zoneId);
        promotion.setOfferId("promotionOfferId");
        promotion.setOfferName("promotionOfferName");
        promotion.setEffectiveDate("promotionStartDate");
        promotion.setEndDate("promotionEndDate");
        return promotion;
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

}
