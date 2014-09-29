package com.tesco.services.resources;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.InvalidViewException;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewDesign;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.testutils.AsyncCouchbaseWrapperStub;
import com.tesco.couchbase.testutils.BucketTool;
import com.tesco.couchbase.testutils.CouchbaseTestManager;
import com.tesco.couchbase.testutils.CouchbaseWrapperStub;
import com.tesco.services.Configuration;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.yammer.dropwizard.testing.ResourceTest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Created by QT00 on 07/08/2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemPurgeResourceTest extends ResourceTest {

    @Mock
    private Configuration mockConfiguration;

    @Mock
    private Configuration testConfiguration;
    @Mock
    private CouchbaseWrapper couchbaseWrapper;
    @Mock
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;
    private ObjectMapper mapper;
    @Mock
    private CouchbaseClient couchbaseClient;
    private CouchbaseTestManager couchbaseTestManager;

    @Override
    protected void setUpResources() throws Exception {

        testConfiguration = TestConfiguration.load();
        if (testConfiguration.isDummyCouchbaseMode()){
            HashMap<String, ImmutablePair<Long, String>> fakeBase = new HashMap<>();
            couchbaseTestManager = new CouchbaseTestManager(new CouchbaseWrapperStub(fakeBase),
                    new AsyncCouchbaseWrapperStub(fakeBase),
                    mock(BucketTool.class));
        } else {
            couchbaseTestManager = new CouchbaseTestManager(testConfiguration.getCouchbaseBucket(),
                    testConfiguration.getCouchbaseUsername(),
                    testConfiguration.getCouchbasePassword(),
                    testConfiguration.getCouchbaseNodes(),
                    testConfiguration.getCouchbaseAdminUsername(),
                    testConfiguration.getCouchbaseAdminPassword());
        }

        couchbaseWrapper = couchbaseTestManager.getCouchbaseWrapper();
        asyncCouchbaseWrapper = couchbaseTestManager.getAsyncCouchbaseWrapper();

        mapper = new ObjectMapper();
        couchbaseClient = new CouchbaseConnectionManager(testConfiguration).getCouchbaseClient();
        addResource(new ItemPurgeResource(testConfiguration,couchbaseWrapper,asyncCouchbaseWrapper, mapper,couchbaseClient));
    }

    @Test
    public void shouldStartItemPurge() throws Exception {
        /*Added by Surya for View check to avoid Junit Failures - Start*/
        checkforView(couchbaseClient,testConfiguration);
        /*Added by Surya for View check to avoid Junit Failures - End*/
        WebResource resource = client().resource("/itempurge/purge");
        ClientResponse response = resource.post(ClientResponse.class);
        String responseText = response.getEntity(String.class);

        assertThat(responseText).isEqualTo("{\"message\":\"Purge Completed\"}");
        assertThat(response.getStatus()).isEqualTo(200);
    }
   /*Added by Surya for View check to avoid Junit Failures - Start*/
        private void createView(Configuration configuration, CouchbaseClient couchbaseClient)throws Exception{
        final DesignDocument designDoc = new DesignDocument(configuration.getCouchBaseDesignDocName());
        designDoc.setView(new ViewDesign(configuration.getCouchBaseViewName(),
                "function (doc, meta) {\n" +
                        "  if (doc.last_updated_date && meta.type == \"json\" ) {\n" +
                        "    emit(doc.last_updated_date, {KEY: meta.id});\n" +
                        "  }\n" +
                        "}"
        ));

        couchbaseClient.createDesignDoc(designDoc);
    }
    public void checkforView(CouchbaseClient couchbaseClient, Configuration configuration) throws Exception {
        try {
            View view = couchbaseClient.getView(configuration.getCouchBaseDesignDocName(), configuration.getCouchBaseViewName());
        }catch(InvalidViewException e){
            createView(configuration, couchbaseClient);
            Thread.sleep(50);
        }
    }
        /*Added by Surya for View check to avoid Junit Failures - End*/
}
