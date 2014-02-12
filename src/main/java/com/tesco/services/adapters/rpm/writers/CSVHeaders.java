package com.tesco.services.adapters.rpm.writers;

public interface CSVHeaders {

    static interface Price {
        String TPNB = "ITEM";
        String PRICE_ZONE_ID = "PRICE_ZONE_ID";
        String PRICE_ZONE_PRICE = "SELLING_RETAIL";
        String[] PRICE_ZONE_HEADERS = {TPNB, PRICE_ZONE_ID, PRICE_ZONE_PRICE};

        String PROMO_ZONE_ID = "PROMO_ZONE_ID";
        String PROMO_ZONE_PRICE = "SIMPLE_PROMO_RETAIL";
        String[] PROMO_ZONE_HEADERS = {TPNB, PROMO_ZONE_ID, PROMO_ZONE_PRICE};
    }

    static interface StoreZone {
        String STORE_ID = "STORE";
        String ZONE_ID = "ZONE_ID";
        String CURRENCY_CODE = "CURRENCY_CODE";
        String ZONE_TYPE = "ZONE_TYPE";
        String[] HEADERS = {STORE_ID, ZONE_ID, CURRENCY_CODE, ZONE_TYPE};
    }
}
