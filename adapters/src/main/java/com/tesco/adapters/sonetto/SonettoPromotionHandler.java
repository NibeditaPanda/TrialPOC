package com.tesco.adapters.sonetto;

import com.mongodb.BasicDBObject;
import com.tesco.adapters.core.PriceKeys;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SonettoPromotionHandler extends DefaultHandler {

    private String currentSonnettoID;
    private String currentElementValue;
    private SonettoPromotionWriter writer;
    private String shelfURL;
    private boolean isPromotion;
    private boolean isShelfTalkerImage;

    public SonettoPromotionHandler(SonettoPromotionWriter writer, String shelfURL) {
        this.writer = writer;
        this.shelfURL = shelfURL;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equalsIgnoreCase(PriceKeys.PROMOTION)) {
            isPromotion = true;
            currentSonnettoID = attributes.getValue(PriceKeys.SONETTO);
        }

        if (isPromotion && qName.equalsIgnoreCase(PriceKeys.SHELF_TALKER_IMAGE)) {
            isShelfTalkerImage = true;
        }

    }

    @Override
    public void characters(char[] value, int start, int length) throws SAXException {
        if (isShelfTalkerImage) {
            currentElementValue = new String(value, start, length);
        }
    }


    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase(PriceKeys.SHELF_TALKER_IMAGE)) {
            isShelfTalkerImage = false;
        }

        if (qName.equalsIgnoreCase(PriceKeys.PROMOTION)) {
            BasicDBObject promotions = new BasicDBObject();
            promotions.put(PriceKeys.PROMOTION_OFFER_ID, currentSonnettoID);
            promotions.put(PriceKeys.SHELF_TALKER_IMAGE, String.format(this.shelfURL, currentElementValue));
            this.writer.write(promotions);

            isPromotion = false;
        }
    }

}
