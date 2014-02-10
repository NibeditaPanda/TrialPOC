package com.tesco.services.core;

import com.google.common.base.Optional;

import java.util.*;

public class ProductPriceBuilder implements ProductPriceVisitor {
    public static final String VARIANTS = "variants";
    public static final String TPNB = "tpnb";
    public static final String TPNC = "tpnc";
    public static final String PRICE = "price";
    public static final String PROMO_PRICE = "promoPrice";
    public static final String CURRENCY = "currency";

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
        priceInfo.put(VARIANTS, new ArrayList<Map<String, String>>());
    }

    @Override
    public void visit(ProductVariant productVariant) {
        List<Map<String, String>> variants = (List<Map<String, String>>) priceInfo.get(VARIANTS);

        SaleInfo priceZoneSaleInfo = priceZoneId.isPresent() ? productVariant.getSaleInfo(priceZoneId.get()) : null;
        SaleInfo promoZoneSaleInfo = promoZoneId.isPresent() ? productVariant.getSaleInfo(promoZoneId.get()) : null;

        if (priceZoneSaleInfo == null && promoZoneSaleInfo == null) return;

        Map<String, String> variantInfo = new LinkedHashMap<>();
        variantInfo.put(TPNC, productVariant.getTPNC());
        variantInfo.put(CURRENCY, currency);
        variants.add(variantInfo);

        if (priceZoneSaleInfo != null ) {
            variantInfo.put(PRICE, priceZoneSaleInfo.getPrice());
        }

        if (promoZoneSaleInfo != null) {
            variantInfo.put(PROMO_PRICE, promoZoneSaleInfo.getPrice());
        }
    }

    public Map<String, Object> getPriceInfo() {
        return priceInfo;
    }
}
