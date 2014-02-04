package com.tesco.services.core;

public class SaleInfo {
    private int zoneId;
    private String price;

    public SaleInfo(int zoneId, String price) {
        this.zoneId = zoneId;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SaleInfo saleInfo = (SaleInfo) o;

        if (!price.equals(saleInfo.price)) return false;
        if (zoneId != saleInfo.zoneId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = zoneId;
        result = 31 * result + price.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SaleInfo{" +
                "zoneId='" + zoneId + '\'' +
                ", price='" + price + '\'' +
                '}';
    }

    public int getZoneId() {
        return zoneId;
    }
}
