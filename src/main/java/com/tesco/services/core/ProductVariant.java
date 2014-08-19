package com.tesco.services.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ProductVariant implements PriceVisitable, Serializable {
    @JsonProperty
    private String tpnc;
    /**Added By Nibedita/Mukund - PS-112
     * Given the  price End Point,When the price rest calls are requested, then the response JSON should contain selling UOM for the tpnc line with IDL  */
    @JsonProperty
    private String sellingUOM;
    @JsonProperty
    private Map<Integer, SaleInfo> zoneIdToSaleInfo = new HashMap<>();

    public ProductVariant(String tpnc) {
        this.tpnc = tpnc;
    }
    public ProductVariant(){

    }
    /**Modified By Nibedita/Mukund - PS-112
     * Given the  price End Point,When the price rest calls are requested, then the response JSON should contain selling UOM for the tpnc line with IDL  */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductVariant productVariant = (ProductVariant) o;

        if (!tpnc.equals(productVariant.tpnc)) return false;
        if (!sellingUOM.equals(productVariant.sellingUOM)) return false;
        if (!zoneIdToSaleInfo.equals(productVariant.zoneIdToSaleInfo)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tpnc.hashCode();
        result = 31 * result + zoneIdToSaleInfo.hashCode();
        return result;
    }
    /**Modified By Nibedita/Mukund - PS-112
     * Given the  price End Point,When the price rest calls are requested, then the response JSON should contain selling UOM for the tpnc line with IDL  */
    @Override
    public String toString() {
        return "ProductVariant{" +
                "tpnc='" + tpnc + '\'' +
                "sellingUOM='" + sellingUOM + '\''+
                ", zoneIdToSaleInfo=" + zoneIdToSaleInfo +
                '}';
    }

    public void addSaleInfo(SaleInfo saleInfo) {
        zoneIdToSaleInfo.put(saleInfo.getZoneId(), saleInfo);
    }

    public String getTPNC() {
        return tpnc;
    }

    @Override
    public void accept(ProductPriceVisitor visitor) {
        visitor.visit(this);
    }

    public SaleInfo getSaleInfo(int zoneId) {
        return zoneIdToSaleInfo.get(zoneId);
    }
    /**Added By Nibedita/Mukund - PS-112
     * Given the  price End Point,When the price rest calls are requested, then the response JSON should contain selling UOM for the tpnc line with IDL  */
    public String getSellingUOM() {
        return sellingUOM;
    }

    public void setSellingUOM(String sellingUOM) {
        this.sellingUOM = sellingUOM;
    }
}
