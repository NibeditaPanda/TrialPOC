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

    public String getLast_updated_dateTime() {
        return last_updated_dateTime;
    }

    public void setLast_updated_dateTime(String last_updated_dateTime) {
        this.last_updated_dateTime = last_updated_dateTime;
    }
    /*Modified by Nibedita - for adding last_updated_dateTime field in Product JSON document while import - Story 114 -Start*/
    @JsonProperty
     private String last_updated_dateTime;
    /*Modified by Nibedita - for adding last_updated_dateTime field in Product JSON document while import - Story 114 -End*/
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
        /*Modified by Nibedita - for adding last_updated_dateTime field in Product JSON document while import - Story 114 -Start*/
        //if (!last_updated_dateTime.equals(product.last_updated_dateTime)) return false;
        /*Modified by Nibedita - for adding last_updated_dateTime field in Product JSON document while import - Story 114 -End*/
        if (!tpncToProductVariant.equals(product.tpncToProductVariant)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tpnb.hashCode();
        result = 31 * result + tpncToProductVariant.hashCode();
        return result;
    }
/*Modified by Nibedita - for adding last_updated_dateTime field in Product JSON document while import - Story 114 -Start*/
    @Override
    public String toString() {
        return "Product{" +
                "tpnb='" + tpnb + '\'' +
                "last_updated_dateTime='" + last_updated_dateTime + '\'' +
                ", tpncToProductVariant=" + tpncToProductVariant +
                '}';
    }
 /*Modified by Nibedita - for adding last_updated_dateTime field in Product JSON document while import - Story 114 -End*/
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
