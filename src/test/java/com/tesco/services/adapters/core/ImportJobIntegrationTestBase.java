package com.tesco.services.adapters.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.testutils.AsyncCouchbaseWrapperStub;
import com.tesco.couchbase.testutils.BucketTool;
import com.tesco.couchbase.testutils.CouchbaseTestManager;
import com.tesco.couchbase.testutils.CouchbaseWrapperStub;
import com.tesco.services.IntegrationTest;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.resources.TestConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import static com.tesco.services.adapters.core.TestFiles.RPM_PRICE_ZONE_PRICE_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMO_DESC_EXTRACT_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMO_EXTRACT_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMO_ZONE_PRICE_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_STORE_ZONE_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.SONETTO_PROMOTIONS_XML_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.SONETTO_PROMOTIONS_XSD_FILE_PATH;
import static org.mockito.Mockito.mock;

public abstract class ImportJobIntegrationTestBase /*extends IntegrationTest*/ {
    protected CouchbaseConnectionManager couchbaseConnectionManager;
    private CouchbaseWrapper couchbaseWrapper;
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;
    private CouchbaseTestManager couchbaseTestManager;

    @Before
    public void setUp() throws IOException, ParserConfigurationException, SAXException, ConfigurationException, JAXBException, ColumnNotFoundException, URISyntaxException, InterruptedException {
        System.out.println("ImportJobTestBase setup");
        TestConfiguration configuration = new TestConfiguration().load();
        if (configuration.isDummyCouchbaseMode()){
            HashMap<String, ImmutablePair<Long, String>> fakeBase = new HashMap<>();
            couchbaseTestManager = new CouchbaseTestManager(new CouchbaseWrapperStub(fakeBase),
                    new AsyncCouchbaseWrapperStub(fakeBase),
                    mock(BucketTool.class));
        } else {
            couchbaseTestManager = new CouchbaseTestManager(configuration.getCouchbaseBucket(),
                    configuration.getCouchbaseUsername(),
                    configuration.getCouchbasePassword(),
                    configuration.getCouchbaseNodes(),
                    configuration.getCouchbaseAdminUsername(),
                    configuration.getCouchbaseAdminPassword());
        }

        couchbaseWrapper = couchbaseTestManager.getCouchbaseWrapper();
        asyncCouchbaseWrapper = couchbaseTestManager.getAsyncCouchbaseWrapper();

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
                couchbaseWrapper,
                asyncCouchbaseWrapper);
        importJob.run();
    }

    protected void preImportCallBack() {
    }
}
