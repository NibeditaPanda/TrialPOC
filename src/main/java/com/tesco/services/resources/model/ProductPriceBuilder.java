package com.tesco.services.resources.model;

import com.google.common.base.Optional;
import com.tesco.services.core.*;
import com.tesco.services.utility.Dockyard;

import java.util.*;

public class ProductPriceBuilder implements ProductPriceVisitor {
    public static final String VARIANTS = "variants";
    public static final String TPNB = "tpnb";
    public static final String TPNC = "tpnc";
    public static final String PRICE = "price";
    /**Modified By Nibedita - PS-118- Positive Scenario
     * Given the  price IDL ,When the price rest calls are requested, then the response JSON should be as per format mentioned in IDL  */
    public static final String PROMO_PRICE = "promoprice";
    /**Modified By Nibedita - PS-173
     * Given the  price IDL ,When the price rest calls are requested and selling UOM filed is available in price_zone.csv,
     * then the response JSON should display selling UOM for the tpnc's in line with IDL  */
    public static final String SELLING_UOM = "sellingUOM";


    public static final String PROMOTION_INFO = "promotions";
    public static final String CURRENCY = "currency";
    public static final String OFFER_NAME = "offerName";
    public static final String EFFECTIVE_DATE = "effectiveDate";
    public static final String END_DATE = "endDate";
    public static final String CUSTOMER_FRIENDLY_DESCRIPTION_1 = "customerFriendlyDescription1";
    private static final String CUSTOMER_FRIENDLY_DESCRIPTION_2 = "customerFriendlyDescription2";

    private Map<String, Object> priceInfo = new LinkedHashMap<>();
    private Optional<Integer> priceZoneId;
    private Optional<Integer> promoZoneId;
    private String currency;

    public ProductPriceBuilder(Optional<Integer> priceZoneId, Optional<Integer> promoZoneId, String currency) {
        this.priceZoneId = priceZoneId;
        this.promoZoneId = promoZoneId;
        this.currency = currency;
    }

    @Override
    public void visit(Product product) {
        priceInfo.put(TPNB, product.getTPNB());
        priceInfo.put(VARIANTS, new ArrayList<Map<String, Object>>());
        /**Modified By Nibedita - PS-118- Positive Scenario
         * Given the  price IDL ,When the price rest calls are requested, then the response JSON should be as per format mentioned in IDL  */
        priceInfo.put(PROMOTION_INFO, new ArrayList<Map<String, String>>());
    }

    @Override
    public void visit(ProductVariant productVariant) {
        List<Map<String, Object>> variants = (List<Map<String, Object>>) priceInfo.get(VARIANTS);
        List<Map<String, String>> promotions = (List<Map<String, String>>) priceInfo.get(PROMOTION_INFO);

        SaleInfo priceZoneSaleInfo = priceZoneId.isPresent() ? productVariant.getSaleInfo(priceZoneId.get()) : null;
        SaleInfo promoZoneSaleInfo = promoZoneId.isPresent() ? productVariant.getSaleInfo(promoZoneId.get()) : null;

        if (priceZoneSaleInfo == null && promoZoneSaleInfo == null)
            return;

        Map<String, Object> variantInfo = new LinkedHashMap<>();
        variantInfo.put(TPNC, productVariant.getTPNC());
        variantInfo.put(CURRENCY, currency);
        /**Modified By Nibedita - PS-173
         * Given the  price IDL ,When the price rest calls are requested and selling UOM filed is available in price_zone.csv,
         * then the response JSON should display selling UOM for the tpnc's in line with IDL  */
        variantInfo.put(SELLING_UOM, productVariant.getSellingUOM());
        variants.add(variantInfo);
        /*Modified by Sushil PS-178 to configure decimal places for price - Start*/
        if (priceZoneSaleInfo != null ) {
            variantInfo.put(PRICE, Dockyard.priceScaleRoundHalfUp(currency, priceZoneSaleInfo.getPrice()));
        }
        /**Modified By Nibedita - PS-118- Positive Scenario
         * Given the  price IDL ,When the price rest calls are requested, then the response JSON should be as per format mentioned in IDL  */
        if (promoZoneSaleInfo != null) {
            variantInfo.put(PROMO_PRICE, Dockyard.priceScaleRoundHalfUp(currency, promoZoneSaleInfo.getPrice()));
        }
        /*Modified by Sushil PS-178 to configure decimal places for price - End*/
        /**Modified By Nibedita - PS-118- Positive Scenario
         * Given the  price IDL ,When the price rest calls are requested, then the response JSON should be as per format mentioned in IDL  */
        if (promoZoneSaleInfo == null) {
            variantInfo.put(PROMO_PRICE, null);
        }
        if (promoZoneSaleInfo != null && promotions.size()==0) {
            addPromotionInfo(promoZoneSaleInfo,promotions);
        }
    }

    /**Modified By Nibedita - PS-118- Positive Scenario
     * Given the  price IDL ,When the price rest calls are requested, then the response JSON should be as per format mentioned in IDL  */
    private void addPromotionInfo(SaleInfo promoZoneSaleInfo,List<Map<String, String>> promotion_info) {
        Collection<Promotion> promotions = promoZoneSaleInfo.getPromotions();

        if (promotions.isEmpty())
            return;

        for (Promotion promotion : promotions) {
            Map<String, String> promotionInfoMap = new LinkedHashMap<>();
            promotionInfoMap.put(OFFER_NAME, promotion.getOfferName());
            promotionInfoMap.put(EFFECTIVE_DATE, promotion.getEffectiveDate());
            promotionInfoMap.put(END_DATE, promotion.getEndDate());
            promotionInfoMap.put(CUSTOMER_FRIENDLY_DESCRIPTION_1, promotion.getCFDescription1());
            promotionInfoMap.put(CUSTOMER_FRIENDLY_DESCRIPTION_2, promotion.getCFDescription2());
            promotion_info.add(promotionInfoMap);
        }

    }

    public Map<String, Object> getPriceInfo() {
        return priceInfo;
    }
}
