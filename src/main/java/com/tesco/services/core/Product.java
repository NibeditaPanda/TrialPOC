package com.tesco.services.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Product implements PriceVisitable, Serializable {
    @JsonProperty
    private String tpnb;

    @JsonProperty
    private Map<String, ProductVariant> tpncToProductVariant = new HashMap<>();

    public Product(String tpnb) {
        this.tpnb = tpnb;
    }
    public Product() {

    }

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

    @Override
    public void accept(ProductPriceVisitor productPriceVisitor) {
        productPriceVisitor.visit(this);

        for (ProductVariant productVariant : tpncToProductVariant.values()) {
            productVariant.accept(productPriceVisitor);
        }
    }
}
