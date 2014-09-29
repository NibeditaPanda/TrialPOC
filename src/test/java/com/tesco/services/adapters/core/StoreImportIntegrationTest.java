package com.tesco.services.adapters.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.testutils.*;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.core.Store;
import com.tesco.services.repositories.StoreRepository;
import com.tesco.services.resources.TestConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class StoreImportIntegrationTest extends ImportJobIntegrationTestBase {
    private String oldStoreId = "4002";
    private CouchbaseWrapper couchbaseWrapper;
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;
    private CouchbaseTestManager couchbaseTestManager;

    @Before
    public void setUp() throws IOException, ParserConfigurationException, SAXException, ConfigurationException, JAXBException, ColumnNotFoundException, URISyntaxException, InterruptedException {
        System.out.println("ImportJobTestBase setup");
        TestConfiguration configuration = new TestConfiguration().load();
        if (configuration.isDummyCouchbaseMode()) {
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
    }
    @Override
    protected void preImportCallBack() {
    }

    @Test
    public void shouldImportStoreZones() throws InterruptedException, IOException, URISyntaxException {
        String storeId = "2002";
        Store store = new Store(storeId, Optional.of(1), Optional.of(5), "GBP");

        StoreRepository storeRepository = new StoreRepository(couchbaseWrapper,asyncCouchbaseWrapper,new ObjectMapper());
        TestListener<Void, Exception> listener = new TestListener<>();
        storeRepository.insertStore(store,listener);
        assertThat(storeRepository.getByStoreId(storeId).get()).isEqualTo(store);
        assertThat(storeRepository.getByStoreId(oldStoreId).isPresent()).isFalse();
    }
}
