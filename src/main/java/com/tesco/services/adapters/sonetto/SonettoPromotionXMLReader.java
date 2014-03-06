package com.tesco.services.adapters.sonetto;

import com.tesco.services.adapters.sonetto.Elements.Promotion;
import com.tesco.services.adapters.sonetto.Elements.Promotions;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

//TODO: Add tests / test drive and implement functionality for couchbase
public class SonettoPromotionXMLReader {
    private String shelfURL;
    private String promotionsDataExportXsd;

    public SonettoPromotionXMLReader(String shelfURL, String promotionsDataExportXsd) {
        this.shelfURL = shelfURL;
        this.promotionsDataExportXsd = promotionsDataExportXsd;
    }

    public void handle(String xmlPath) throws JAXBException, SAXException, IOException {
        StreamSource source = new StreamSource(xmlPath);

        Unmarshaller u = JAXBContext.newInstance(Promotions.class).createUnmarshaller();
        Promotions promotions = u.unmarshal(source, Promotions.class).getValue();

        for (Promotion promotion : promotions.getStorePromotions()) {
            //TODO: Write to db
        }

        for (Promotion promotion : promotions.getInternetPromotions()) {
            // TODO: Write to db
        }
    }
}
