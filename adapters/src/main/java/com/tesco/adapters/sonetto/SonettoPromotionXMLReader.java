package com.tesco.adapters.sonetto;

import com.mongodb.DBObject;
import com.tesco.adapters.sonetto.Elements.Promotion;
import com.tesco.adapters.sonetto.Elements.Promotions;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class SonettoPromotionXMLReader {
    private SonettoPromotionWriter writer;
    private String shelfURL;

    public SonettoPromotionXMLReader(SonettoPromotionWriter writer, String shelfURL) {
        this.writer = writer;
        this.shelfURL = shelfURL;
    }

    public void handle(String xmlPath) throws JAXBException {
        StreamSource source = new StreamSource(xmlPath);

        Unmarshaller u = JAXBContext.newInstance(Promotions.class).createUnmarshaller();
        Promotions promotions = u.unmarshal(source, Promotions.class).getValue();

        for (Promotion promotion : promotions.getStorePromotions()) {
            DBObject promotionDBObject = promotion.buildStoreDBObject(shelfURL);
            writer.updatePromotion(promotionDBObject);
        }

        for (Promotion promotion : promotions.getInternetPromotions()) {
            writer.createPromotion(promotion.buildInternetDBObject());
        }
    }
}
