package com.tesco.services.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
/*import com.tesco.adapters.core.InvalidDataException;
import com.tesco.adapters.core.ProductKeyGenerator;
import com.tesco.adapters.core.models.Product;
import com.tesco.adapters.core.models.ProductKey;
import com.tesco.adapters.core.models.fields.ProductField;*/
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.listeners.*;
//import com.tesco.services.ProductQueryResult;
import com.tesco.services.core.Store;
import com.tesco.services.exceptions.InvalidDataException;
import org.slf4j.Logger;
import com.tesco.services.core.Product;

import java.io.IOException;
import java.util.*;

import static com.google.common.base.Optional.fromNullable;
import static org.slf4j.LoggerFactory.getLogger;

public class AsyncReadWriteProductRepository {

    private final Logger logger = getLogger(getClass().getName());

    private ObjectMapper mapper;
    //private final ProductKeyGenerator productKeyGenerator;
    private AsyncCouchbaseWrapper couchbaseWrapper;
    //private final AsyncIndexRepository indexRepository;

    public AsyncReadWriteProductRepository(AsyncCouchbaseWrapper couchbaseWrapper, /*AsyncIndexRepository indexRepository,*/ ObjectMapper mapper/*, ProductKeyGenerator productKeyGenerator*/) {
        this.couchbaseWrapper = couchbaseWrapper;
      //  this.indexRepository = indexRepository;
        this.mapper = mapper;
        //this.productKeyGenerator = productKeyGenerator;
    }


