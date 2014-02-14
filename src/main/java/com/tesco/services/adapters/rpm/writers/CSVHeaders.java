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

    static interface PromoExtract {
        String TPNB = "ITEM";
        String ZONE_ID = "ZONE_ID";
        String OFFER_ID = "OFFER_ID";
        String OFFER_NAME = "OFFER_NAME";
        String START_DATE = "EFFECTIVE_DATE";
        String END_DATE = "END_DATE";
        String[] HEADERS = {TPNB, ZONE_ID, OFFER_ID, OFFER_NAME, START_DATE, END_DATE};
    }

    static interface PromoDescExtract {
        String TPNB = "ITEM";
        String ZONE_ID = "promo_zone";
        String OFFER_ID = "offer_id";
        String DESC1 = "desc_1";
        String DESC2 = "desc_2";
        String[] HEADERS = {TPNB, ZONE_ID, OFFER_ID, DESC1, DESC2};
    }
}
