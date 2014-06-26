package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.listeners.GetListener;
import com.tesco.couchbase.listeners.Listener;
import com.tesco.couchbase.listeners.SetListener;
import com.tesco.services.core.Store;
import com.tesco.services.exceptions.InvalidDataException;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;


public class StoreRepository {
    private CouchbaseClient couchbaseClient;
    private ObjectMapper mapper;
    private CouchbaseWrapper couchbaseWrapper;
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;

    public Store getStoreIdentified() {
        return storeIdentified;
    }

    private Store storeIdentified = null;

    private final Logger logger = getLogger(getClass().getName());
    public StoreRepository(CouchbaseClient couchbaseClient) {
        this.couchbaseClient = couchbaseClient;
    }

public StoreRepository(CouchbaseWrapper couchbaseWrapper,AsyncCouchbaseWrapper asyncCouchbaseWrapper,ObjectMapper mapper) {
    this.couchbaseWrapper = couchbaseWrapper;
    this.asyncCouchbaseWrapper = asyncCouchbaseWrapper;
    this.mapper = mapper;

}

    public void put(Store store) {
        final String storeId = store.getStoreId();
        try {
            couchbaseWrapper.set(getStoreKey(storeId), mapper.writeValueAsString(store));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    private String getStoreKey(String storeId) {
        return String.format("STORE_%s", storeId);
    }

    public Optional<Store> getByStoreId(String storeId) {
        Object storeJson = couchbaseWrapper.get(getStoreKey(storeId));
        Store store = new Store();
        if(storeJson == null){
            store = null;
        }
        else{
            try {
                store = mapper.readValue((String)storeJson,Store.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (store != null) ? Optional.of(store) : Optional.<Store>absent();
    }

    public void getStoreByStoreId(String storeId, final Listener<Store, Exception> listener) {
        final String key = getStoreKey(storeId);
        asyncCouchbaseWrapper.get(key, new GetListener(asyncCouchbaseWrapper, key) {
            @Override
            protected void process(Object jsonStore) {
                if (jsonStore != null) {
                    try {
                        Store store = mapper.readValue((String) jsonStore, Store.class);
                        listener.onComplete(store);
                        storeIdentified = store;
                    } catch (IOException e) {
                        throw new InvalidDataException(String.format("Failed to deserialize store json: %s", jsonStore), e);
                    }
                } else {
                    listener.onComplete(null);
                }
            }

            @Override
            public void onException(Exception e) {
                listener.onException(e);
            }
        });
    }


    public void insertStore(Store store, final Listener<Void, Exception> listener) {
        String storeKey = getStoreKey(store.getStoreId());
        logger.debug("({}) insertStore", store);
        try {
            String jsonStore = mapper.writeValueAsString(store);
            asyncCouchbaseWrapper.set(storeKey, jsonStore, new SetListener(asyncCouchbaseWrapper, storeKey, jsonStore) {
                @Override
                public void process() {
                    listener.onComplete(null);
                }

                @Override
                public void onException(Exception e) {
                    listener.onException(e);
                }
            });
        } catch (IOException e) {
            listener.onException(e);
        }
    }

}