    public void upsert(final Product product,/* List<ProductField> oldProductFields,*/ final Listener<Void, Exception> listener) {

       /* if (product.getUniqueKey() != null) {
            removeIndexes(product, oldProductFields, new Listener<Void, Exception>() {
                @Override
                public void onComplete(Void result) {
                    insertProduct(product, new Listener<Void, Exception>() {
                        @Override
                        public void onComplete(Void result) {
                            updateIndexes(product, new Listener<Void, Exception>() {
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

                        @Override
                        public void onException(Exception e) {
                            listener.onException(e);
                        }
                    });

                }

                @Override
                public void onException(Exception e) {
                    listener.onException(e);
                }
            });

         else {}*/
          //  final Product newProduct = productKeyGenerator.assignTo(product);
            insertProduct(product, new Listener<Void, Exception>() {
                @Override
                public void onComplete(Void result) {
                    /*updateIndexes(newProduct, new Listener<Void, Exception>() {
                        @Override
                        public void onComplete(Void result) {
                            listener.onComplete(null);
                        }

                        @Override
                        public void onException(Exception e) {
                            listener.onException(e);
                        }
                    });*/listener.onComplete(null);
                }

                @Override
                public void onException(Exception e) {
                    listener.onException(e);
                }
            });
        }


/*

    public void deleteProduct(final ProductKey productKey, final Listener<Void, Exception> listener) {
        getProduct(productKey, new Listener<Product, Exception>() {
            @Override
            public void onComplete(Product product) {
                List<ProductField> productFields = getProductFields(product);
                removeIndexes(productKey, productFields, new Listener<Void, Exception>() {
                    @Override
                    public void onComplete(Void result) {
                        couchbaseWrapper.delete(productKey.getValue(), new DeleteListener(couchbaseWrapper, productKey.getValue()) {
                            @Override
                            public void process() {
                                listener.onComplete(null);
                            }

                            @Override
                            public void onException(Exception e) {
                                listener.onException(e);
                            }
                        });
                    }

                    @Override
                    public void onException(Exception e) {
                        listener.onException(e);
                    }
                });
            }

            @Override
            public void onException(Exception e) {
                listener.onException(e);
            }
        });
    }
*/
/*

    public void getProducts(final Optional<Integer> pageSize, final Optional<Integer> pageNumber, final Listener<ProductQueryResult, Exception> listener, ProductField... fields) {
        searchByMultipleValues(new Listener<KeyResults, Exception>() {
            @Override
            public void onComplete(final KeyResults keyResults) {
                final int total = keyResults.getProductKeys().size();
                List<ProductKey> productKeys = paginate(keyResults.productKeys, pageSize, pageNumber);

                if (!productKeys.isEmpty()) {
                    getProducts(productKeys, new Listener<List<Product>, Exception>() {
                        @Override
                        public void onComplete(List<Product> products) {
                            listener.onComplete(new ProductQueryResult(products, total, pageSize.orNull(), pageNumber.orNull(), keyResults.getMissingKeys()));
                        }

                        @Override
                        public void onException(Exception e) {
                            listener.onException(e);
                        }
                    });
                } else {
                    listener.onComplete(new ProductQueryResult(Collections.<Product>emptyList(), total, pageSize.orNull(), pageNumber.orNull(), keyResults.getMissingKeys()));
                }
            }

            @Override
            public void onException(Exception e) {
                listener.onException(e);
            }
        }, fields);
    }
*/
/*

    public void getProduct(ProductField field, final Listener<Product,Exception> listener ) {

        getProducts(fromNullable(1), fromNullable(1), new Listener<ProductQueryResult, Exception>() {
            @Override
            public void onComplete(ProductQueryResult productQueryResult) {
                List<Product> products = productQueryResult.products;
                if (products != null && !products.isEmpty()) {
                    listener.onComplete(products.get(0));
                } else {
                    listener.onComplete(null);
                }
            }

            @Override
            public void onException(Exception e) {
                listener.onException(e);
            }
        }, field);
    }
*/

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
/*

    private void getProducts(final List<ProductKey> productKeys, final Listener<List<Product>, Exception> listener) {
        final ArrayList<String> stringProductKeys = new ArrayList<>();
        for (ProductKey productKey : productKeys){
            stringProductKeys.add(productKey.getValue());
        }


        couchbaseWrapper.getBulk(stringProductKeys, new BulkGetListener(couchbaseWrapper, stringProductKeys) {
            @Override
            protected void process(Map<String, ?> jsonProducts) {
                List<Product> products = new ArrayList<>();
                for (ProductKey productKey : productKeys) {
                    if (jsonProducts.containsKey(productKey.getValue())) {
                        Object jsonProduct = jsonProducts.get(productKey.getValue());
                        try {
                            Product product = mapper.readValue((String) jsonProduct, Product.class);
                            products.add(product);
                        } catch (IOException e) {
                            throw new InvalidDataException(String.format("Failed to deserialize product json: %s", jsonProduct), e);
                        }
                    } else {
                        throw new InvalidDataException(String.format("Product with key '%s' expected but not found in Couchbase", productKey));
                    }
                }
                listener.onComplete(products);
            }

            @Override
            public void onException(Exception e) {
                listener.onException(e);
            }
        });
    }
*/
/*

    private List<ProductKey> paginate(List<ProductKey> productKeys, Optional<Integer> pageSize, Optional<Integer> pageNumber) {
        if (pageSize.isPresent() && pageNumber.isPresent() && pageSize.get() != 0 && pageNumber.get() != 0) {
            int total = productKeys.size();
            int fromIndex = (pageNumber.get() - 1) * pageSize.get();
            int toIndex = fromIndex + pageSize.get();
            if (toIndex > total) toIndex = total;
            if (fromIndex < 0) fromIndex = 0;

            if (fromIndex < total && toIndex >= 0) {
                return productKeys.subList(fromIndex, toIndex);
            }
        }
        return productKeys;
    }

    private void searchByMultipleValues(final Listener<KeyResults,Exception> listener, final ProductField... values) {
        final Set<ProductKey> allProductKeys = new LinkedHashSet<>();
        final List<ProductField> missingKeys = new ArrayList<>();

        final List<String> indexKeys = new ArrayList<>();
        for (ProductField value : values) {
            indexKeys.add(value.getDBKey());
        }
        couchbaseWrapper.getBulk(indexKeys, new BulkGetListener(couchbaseWrapper, indexKeys) {
            @Override
            protected void process(Map<String, ?> map) {
                for (ProductField productField : values) {
                    if (map.containsKey(productField.getDBKey())) {
                        Object jsonProductKeys = map.get(productField.getDBKey());
                        try {
                            List<ProductKey> productKeys = Lists.newArrayList(mapper.readValue((String) jsonProductKeys, ProductKey[].class));
                            Collections.sort(productKeys);
                            allProductKeys.addAll(productKeys);
                        } catch (IOException e) {
                            throw new InvalidDataException(String.format("Failed to deserialize index document json: %s", jsonProductKeys), e);
                        }
                    } else {
                        missingKeys.add(productField);
                    }
                }

                listener.onComplete(new KeyResults(new ArrayList<>(allProductKeys), missingKeys));
            }

            @Override
            public void onException(Exception e) {
                listener.onException(e);
            }
        });
    }

    class KeyResults {

        private List<ProductKey> productKeys;
        private List<ProductField> missingKeys;

        KeyResults(List<ProductKey> productKeys, List<ProductField> missingKeys) {
            this.productKeys = productKeys;
            this.missingKeys = missingKeys;
        }

        public List<ProductKey> getProductKeys() {
            return productKeys;
        }

        public List<ProductField> getMissingKeys() {
            return missingKeys;
        }

    }
*/

