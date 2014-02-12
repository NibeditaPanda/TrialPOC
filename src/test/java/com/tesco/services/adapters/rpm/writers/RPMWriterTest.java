package com.tesco.services.adapters.rpm.writers;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;
import com.tesco.services.adapters.rpm.readers.*;
import com.tesco.services.adapters.rpm.readers.PriceServiceCSVReader;
import com.tesco.services.adapters.sonetto.SonettoPromotionXMLReader;
import com.tesco.services.core.PriceKeys;
import com.tesco.services.core.Product;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.SaleInfo;
import com.tesco.services.core.Store;
import com.tesco.services.repositories.DataGridResource;
import com.tesco.services.repositories.ProductPriceRepository;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.repositories.StoreRepository;
import com.tesco.services.repositories.UUIDGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static com.tesco.services.core.PriceKeys.ITEM_NUMBER;
import static com.tesco.services.core.PriceKeys.STORE_ID;
import static org.fest.util.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private WriteResult writeResult;

    @Mock
    private DBCursor dbCursor;

    @Mock
    private DataGridResource dataGridResource;

    @Mock
    private UUIDGenerator uuidGenerator;

    private BasicDBObject newPrice;
    private BasicDBObject newStore;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductPriceRepository productPriceRepository;

    @Mock
    private PriceServiceCSVReader rpmPriceReader;

    @Mock
    private PriceServiceCSVReader rpmPromoReader;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PriceServiceCSVReader storeZoneReader;

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
                productPriceRepository,
                storeRepository,
                rpmPriceReader,
                rpmPromoReader,
                storeZoneReader);
        when(rpmPriceReader.getNext()).thenReturn(null);
        when(rpmPromoReader.getNext()).thenReturn(null);
        when(storeZoneReader.getNext()).thenReturn(null);

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

        when(this.promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("promotionOfferId", "itemNumber", "zoneId")).thenReturn(newArrayList(aPromotionWithDescriptions()));

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

        verify(this.promotionRepository).addPromotion(aPromotion());
        verify(this.promotionRepository).updatePromotion("uuid", aPromotionWithDescriptions());

    }

    @Test
    public void shouldInsertPriceZonePrice() throws Exception {
        String tpnb = "059428124";
        ProductVariant productVariant = new ProductVariant(tpnb);
        int zoneId = 1;
        String price = "2.4";
        productVariant.addSaleInfo(new SaleInfo(zoneId, price));

        Product product = new Product(tpnb);
        product.addProductVariant(productVariant);

        Map<String, String> productInfoMap = productInfoMap(tpnb, zoneId, price);
        when(rpmPriceReader.getNext()).thenReturn(productInfoMap).thenReturn(null);

        when(productPriceRepository.getByTPNB(tpnb)).thenReturn(Optional.<Product>absent());
        this.rpmWriter.write();

        verify(productPriceRepository).put(product);
    }

    @Test
    public void shouldInsertMultiplePriceZonePricesForAVariant() throws Exception {
        String itemNumber = "0123";

        when(rpmPriceReader.getNext()).thenReturn(productInfoMap(itemNumber, 2, "2.4"))
                                        .thenReturn(productInfoMap(itemNumber, 4, "4.4"))
                                        .thenReturn(null);

        Product product = createProductWithVariant(itemNumber, itemNumber);

        when(productPriceRepository.getByTPNB(itemNumber)).thenReturn(Optional.<Product>absent()).thenReturn(Optional.of(product));

        this.rpmWriter.write();
        
        InOrder inOrder = inOrder(productPriceRepository);

        ProductVariant expectedProductVariant = createProductVariant(itemNumber, 2, "2.4");
        Product expectedProduct = new Product(itemNumber);
        expectedProduct.addProductVariant(expectedProductVariant);

        inOrder.verify(productPriceRepository).put(expectedProduct);

        expectedProductVariant.addSaleInfo(new SaleInfo(4, "4.4"));

        inOrder.verify(productPriceRepository).put(expectedProduct);
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

        when(productPriceRepository.getByTPNB(tpnb)).thenReturn(Optional.<Product>absent()).thenReturn(Optional.of(product));

        this.rpmWriter.write();

        InOrder inOrder = inOrder(productPriceRepository);

        Product expectedProduct = createProductWithVariant(tpnb, itemNumber);

        inOrder.verify(productPriceRepository).put(expectedProduct);

        ProductVariant expectedProductVariant2 = createProductVariant(itemNumber2, 3, "3.0");
        expectedProduct.addProductVariant(expectedProductVariant2);

        inOrder.verify(productPriceRepository).put(expectedProduct);
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
        ProductVariant productVariant = new ProductVariant(tpnc);
        int zoneId = 5;

        String price = "2.3";
        productVariant.addSaleInfo(new SaleInfo(zoneId, price));

        final String tpnb = "059428124";
        Product product = new Product(tpnb);
        product.addProductVariant(productVariant);
        Map<String, String> productInfoMap = productPromoInfoMap("059428124", zoneId, price);

        when(productPriceRepository.getByTPNB(tpnb)).thenReturn(Optional.of(product));
        when(rpmPromoReader.getNext()).thenReturn(productInfoMap).thenReturn(null);

        this.rpmWriter.write();

        verify(productPriceRepository).put(product);
    }

    @Test
    public void shouldInsertStorePriceZones() throws Exception {
        int firstStoreId = 2002;
        int secondStoreId = 2003;

        when(storeZoneReader.getNext()).thenReturn(getStoreInfoMap(firstStoreId, 1, 1, "GBP")).thenReturn(getStoreInfoMap(secondStoreId, 2, 1, "EUR")).thenReturn(null);
        when(storeRepository.getByStoreId(firstStoreId)).thenReturn(Optional.<Store>absent());
        when(storeRepository.getByStoreId(secondStoreId)).thenReturn(Optional.<Store>absent());
        this.rpmWriter.write();

        InOrder inOrder = inOrder(storeRepository);
        inOrder.verify(storeRepository).put(new Store(firstStoreId, Optional.of(1), Optional.<Integer>absent(), "GBP"));
        inOrder.verify(storeRepository).put(new Store(secondStoreId, Optional.of(2), Optional.<Integer>absent(), "EUR"));
    }

    private Map<String,String> getStoreInfoMap(int firstStoreId, int zoneId, int zoneType, String currency) {
        Map<String, String> storeInfoMap = new HashMap<>();
        storeInfoMap.put(CSVHeaders.StoreZone.STORE_ID, String.valueOf(firstStoreId));
        storeInfoMap.put(CSVHeaders.StoreZone.ZONE_ID, String.valueOf(zoneId));
        storeInfoMap.put(CSVHeaders.StoreZone.ZONE_TYPE, String.valueOf(zoneType));
        storeInfoMap.put(CSVHeaders.StoreZone.CURRENCY_CODE, currency);

        return storeInfoMap;
    }

    @Test
    public void shouldInsertStorePriceAndPromoZones() throws Exception {
        int storeId = 2002;

        when(storeZoneReader.getNext()).thenReturn(getStoreInfoMap(storeId, 1, 1, "GBP")).thenReturn(getStoreInfoMap(storeId, 5, 2, "GBP")).thenReturn(null);
        Store store = new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP");
        when(storeRepository.getByStoreId(storeId)).thenReturn(Optional.<Store>absent()).thenReturn(Optional.of(store));

        this.rpmWriter.write();

        InOrder inOrder = inOrder(storeRepository);
        inOrder.verify(storeRepository).put(new Store(storeId, Optional.of(1), Optional.<Integer>absent(), "GBP"));
        inOrder.verify(storeRepository).put(new Store(storeId, Optional.of(1), Optional.of(5), "GBP"));
    }

    private ProductVariant createProductVariant(String tpnc, int zoneId, String price) {
        ProductVariant productVariant = new ProductVariant(tpnc);
        productVariant.addSaleInfo(new SaleInfo(zoneId, price));
        return productVariant;
    }

    private Product createProductWithVariant(String tpnb, String tpnc) {
        Product product = new Product(tpnb);
        product.addProductVariant(createProductVariant(tpnc, 2, "2.4"));

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
        promotion.setZoneId("zoneId");
        promotion.setOfferId("promotionOfferId");
        promotion.setOfferName("promotionOfferName");
        promotion.setStartDate("promotionStartDate");
        promotion.setEndDate("promotionEndDate");
        return promotion;
    }

    private Promotion aPromotionWithDescriptions() {
        Promotion promotion = new Promotion();
        promotion.setUniqueKey("uuid");
        promotion.setItemNumber("itemNumber");
        promotion.setZoneId("zoneId");
        promotion.setOfferId("promotionOfferId");
        promotion.setOfferName("promotionOfferName");
        promotion.setStartDate("promotionStartDate");
        promotion.setEndDate("promotionEndDate");
        promotion.setCFDescription1("promotionCfDesc1");
        promotion.setCFDescription2("promotionCfDesc2");
        return promotion;
    }

}
