package com.tesco.adapters.rpm.writers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;
import com.tesco.core.PriceKeys;
import com.tesco.adapters.rpm.readers.RPMPriceZoneCSVFileReader;
import com.tesco.adapters.rpm.readers.RPMPromotionCSVFileReader;
import com.tesco.adapters.rpm.readers.RPMPromotionDescriptionCSVFileReader;
import com.tesco.adapters.rpm.readers.RPMStoreZoneCSVFileReader;
import com.tesco.adapters.sonetto.SonettoPromotionXMLReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static com.tesco.core.PriceKeys.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
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
    private DBCollection promotionCollection;

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

    @Before
    public void setUp() throws Exception {
        rpmWriter = new RPMWriter(priceCollection,
                storeCollection,
                promotionCollection,
                "./src/test/java/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml",
                rpmPriceZoneCSVFileReader,
                rpmStoreZoneCSVFileReader,
                rpmPromotionCSVFileReader,
                rpmPromotionDescriptionCSVFileReader,
                sonettoPromotionXMLReader);
    }

    @Test
    public void shouldInsertToPriceCollection() throws Exception {

        BasicDBObject existingPrice = new BasicDBObject(aPrice());
        BasicDBObject newPrice = new BasicDBObject("$set", existingPrice);

        when(rpmPriceZoneCSVFileReader.getNext()).thenReturn(existingPrice).thenReturn(null);
        when(priceCollection.update(any(BasicDBObject.class), any(BasicDBObject.class), anyBoolean(), anyBoolean())).thenReturn(writeResult);
        when(priceCollection.find(any(BasicDBObject.class))).thenReturn(dbCursor);

        BasicDBObject existingStore = new BasicDBObject(aStore());
        BasicDBObject newStore = new BasicDBObject("$set", existingStore);

        when(rpmStoreZoneCSVFileReader.getNext()).thenReturn(existingStore).thenReturn(null);
        when(storeCollection.update(any(BasicDBObject.class), any(BasicDBObject.class), anyBoolean(), anyBoolean())).thenReturn(writeResult);

        BasicDBObject existingPromotion = new BasicDBObject(aPromotion());

        when(rpmPromotionCSVFileReader.getNext()).thenReturn(existingPromotion).thenReturn(null);
        when(storeCollection.update(any(BasicDBObject.class), any(BasicDBObject.class), anyBoolean(), anyBoolean())).thenReturn(writeResult);

        BasicDBObject existingPromotionDesc = new BasicDBObject(aPromotionDescription());
        when(rpmPromotionDescriptionCSVFileReader.getNext()).thenReturn(existingPromotionDesc).thenReturn(null);
        when(promotionCollection.update(any(BasicDBObject.class), any(BasicDBObject.class), anyBoolean(), anyBoolean())).thenReturn(writeResult);

        when(writeResult.getN()).thenReturn(0);
        when(writeResult.getField("updatedExisting")).thenReturn("false");

        this.rpmWriter.write();

        Map<String, String> priceId = new HashMap();
        priceId.put(ITEM_NUMBER, ITEM_NUMBER);

        Map<String, String> storeId = new HashMap();
        storeId.put(STORE_ID, STORE_ID);

        verify(this.priceCollection).update(new BasicDBObject(priceId), newPrice, true, true);
        verify(this.storeCollection).update(new BasicDBObject(storeId), newStore, true, true );
        verify(this.promotionCollection).insert(existingPromotion);

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

    private Map<String, String> aPromotion() {
        Map<String, String> promotion = new HashMap<>();
        promotion.put(ITEM_NUMBER, "itemNumber");
        promotion.put(ZONE_ID, "zoneId");
        promotion.put(PROMOTION_OFFER_ID, "promotionOfferId");
        promotion.put(PROMOTION_OFFER_NAME, "promotionOfferName");
        promotion.put(PROMOTION_START_DATE, "promotionStartDate");
        promotion.put(PROMOTION_END_DATE, "promotionEndDate");
        return promotion;
    }

    private Map<String, String> aPromotionDescription() {
        HashMap<String, String> promotion = new HashMap<>();

        promotion.put(PROMOTION_OFFER_ID, "promotionOfferId");
        promotion.put(ZONE_ID, "zoneId");
        promotion.put(PROMOTION_CF_DESCRIPTION_1, "promotionCfDesc1");
        promotion.put(PROMOTION_CF_DESCRIPTION_2, "promotionCfDesc2");

        return promotion;
    }
}
