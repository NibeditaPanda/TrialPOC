package com.tesco.services.adapters.core;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.dao.DBFactory;
import com.tesco.services.repositories.DataGridResource;
import com.tesco.services.repositories.DataGridResourceForTest;
import com.tesco.services.resources.TestConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static com.mongodb.QueryBuilder.start;
import static com.tesco.services.adapters.core.TestFiles.*;
import static com.tesco.services.core.PriceKeys.ITEM_NUMBER;
import static com.tesco.services.core.PriceKeys.PRICE_COLLECTION;
import static com.tesco.services.core.PriceKeys.PROMOTION_COLLECTION;
import static com.tesco.services.core.PriceKeys.STORE_COLLECTION;
import static com.tesco.services.core.PriceKeys.ZONES;

public abstract class ImportJobIntegrationTestBase {
    protected DBCollection priceCollection;
    protected DBCollection storeCollection;
    protected DBCollection promotionCollection;

    protected DataGridResource dataGridResource;
    protected DBFactory dbFactory;


    @Before
    public void setUp() throws IOException, ParserConfigurationException, SAXException, ConfigurationException, JAXBException, ColumnNotFoundException {
        System.out.println("ImportJobTestBase setup");
        TestConfiguration configuration = new TestConfiguration();
        dataGridResource = new DataGridResourceForTest(configuration);

        initCaches();

        dbFactory = new DBFactory(configuration);

        dbFactory.getCollection(PRICE_COLLECTION).drop();
        priceCollection = dbFactory.getCollection(PRICE_COLLECTION);

        dbFactory.getCollection(STORE_COLLECTION).drop();
        storeCollection = dbFactory.getCollection(STORE_COLLECTION);

        dbFactory.getCollection(PROMOTION_COLLECTION).drop();
        promotionCollection = dbFactory.getCollection(PROMOTION_COLLECTION);

        preImportCallBack();

        ImportJob importJob = new ImportJob(
                RPM_PRICE_ZONE_CSV_FILE_PATH,
                RPM_STORE_ZONE_CSV_FILE_PATH,
                RPM_PROMOTION_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XML_FILE_PATH,
                RPM_PROMOTION_DESC_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XSD_FILE_PATH,
                "http://ui.tescoassets.com/Groceries/UIAssets/I/Sites/Retail/Superstore/Online/Product/pos/%s.png",
                RPM_PRICE_ZONE_PRICE_CSV_FILE_PATH,
                RPM_PROMO_ZONE_PRICE_CSV_FILE_PATH,
                RPM_PROMO_EXTRACT_CSV_FILE_PATH,
                RPM_PROMO_DESC_EXTRACT_CSV_FILE_PATH,
                dbFactory,
                dataGridResource);
        importJob.run();
    }

    protected void preImportCallBack() {

    }

    private void initCaches() {
        dataGridResource.getProductPriceCache();
        dataGridResource.getPromotionCache();
        dataGridResource.getStoreCache();
    }

    @After
    public void tearDown() throws Exception {
        dataGridResource.stop();

    }

    protected DBObject findPricesFromZone(String itemNumber, String zoneId) {
        DBObject queryForItemNumber = start(ITEM_NUMBER).is(itemNumber).get();
        DBObject prices = priceCollection.findOne(queryForItemNumber);
        return (DBObject) ((DBObject) prices.get(ZONES)).get(zoneId);
    }
}
