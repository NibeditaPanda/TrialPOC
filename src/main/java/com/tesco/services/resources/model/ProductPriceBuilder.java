package com.tesco.services.resources.model;

import com.google.common.base.Optional;
import com.tesco.services.core.Product;
import com.tesco.services.core.ProductPriceVisitor;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.SaleInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductPriceBuilder implements ProductPriceVisitor {
    public static final String VARIANTS = "variants";
    public static final String TPNB = "tpnb";
    public static final String TPNC = "tpnc";
    public static final String PRICE = "price";
    public static final String PROMO_PRICE = "promoPrice";
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
    }

    @Override
    public void visit(ProductVariant productVariant) {
        List<Map<String, Object>> variants = (List<Map<String, Object>>) priceInfo.get(VARIANTS);

        SaleInfo priceZoneSaleInfo = priceZoneId.isPresent() ? productVariant.getSaleInfo(priceZoneId.get()) : null;
        SaleInfo promoZoneSaleInfo = promoZoneId.isPresent() ? productVariant.getSaleInfo(promoZoneId.get()) : null;

        if (priceZoneSaleInfo == null && promoZoneSaleInfo == null) return;

        Map<String, Object> variantInfo = new LinkedHashMap<>();
        variantInfo.put(TPNC, productVariant.getTPNC());
        variantInfo.put(CURRENCY, currency);
        variants.add(variantInfo);

        if (priceZoneSaleInfo != null ) {
            variantInfo.put(PRICE, priceZoneSaleInfo.getPrice());
        }

        if (promoZoneSaleInfo != null) {
            addPromotionInfo(promoZoneSaleInfo, variantInfo);
        }
    }

    private void addPromotionInfo(SaleInfo promoZoneSaleInfo, Map<String, Object> variantInfo) {
        variantInfo.put(PROMO_PRICE, promoZoneSaleInfo.getPrice());
        Collection<Promotion> promotions = promoZoneSaleInfo.getPromotions();

        if (promotions.isEmpty()) return;

        List<Map<String, String>> promotionMaps = new ArrayList<>();

        for (Promotion promotion : promotions) {
            Map<String, String> promotionInfoMap = new LinkedHashMap<>();
            promotionInfoMap.put(OFFER_NAME, promotion.getOfferName());
            promotionInfoMap.put(EFFECTIVE_DATE, promotion.getEffectiveDate());
            promotionInfoMap.put(END_DATE, promotion.getEndDate());
            promotionInfoMap.put(CUSTOMER_FRIENDLY_DESCRIPTION_1, promotion.getCFDescription1());
            promotionInfoMap.put(CUSTOMER_FRIENDLY_DESCRIPTION_2, promotion.getCFDescription2());
            promotionMaps.add(promotionInfoMap);
        }

        variantInfo.put(PROMOTION_INFO, promotionMaps);
    }

    public Map<String, Object> getPriceInfo() {
        return priceInfo;
    }
}
