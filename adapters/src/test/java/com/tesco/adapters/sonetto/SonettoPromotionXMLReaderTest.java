package com.tesco.adapters.sonetto;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.adapters.core.PriceKeys;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SonettoPromotionXMLReaderTest {

    private SonettoPromotionWriter mockWriter = mock(SonettoPromotionWriter.class);

    @Test
    public void shouldReadShelfTalkerIDFromSonetto() throws IOException, SAXException, ParserConfigurationException {
        String shelfURL = "http://ui.tescoassets.com/Groceries/UIAssets/I/Sites/Retail/Superstore/Online/Product/pos/%s.png";
        String image = String.format(shelfURL, "specialpurchase");

        new SonettoPromotionXMLReader("src/test/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml", new SonettoPromotionHandler(mockWriter, shelfURL)).read();

        DBObject promotions = new BasicDBObject();
        promotions.put(PriceKeys.PROMOTION_OFFER_ID, "A29721690");
        promotions.put(PriceKeys.SHELF_TALKER_IMAGE, image);

        verify(mockWriter, times(1)).write(promotions);
    }

}
