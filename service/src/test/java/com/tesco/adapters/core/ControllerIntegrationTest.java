package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tesco.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.core.DBFactory;
import com.tesco.services.resources.TestConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static com.mongodb.QueryBuilder.start;
import static com.tesco.core.PriceKeys.*;

public class ControllerIntegrationTest {
    protected DBCollection priceCollection;
    protected DBCollection storeCollection;
    protected DBCollection promotionCollection;
    protected DBCollection tempPriceCollection;
    protected DBCollection tempStoreCollection;
    protected DBCollection tempPromotionCollection;

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
        TestConfiguration configuration = new TestConfiguration();

        DBFactory dbFactory = new DBFactory(configuration);

        dbFactory.getCollection(PRICE_COLLECTION).drop();
        priceCollection = dbFactory.getCollection(PRICE_COLLECTION);

        dbFactory.getCollection(STORE_COLLECTION).drop();
        storeCollection = dbFactory.getCollection(STORE_COLLECTION);

        dbFactory.getCollection(PROMOTION_COLLECTION).drop();
        promotionCollection = dbFactory.getCollection(PROMOTION_COLLECTION);

        dbFactory.getCollection(priceCollection + "_temp").drop();
        tempPriceCollection = dbFactory.getCollection(priceCollection + "_temp");
        dbFactory.getCollection(STORE_COLLECTION + "_temp").drop();
        tempStoreCollection = dbFactory.getCollection(STORE_COLLECTION + "_temp");
        dbFactory.getCollection(promotionCollection + "_temp").drop();
        tempPromotionCollection = dbFactory.getCollection(promotionCollection + "_temp");


        Controller controller = new Controller(
                RPM_PRICE_ZONE_CSV_FILE_PATH,
                RPM_STORE_ZONE_CSV_FILE_PATH,
                RPM_PROMOTION_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XML_FILE_PATH,
                RPM_PROMOTION_DESC_CSV_FILE_PATH, SONETTO_PROMOTIONS_XSD_FILE_PATH,
                "http://ui.tescoassets.com/Groceries/UIAssets/I/Sites/Retail/Superstore/Online/Product/pos/%s.png");

        controller.processData(tempPriceCollection, tempStoreCollection, tempPromotionCollection, false);
    }

    protected DBObject findPricesFromZone(String itemNumber, String zoneId) {
        DBObject queryForItemNumber = start(ITEM_NUMBER).is(itemNumber).get();
        DBObject prices = priceCollection.findOne(queryForItemNumber);
        return (DBObject) ((DBObject) prices.get(ZONES)).get(zoneId);
    }
}
