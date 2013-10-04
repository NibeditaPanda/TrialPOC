package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tesco.adapters.core.exceptions.ColumnNotFoundException;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static com.mongodb.QueryBuilder.start;
import static com.tesco.adapters.core.DBFactory.getCollection;
import static com.tesco.adapters.core.PriceKeys.*;

public class ControllerIntegrationTest {
    protected DBCollection priceCollection;
    protected DBCollection storeCollection;
    protected DBCollection promotionCollection;

    protected static final String RPM_PRICE_ZONE_CSV_FILE_PATH = "./src/test/resources/com/tesco/adapters/rpm/fixtures/price_zone.csv";
    protected static final String RPM_STORE_ZONE_CSV_FILE_PATH = "./src/test/resources/com/tesco/adapters/rpm/fixtures/store_zone.csv";
    protected static final String RPM_PROMOTION_CSV_FILE_PATH = "./src/test/resources/com/tesco/adapters/rpm/fixtures/prom_extract.csv";
    protected static final String RPM_PROMOTION_DESC_CSV_FILE_PATH = "src/test/resources/com/tesco/adapters/rpm/fixtures/PROM_DESC_EXTRACT.csv";
    protected static final String RPM_PRICE_ZONE_TO_UPDATE_CSV_FILE_PATH = "./src/test/resources/com/tesco/adapters/rpm/fixtures/price_zone_to_update.csv";
    protected static final String RPM_STORE_ZONE_TO_UPDATE_CSV_FILE_PATH = "./src/test/resources/com/tesco/adapters/rpm/fixtures/store_zone_to_update.csv";

    protected static final String SONETTO_PROMOTIONS_XML_FILE_PATH = "./src/test/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml";
    protected static final String SONETTO_PROMOTIONS_XSD_FILE_PATH = "./templates/Promotions.xsd.xml";

    @Before
    public void setUp() throws IOException, ParserConfigurationException, SAXException, ConfigurationException, JAXBException, ColumnNotFoundException {
        getCollection(PRICE_COLLECTION).drop();
        priceCollection = getCollection(PRICE_COLLECTION);

        getCollection(STORE_COLLECTION).drop();
        storeCollection = getCollection(STORE_COLLECTION);

        getCollection(PROMOTION_COLLECTION).drop();
        promotionCollection = getCollection(PROMOTION_COLLECTION);

        new Controller(priceCollection, storeCollection, promotionCollection,
                RPM_PRICE_ZONE_CSV_FILE_PATH,
                RPM_STORE_ZONE_CSV_FILE_PATH,
                RPM_PROMOTION_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XML_FILE_PATH,
                RPM_PROMOTION_DESC_CSV_FILE_PATH, SONETTO_PROMOTIONS_XSD_FILE_PATH).fetchAndSavePriceDetails();
    }

    protected DBObject findPricesFromZone(String itemNumber, String zoneId) {
        DBObject queryForItemNumber = start(ITEM_NUMBER).is(itemNumber).get();
        DBObject prices = priceCollection.findOne(queryForItemNumber);
        return (DBObject) ((DBObject) prices.get(ZONES)).get(zoneId);
    }
}
