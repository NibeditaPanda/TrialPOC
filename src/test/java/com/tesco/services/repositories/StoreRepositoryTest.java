package com.tesco.services.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.listeners.SetListener;
import com.tesco.couchbase.testutils.*;
import com.tesco.services.Configuration;
import com.tesco.services.IntegrationTest;
import com.tesco.services.adapters.rpm.writers.CSVHeaders;
import com.tesco.services.adapters.rpm.writers.StoreMapper;
import com.tesco.services.core.Store;
import com.tesco.services.resources.TestConfiguration;
import net.spy.memcached.internal.OperationFuture;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class StoreRepositoryTest extends IntegrationTest {
    @Rule
    public TestName name = new TestName();

    private String storeId = "2012";
    private Store store;
    private StoreRepository storeRepository;


    private CouchbaseTestManager couchbaseTestManager;
    @Mock
    private CouchbaseWrapper couchbaseWrapper;
    @Mock
    private OperationFuture<?> operationFuture;
    @Mock
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
       // String bucketName = "PriceService";///should be name.getMethodName();
        Configuration testConfiguration = TestConfiguration.load();

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
        storeRepository = new StoreRepository(this.couchbaseWrapper,asyncCouchbaseWrapper, mapper);
    }

    @Test
    public void shouldCacheStoreByStoreId() throws Exception {
        store = new Store(storeId,"GBP");
        storeRepository.put(store);
        assertThat(storeRepository.getByStoreId(storeId)).isEqualTo(Optional.of(store));
    }

    @Test
    public void shouldReturnNullObjectWhenProductIsNotFound() throws Exception {
        int storeId = 1234;
        assertThat(storeRepository.getByStoreId(String.valueOf(storeId)).isPresent()).isFalse();
    }

    @Test
    public void shouldNamespacePrefixKey() {
        store = new Store(storeId,"GBP");
       final CouchbaseWrapper couchbaseWrapperMock = mock(CouchbaseWrapper.class);
        storeRepository = new StoreRepository(couchbaseWrapperMock,asyncCouchbaseWrapper,mapper);
        final InOrder inOrder = inOrder(couchbaseWrapperMock);

        storeRepository.put(store);
        storeRepository.getByStoreId(storeId);

        try {
            inOrder.verify(couchbaseWrapperMock).set("STORE_" + storeId, mapper.writeValueAsString(store));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        inOrder.verify(couchbaseWrapperMock).get("STORE_" + storeId);
    }
    @Test
    public void getStoreWithStoreIdWithAsyncCouchBaseWrapper(){
        store = new Store(storeId,"GBP");
        storeRepository.put(store);
        TestListener<Store, Exception> listener = new TestListener<>();
        storeRepository.getStoreByStoreId(storeId,listener);
        try {
             assertThat(storeRepository.getStoreIdentified().equals(store));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Test
    public void getNullWhenStoreIdNotPresentWithAsyncCouchBaseWrapper(){
        store = new Store(storeId,"GBP");
        storeRepository.put(store);
        TestListener<Store, Exception> listener = new TestListener<>();
        storeRepository.getStoreByStoreId("1234",listener);
        try {
            if(listener.getResult()== null){
                System.out.println("Store not Found");
            }
            //assertThat(listener.getResult().getStoreId().isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Test
    public void setStoreIdsWithAsyncCouchBaseWrapper(){
        store = new Store(storeId,"GBP");

        TestListener<Void, Exception> listener = new TestListener<>();
        storeRepository.insertStore(store, listener);
        try {
            listener.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Test
    public void storeMapperTestWithAsyncCouchBaseWrapper(){
        StoreMapper storeMapper = new StoreMapper(storeRepository);

        store= storeMapper.mapStore(getStoreInfoMap(storeId, 1, 1, "GBP"));
        TestListener<Void, Exception> listener = new TestListener<>();
        storeRepository.insertStore(store, listener);
        try {
            listener.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void mockAsyncSet() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((SetListener) invocation.getArguments()[2]).onComplete(operationFuture);
                return null;
            }
        }).when(asyncCouchbaseWrapper).set(any(String.class), any(String.class), any(SetListener.class));
    }
    private Map<String, String> getStoreInfoMap(String firstStoreId, int zoneId, int zoneType, String currency) {
        Map<String, String> storeInfoMap = new HashMap<>();
        storeInfoMap.put(CSVHeaders.StoreZone.STORE_ID, firstStoreId);
        storeInfoMap.put(CSVHeaders.StoreZone.ZONE_ID, String.valueOf(zoneId));
        storeInfoMap.put(CSVHeaders.StoreZone.ZONE_TYPE, String.valueOf(zoneType));
        storeInfoMap.put(CSVHeaders.StoreZone.CURRENCY_CODE, currency);

        return storeInfoMap;
    }
}
