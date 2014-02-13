package com.tesco.services.repositories;

import com.google.common.base.Optional;
import com.tesco.services.core.Product;
import org.infinispan.Cache;

public class ProductRepository {

    private Cache<String, Product> productPriceCache;

    public ProductRepository(Cache<String, Product> productPriceCache) {
        this.productPriceCache = productPriceCache;
    }

    public Optional<Product> getByTPNB(String tpnb) {
        Product product = productPriceCache.get(tpnb);
        return (product != null) ? Optional.of(product) : Optional.<Product>absent();
    }

    public void put(Product product) {
        productPriceCache.put(product.getTPNB(), product);
    }
}
