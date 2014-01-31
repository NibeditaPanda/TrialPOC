package com.tesco.services.adapters.rpm.dto;

public class PriceDTO {
    private String itemNumber;
    private String zoneId;
    private String price;

    public PriceDTO(String itemNumber, String zoneId, String price) {
        this.itemNumber = itemNumber;
        this.zoneId = zoneId;
        this.price = price;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getPrice() {
        return price;
    }

    public String getTPNB() {
        return itemNumber.split("-")[0];
    }

    public String getTPNC() {
        return itemNumber;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PriceDTO priceDTO = (PriceDTO) o;

        if (!itemNumber.equals(priceDTO.itemNumber)) return false;
        if (!price.equals(priceDTO.price)) return false;
        if (!zoneId.equals(priceDTO.zoneId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = itemNumber.hashCode();
        result = 31 * result + zoneId.hashCode();
        result = 31 * result + price.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PriceDTO{" +
                "itemNumber='" + itemNumber + '\'' +
                ", zoneId='" + zoneId + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
