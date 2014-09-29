package com.tesco.services.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * <p>
 *     Class to build and maintain the product document. The class works in closely coupled
 * with ProductRepository and ProductVariant classes.
 * The value of TPNB is taken as the token and the keyword PRODUCT_ is prefixed to build the
 * key for the document. E.g., PRODUCT_<TPNB> and the corresponding value is the complete
 * document which will have all the details of the product.
 * </p>
 *
 * @return Returns an instance of the Product class.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Product implements PriceVisitable, Serializable {
    @JsonProperty
    private String tpnb;

    @JsonProperty
    private String last_updated_date;

    public String getLast_updated_date() {
        return last_updated_date;
    }

    public void setLast_updated_date(String last_updated_date) {
        this.last_updated_date = last_updated_date;
    }

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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Product product = (Product) o;

        if ((!tpnb.equals(product.tpnb)) ||
                (!last_updated_date.equals(product.last_updated_date)) ||
                (!tpncToProductVariant.equals(product.tpncToProductVariant))){
            return false;
        }

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
                "last_updated_date='" + last_updated_date + '\'' +
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

    public Map<String,ProductVariant> getTpncToProductVariant() {
        return tpncToProductVariant;
    }
}
