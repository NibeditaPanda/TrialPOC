package com.tesco.services.core;

import java.util.HashMap;
import java.util.Map;

public class Product {
    private String tpnb;

    public Product(String tpnb) {
        this.tpnb = tpnb;
    }

    private Map<String, ProductVariant> tpncToProductVariant = new HashMap<>();

    public void addProductVariant(ProductVariant productVariant) {
        tpncToProductVariant.put(productVariant.getTPNC(), productVariant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        if (!tpnb.equals(product.tpnb)) return false;
        if (!tpncToProductVariant.equals(product.tpncToProductVariant)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tpnb.hashCode();
        result = 31 * result + tpncToProductVariant.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Product{" +
                "tpnb='" + tpnb + '\'' +
                ", tpncToProductVariant=" + tpncToProductVariant +
                '}';
    }

    public ProductVariant getProductVariantByTPNC(String tpnc) {
        return tpncToProductVariant.get(tpnc);
    }

    public String getTPNB() {
        return tpnb;
    }
}
