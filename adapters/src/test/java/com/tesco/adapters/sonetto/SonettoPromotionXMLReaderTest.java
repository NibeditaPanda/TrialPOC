package com.tesco.adapters.sonetto;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.adapters.core.PriceKeys;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class SonettoPromotionXMLReaderTest {

    private SonettoPromotionWriter mockWriter;
    private SonettoPromotionXMLReader sonettoPromotionXMLHandler;
    private final String shelfURL = "http://ui.tescoassets.com/Groceries/UIAssets/I/Sites/Retail/Superstore/Online/Product/pos/%s.png";

    @Before
    public void setup(){
        mockWriter = mock(SonettoPromotionWriter.class);
        sonettoPromotionXMLHandler = new SonettoPromotionXMLReader(mockWriter, shelfURL);
    }

    @Test
    public void shouldReadShelfTalkerIDFromSonetto() throws IOException, SAXException, ParserConfigurationException, JAXBException {
        String image = String.format(shelfURL, "specialpurchase");

        sonettoPromotionXMLHandler.handle("src/test/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml");

        DBObject promotions = new BasicDBObject();
        promotions.put(PriceKeys.PROMOTION_OFFER_ID, "A29721690");
        promotions.put(PriceKeys.SHELF_TALKER_IMAGE, image);

        verify(mockWriter, times(1)).updatePromotion(promotions);
    }

    @Test
    public void handlesStorePromotionsAndUpdates() throws IOException, SAXException, ParserConfigurationException, JAXBException {
        String image = String.format(shelfURL, "specialpurchase");

        sonettoPromotionXMLHandler.handle("src/test/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml");

        DBObject storePromotion = new BasicDBObject();
        storePromotion.put(PriceKeys.PROMOTION_OFFER_ID, "A29721690");
        storePromotion.put(PriceKeys.SHELF_TALKER_IMAGE, image);


        verify(mockWriter, times(1)).updatePromotion(storePromotion);
    }

    @Test
    public void handlesInternetExclusivePromotionsAndInserts() throws IOException, SAXException, ParserConfigurationException, JAXBException {
        sonettoPromotionXMLHandler.handle("src/test/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml");

        DBObject internetPromotionA = new BasicDBObject();
        internetPromotionA.put(PriceKeys.PROMOTION_OFFER_ID, "S00001030");
        internetPromotionA.put(PriceKeys.PROMOTION_OFFER_TEXT, "£10 off your first shop");
        internetPromotionA.put(PriceKeys.PROMOTION_START_DATE, "2009-11-03");
        internetPromotionA.put(PriceKeys.PROMOTION_END_DATE, "2015-12-31");

        DBObject internetPromotionB = new BasicDBObject();
        internetPromotionB.put(PriceKeys.PROMOTION_OFFER_ID, "S00001028");
        internetPromotionB.put(PriceKeys.PROMOTION_OFFER_TEXT, "£10 off your first shop");
        internetPromotionB.put(PriceKeys.PROMOTION_START_DATE, "2009-11-03");
        internetPromotionB.put(PriceKeys.PROMOTION_END_DATE, "2015-12-31");

        verify(mockWriter, times(1)).createPromotion(internetPromotionA);
        verify(mockWriter, times(1)).createPromotion(internetPromotionB);
    }
}
