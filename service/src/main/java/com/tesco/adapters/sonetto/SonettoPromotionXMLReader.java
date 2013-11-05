package com.tesco.adapters.sonetto;

import com.mongodb.DBObject;
import com.tesco.adapters.sonetto.Elements.Promotion;
import com.tesco.adapters.sonetto.Elements.Promotions;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

public class SonettoPromotionXMLReader {
    private SonettoPromotionWriter writer;
    private String shelfURL;
    private String promotionsDataExportXsd;

    public SonettoPromotionXMLReader(SonettoPromotionWriter writer, String shelfURL, String promotionsDataExportXsd) {
        this.writer = writer;
        this.shelfURL = shelfURL;
        this.promotionsDataExportXsd = promotionsDataExportXsd;
    }

    public void handle(String xmlPath) throws JAXBException, SAXException, IOException {
        StreamSource source = new StreamSource(xmlPath);

        Unmarshaller u = JAXBContext.newInstance(Promotions.class).createUnmarshaller();
        Promotions promotions = u.unmarshal(source, Promotions.class).getValue();

        SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File(this.promotionsDataExportXsd));

        Validator validator = schema.newValidator();
        validator.validate(source);

        for (Promotion promotion : promotions.getStorePromotions()) {
            DBObject promotionDBObject = promotion.buildStoreDBObject(shelfURL);
            writer.updatePromotion(promotionDBObject);
        }

        for (Promotion promotion : promotions.getInternetPromotions()) {
            writer.createPromotion(promotion.buildInternetDBObject());
        }
    }
}
