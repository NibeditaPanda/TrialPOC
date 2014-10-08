package com.tesco.services.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.listeners.GetListener;
import com.tesco.couchbase.listeners.Listener;
import com.tesco.couchbase.listeners.SetListener;
import com.tesco.services.core.Product;
import com.tesco.services.core.Store;
import com.tesco.services.exceptions.InvalidDataException;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class AsyncReadWriteProductRepository {

    private final Logger logger = getLogger(getClass().getName());

    private ObjectMapper mapper;
    private AsyncCouchbaseWrapper couchbaseWrapper;

    public AsyncReadWriteProductRepository(AsyncCouchbaseWrapper couchbaseWrapper, ObjectMapper mapper) {
        this.couchbaseWrapper = couchbaseWrapper;
        this.mapper = mapper;
    }

    public void upsert(final Product product,/* List<ProductField> oldProductFields,*/ final Listener<Void, Exception> listener) {

            insertProduct(product, new Listener<Void, Exception>() {
                @Override
                public void onComplete(Void result) {
                    listener.onComplete(null);
                }
                @Override
                public void onException(Exception e) {
                    listener.onException(e);
                }
            });
        }

    public void getByTPNB(String tpnb, final Listener<Product, Exception> listener) {
        final String key = tpnb;
        couchbaseWrapper.get(key, new GetListener(couchbaseWrapper, key) {

            @Override
            protected void process(Object jsonProduct) {
                if (jsonProduct != null) {
                    try {
                        Product product = mapper.readValue((String) jsonProduct, Product.class);
                        listener.onComplete(product);
                    } catch (IOException e) {
                        throw new InvalidDataException(String.format("Failed to deserialize product json: %s", jsonProduct), e);
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

    public void insertProduct(Product product, final Listener<Void, Exception> listener) {
        String productKey = getProductKey(product.getTPNB());
        if(logger.isDebugEnabled()) {
            logger.debug("({}) insertProduct", product);
        }
        try {
            String jsonProduct = mapper.writeValueAsString(product);
            couchbaseWrapper.set(getProductKey(product.getTPNB()), jsonProduct, new SetListener(couchbaseWrapper, productKey, jsonProduct) {
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

    private String getProductKey(String tpnb) {
        return String.format("PRODUCT_%s", tpnb);
    }

    public void insertStore(Store store, final Listener<Void, Exception> listener) {
        String storeKey = getStoreKey(store.getStoreId());
        if(logger.isDebugEnabled()) {
            logger.debug("({}) insertProduct", store);
        }
        try {
            String jsonStore = mapper.writeValueAsString(store);
            couchbaseWrapper.set(storeKey, jsonStore, new SetListener(couchbaseWrapper, storeKey, jsonStore) {
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

    private String getStoreKey(String storeId) {
        return String.format("STORE_%s", storeId);
    }

}
