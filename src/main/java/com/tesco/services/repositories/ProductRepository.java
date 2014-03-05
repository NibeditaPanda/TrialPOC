package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.google.common.base.Optional;
import com.tesco.services.core.Product;

public class ProductRepository {

    private CouchbaseClient couchbaseClient;

    public ProductRepository(CouchbaseClient couchbaseClient) {
        this.couchbaseClient = couchbaseClient;
    }

    public Optional<Product> getByTPNB(String tpnb) {
        Product product = (Product) couchbaseClient.get(getProductKey(tpnb));
        return (product != null) ? Optional.of(product) : Optional.<Product>absent();
    }

    private String getProductKey(String tpnb) {
        return String.format("PRODUCT_%s", tpnb);
    }

    public void put(Product product) {
        couchbaseClient.set(getProductKey(product.getTPNB()), product);
    }
}
