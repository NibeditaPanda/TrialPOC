package com.tesco.services.adapters.core;

import com.tesco.services.IntegrationTest;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.resources.TestConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.tesco.services.adapters.core.TestFiles.RPM_PRICE_ZONE_PRICE_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMO_DESC_EXTRACT_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMO_EXTRACT_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMO_ZONE_PRICE_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_STORE_ZONE_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.SONETTO_PROMOTIONS_XML_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.SONETTO_PROMOTIONS_XSD_FILE_PATH;

public abstract class ImportJobIntegrationTestBase extends IntegrationTest {
    protected CouchbaseConnectionManager couchbaseConnectionManager;

    @Before
    public void setUp() throws IOException, ParserConfigurationException, SAXException, ConfigurationException, JAXBException, ColumnNotFoundException, URISyntaxException, InterruptedException {
        System.out.println("ImportJobTestBase setup");
        TestConfiguration configuration = new TestConfiguration();
        couchbaseConnectionManager = new CouchbaseConnectionManager(configuration);

        preImportCallBack();

        ImportJob importJob = new ImportJob(
                RPM_STORE_ZONE_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XML_FILE_PATH,
                SONETTO_PROMOTIONS_XSD_FILE_PATH,
                "http://ui.tescoassets.com/Groceries/UIAssets/I/Sites/Retail/Superstore/Online/Product/pos/%s.png",
                RPM_PRICE_ZONE_PRICE_CSV_FILE_PATH,
                RPM_PROMO_ZONE_PRICE_CSV_FILE_PATH,
                RPM_PROMO_EXTRACT_CSV_FILE_PATH,
                RPM_PROMO_DESC_EXTRACT_CSV_FILE_PATH,
                couchbaseConnectionManager);
        importJob.run();
    }

    protected void preImportCallBack() {
    }
}