    public void insertProduct(Product product, final Listener<Void, Exception> listener) {
        String productKey = getProductKey(product.getTPNB());
        logger.debug("({}) insertProduct", product);
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
          logger.debug("({}) insertProduct", store);
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

/*
    private Listener<Void, Exception> updateIndexesInChain(final ProductKey productKey, final List<ProductField> productFields, final Listener<Void, Exception> listener) {
        return new Listener<Void, Exception>() {
            @Override
            public void onComplete(Void result) {
                if (productFields.isEmpty()) {
                    listener.onComplete(null);
                } else {
                    List<ProductField> tail = productFields.subList(1, productFields.size());
                    indexRepository.updateIndex(productKey, productFields.get(0), updateIndexesInChain(productKey, tail, listener));
                }
            }

            @Override
            public void onException(Exception e) {
                listener.onException(e);
            }
        };
    }*/
/*

    private void updateIndexes(final Product product, final Listener<Void, Exception> listener) {

        ProductKey productKey = product.getUniqueKey();

        List<ProductField> productFields = Lists.newArrayList();
        if (product.getItemNumber() != null) productFields.add(product.getItemNumber());
        if (product.getTpnb() != null) productFields.add(product.getTpnb());
        if (product.getTpnc() != null) productFields.add(product.getTpnc());
        if (product.getGtins() != null) productFields.addAll(product.getGtins());

        updateIndexesInChain(productKey, productFields, listener).onComplete(null);
    }
*/
/*
    private Listener<Void, Exception> removeIndexesInChain(final ProductKey productKey, final List<ProductField> productFields, final Listener<Void, Exception> listener) {
        return new Listener<Void, Exception>() {
            @Override
            public void onComplete(Void result) {
                if (productFields.isEmpty()) {
                    listener.onComplete(null);
                } else {
                    List<ProductField> tail = productFields.subList(1, productFields.size());
                    indexRepository.removeIndex(productKey, productFields.get(0), removeIndexesInChain(productKey, tail, listener));
                }
            }

            @Override
            public void onException(Exception e) {
                listener.onException(e);
            }
        };
    }*/
/*

    private void removeIndexes(Product updatedProduct, List<ProductField> oldProductFields, final Listener<Void, Exception> listener) {
        logger.debug("({}) removeIndexes", updatedProduct.getUniqueKey());

        removeUnchanged(updatedProduct.getItemNumber(), oldProductFields);
        removeUnchanged(updatedProduct.getTpnb(), oldProductFields);
        removeUnchanged(updatedProduct.getTpnc(), oldProductFields);

        if (!oldProductFields.isEmpty()) {
            removeIndexes(updatedProduct.getUniqueKey(), oldProductFields, listener);
        } else {
            listener.onComplete(null);
        }
    }
*/
/*

    private void removeIndexes(ProductKey productKey, List<ProductField> productFields, Listener<Void, Exception> listener) {
        removeIndexesInChain(productKey, productFields, listener).onComplete(null);
    }

    private void removeUnchanged(ProductField productField, List<ProductField> oldProductFields) {
        int index = oldProductFields.indexOf(productField);
        if (index >= 0) {
            oldProductFields.remove(index);
        }
    }
*/
/*
    private List<ProductField> getProductFields(Product product) {
        List<ProductField> productFields = new ArrayList<>();

        if (product.getItemNumber() != null) productFields.add(product.getItemNumber());
        if (product.getTpnb() != null) productFields.add(product.getTpnb());
        if (product.getTpnc() != null) productFields.add(product.getTpnc());
        if (product.getGtins() != null) productFields.addAll(product.getGtins());

        return productFields;
    }*/
}
