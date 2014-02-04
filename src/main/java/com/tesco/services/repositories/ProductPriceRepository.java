package com.tesco.services.repositories;

import com.tesco.services.core.Product;
import org.infinispan.Cache;

public class ProductPriceRepository {

    private Cache<String, Product> productPriceCache;

    public ProductPriceRepository(Cache<String, Product> productPriceCache) {
        this.productPriceCache = productPriceCache;
    }

    public Product getByTPNB(String tpnb) {
        return productPriceCache.get(tpnb);
    }

    public void put(Product product) {
        productPriceCache.put(product.getTPNB(), product);
    }
}
