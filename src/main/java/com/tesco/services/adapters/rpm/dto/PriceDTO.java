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
}
