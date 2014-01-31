package com.tesco.services.adapters.rpm.writers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;
import com.tesco.services.adapters.core.Product;
import com.tesco.services.adapters.core.ProductVariant;
import com.tesco.services.adapters.rpm.dto.PriceDTO;
import com.tesco.services.adapters.rpm.readers.*;
import com.tesco.services.adapters.sonetto.SonettoPromotionXMLReader;
import com.tesco.services.core.PriceKeys;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.SaleInfo;
import com.tesco.services.repositories.DataGridResource;
import com.tesco.services.repositories.ProductPriceRepository;
import com.tesco.services.repositories.PromotionRepository;
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
    private RPMPriceReader rpmPriceReader;

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
                rpmPriceReader);

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
        ProductVariant productVariant = new ProductVariant("059428124");
        String zoneId = "1";
        String price = "2.4";
        productVariant.addSaleInfo(new SaleInfo(zoneId, price));

        Product product = new Product("059428124");
        product.addProductVariant(productVariant);

        PriceDTO priceDTO = new PriceDTO("059428124", zoneId, price);
        when(rpmPriceReader.getNext()).thenReturn(priceDTO).thenReturn(null);

        this.rpmWriter.write();

        verify(productPriceRepository).put(product);
    }

    @Test
    public void shouldInsertMultiplePriceZonePricesForAVariant() throws Exception {
        String itemNumber = "0123";

        when(rpmPriceReader.getNext()).thenReturn(new PriceDTO(itemNumber, "2", "2.4"))
                                        .thenReturn(new PriceDTO(itemNumber, "4", "4.4"))
                                        .thenReturn(null);

        Product product = createProductWithVariant(itemNumber, itemNumber);

        when(productPriceRepository.getByTPNB(itemNumber)).thenReturn(null).thenReturn(product);

        this.rpmWriter.write();
        
        InOrder inOrder = inOrder(productPriceRepository);

        ProductVariant expectedProductVariant = createProductVariant(itemNumber, "2", "2.4");
        Product expectedProduct = new Product(itemNumber);
        expectedProduct.addProductVariant(expectedProductVariant);

        inOrder.verify(productPriceRepository).put(expectedProduct);

        expectedProductVariant.addSaleInfo(new SaleInfo("4", "4.4"));

        inOrder.verify(productPriceRepository).put(expectedProduct);
    }

    @Test
    public void shouldInsertPriceZonePricesForMultipleVariants() throws Exception {
        String tpnb = "1123";
        String itemNumber = String.format("%s-001", tpnb);
        String itemNumber2 = String.format("%s-002", tpnb);

        when(rpmPriceReader.getNext()).thenReturn(new PriceDTO(itemNumber, "2", "2.4"))
                                        .thenReturn(new PriceDTO(itemNumber2, "3", "3.0"))
                                        .thenReturn(null);

        Product product = createProductWithVariant(tpnb, itemNumber);

        when(productPriceRepository.getByTPNB(tpnb)).thenReturn(null).thenReturn(product);

        this.rpmWriter.write();

        InOrder inOrder = inOrder(productPriceRepository);

        Product expectedProduct = createProductWithVariant(tpnb, itemNumber);

        inOrder.verify(productPriceRepository).put(expectedProduct);

        ProductVariant expectedProductVariant2 = createProductVariant(itemNumber2, "3", "3.0");
        expectedProduct.addProductVariant(expectedProductVariant2);

        inOrder.verify(productPriceRepository).put(expectedProduct);
    }

    private ProductVariant createProductVariant(String tpnc, String zoneId, String price) {
        ProductVariant productVariant = new ProductVariant(tpnc);
        productVariant.addSaleInfo(new SaleInfo(zoneId, price));
        return productVariant;
    }

    private Product createProductWithVariant(String tpnb, String tpnc) {
        Product product = new Product(tpnb);
        product.addProductVariant(createProductVariant(tpnc, "2", "2.4"));

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
