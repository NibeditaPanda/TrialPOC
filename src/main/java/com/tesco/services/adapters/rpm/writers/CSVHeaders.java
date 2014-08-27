package com.tesco.services.adapters.rpm.writers;

public interface CSVHeaders {

    static interface Price {
        String ITEM = "ITEM";
        String TPNC = "TPNC";
        String PRICE_ZONE_ID = "PRICE_ZONE_ID";
        String PRICE_ZONE_PRICE = "SELLING_RETAIL";
        /**Added/Modified By Nibedita/Mukund - PS-112
         * Given the  price End Point,When the price rest calls are requested, then the response JSON should contain selling UOM for the tpnc line with IDL  */
        String SELLING_UOM = "SELLING_UOM";
        String[] PRICE_ZONE_HEADERS = {ITEM,TPNC, PRICE_ZONE_ID, PRICE_ZONE_PRICE,SELLING_UOM};

        String PROMO_ZONE_ID = "PROMO_ZONE_ID";
        String PROMO_ZONE_PRICE = "SIMPLE_PROMO_RETAIL";
        String[] PROMO_ZONE_HEADERS = {ITEM, TPNC, PROMO_ZONE_ID, PROMO_ZONE_PRICE};
    }

    static interface StoreZone {
        String STORE_ID = "STORE";
        String ZONE_ID = "ZONE_ID";
        String CURRENCY_CODE = "CURRENCY_CODE";
        String ZONE_TYPE = "ZONE_TYPE";
        String[] HEADERS = {STORE_ID, ZONE_ID, CURRENCY_CODE, ZONE_TYPE};
    }

    static interface PromoExtract {
        String ITEM = "ITEM";
        String TPNC = "TPNC";
        String ZONE_ID = "ZONE_ID";
        String OFFER_ID = "OFFER_ID";
        String OFFER_NAME = "OFFER_NAME";
        String START_DATE = "EFFECTIVE_DATE";
        String END_DATE = "END_DATE";
        String[] HEADERS = {ITEM, TPNC, ZONE_ID, OFFER_ID, OFFER_NAME, START_DATE, END_DATE};
    }

    static interface PromoDescExtract {
        String ITEM = "bpr_tpn";
        String ZONE_ID = "promo_zone";
        String OFFER_ID = "offer_id";
        String DESC1 = "desc_1";
        String DESC2 = "desc_2";
        String[] HEADERS = {ITEM, ZONE_ID, OFFER_ID, DESC1, DESC2};
    }
}
