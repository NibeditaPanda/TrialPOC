package com.tesco.services.adapters.rpm.writers;

public interface CSVHeaders {
    String TPNB = "ITEM";
    String PRICE_ZONE_ID = "PRICE_ZONE_ID";
    String PRICE_ZONE_PRICE = "SELLING_RETAIL";
    String PROMO_ZONE_ID = "PROMO_ZONE_ID";
    String PROMO_ZONE_PRICE = "SIMPLE_PROMO_RETAIL";

    String[] PRICE_ZONE_HEADERS = {TPNB, PRICE_ZONE_ID, PRICE_ZONE_PRICE};
    String[] PROMO_ZONE_HEADERS = {TPNB, PROMO_ZONE_ID, PROMO_ZONE_PRICE};
}
