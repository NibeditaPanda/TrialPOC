package com.tesco.services.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ProductVariant implements PriceVisitable, Serializable {
    private String tpnc;
    private Map<Integer, SaleInfo> zoneIdToSaleInfo = new HashMap<>();

    public ProductVariant(String tpnc) {
        this.tpnc = tpnc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductVariant productVariant = (ProductVariant) o;

        if (!tpnc.equals(productVariant.tpnc)) return false;
        if (!zoneIdToSaleInfo.equals(productVariant.zoneIdToSaleInfo)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tpnc.hashCode();
        result = 31 * result + zoneIdToSaleInfo.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ProductVariant{" +
                "tpnc='" + tpnc + '\'' +
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
}
