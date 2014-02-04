package com.tesco.services.core;

import java.util.*;

public class ProductPriceBuilder implements ProductPriceVisitor {
    public static final String VARIANTS = "variants";
    public static final String TPNB = "tpnb";
    public static final String TPNC = "tpnc";
    public static final String PRICE = "price";

    private Map<String, Object> priceInfo = new LinkedHashMap<>();
    private int priceZoneId;

    public ProductPriceBuilder(int priceZoneId) {
        this.priceZoneId = priceZoneId;
    }

    @Override
    public void visit(Product product) {
        priceInfo.put(TPNB, product.getTPNB());
        priceInfo.put(VARIANTS, new ArrayList<Map<String, String>>());
    }

    @Override
    public void visit(ProductVariant productVariant) {
        List<Map<String, String>> variants = (List<Map<String, String>>) priceInfo.get(VARIANTS);

        SaleInfo saleInfo = productVariant.getSaleInfo(priceZoneId);
        if (saleInfo != null) {
            Map<String, String> variantInfo = new LinkedHashMap<>();
            variantInfo.put(TPNC, productVariant.getTPNC());
            variantInfo.put(PRICE, saleInfo.getPrice());
            variants.add(variantInfo);
        }
    }

    public Map<String, Object> getPriceInfo() {
        return priceInfo;
    }
}
