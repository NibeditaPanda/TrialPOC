package com.tesco.adapters.sonetto;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.core.PriceKeys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SonettoPromotionXMLReaderTest {

    private static final String PROMOTIONS_DATA_EXPORT = "src/test/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml";
    private static final String MALFORMED_PROMOTIONS_DATA_EXPORT = "src/test/resources/com/tesco/adapters/sonetto/MalformedPromotionsDataExport.xml";
    private static final String PROMOTIONS_DATA_EXPORT_XSD = "templates/Promotions.xsd.xml";
    private static final String SHELF_URL = "http://ui.tescoassets.com/Groceries/UIAssets/I/Sites/Retail/Superstore/Online/Product/pos/%s.png";

    @Mock
    private SonettoPromotionWriter mockWriter;
    private SonettoPromotionXMLReader sonettoPromotionXMLHandler;

    @Before
    public void setup(){
        sonettoPromotionXMLHandler = new SonettoPromotionXMLReader(mockWriter, SHELF_URL, PROMOTIONS_DATA_EXPORT_XSD);
    }

    @Test
    public void shouldReadShelfTalkerIDFromSonetto() throws IOException, SAXException, ParserConfigurationException, JAXBException {
        String image = String.format(SHELF_URL, "specialpurchase");

        sonettoPromotionXMLHandler.handle(PROMOTIONS_DATA_EXPORT);

        DBObject promotions = new BasicDBObject();
        promotions.put(PriceKeys.PROMOTION_OFFER_ID, "A29721690");
        promotions.put(PriceKeys.SHELF_TALKER_IMAGE, image);

        verify(mockWriter, times(1)).updatePromotion(promotions);
    }

    @Test
    public void handlesStorePromotionsAndUpdates() throws IOException, SAXException, ParserConfigurationException, JAXBException {
        String image = String.format(SHELF_URL, "specialpurchase");

        sonettoPromotionXMLHandler.handle(PROMOTIONS_DATA_EXPORT);

        DBObject storePromotion = new BasicDBObject();
        storePromotion.put(PriceKeys.PROMOTION_OFFER_ID, "A29721690");
        storePromotion.put(PriceKeys.SHELF_TALKER_IMAGE, image);

        verify(mockWriter, times(1)).updatePromotion(storePromotion);
    }

    @Test
    public void handlesInternetExclusivePromotionsAndInserts() throws IOException, SAXException, ParserConfigurationException, JAXBException {
        sonettoPromotionXMLHandler.handle(PROMOTIONS_DATA_EXPORT);

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
