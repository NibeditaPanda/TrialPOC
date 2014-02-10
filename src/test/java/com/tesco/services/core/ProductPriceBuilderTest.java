package com.tesco.services.core;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

public class ProductPriceBuilderTest {

    private final int priceZoneId = 1;
    private final int promoZoneId = 5;

    private final String tpnb = "01234";
    private final String tpnc1 = "82345";
    private final String tpnc2 = "94553";
    private final String tpnc3 = "94554";
    private ProductPriceBuilder productPriceVisitor;
    private String currency;

    @Before
    public void setUp() {
        currency = "GBP";
        productPriceVisitor = new ProductPriceBuilder(Optional.of(priceZoneId), Optional.of(promoZoneId), currency);
    }

    @Test
    public void shouldBuildProductPrice(){
        Product productWithVariants = createProductWithVariants();

        productWithVariants.accept(productPriceVisitor);

        assertThat(productPriceVisitor.getPriceInfo()).isEqualTo(expectedProductPriceMap(true, true));
    }

    @Test
    public void shouldNotAddPVariantInfoWhenVariantDoesNotHaveSaleInfo(){
        Product productWithVariants = createProductWithVariants();

        ProductVariant productVariantThatDoesNotHaveMatchingZone = new ProductVariant(tpnc3);
        productVariantThatDoesNotHaveMatchingZone.addSaleInfo(new SaleInfo(10, "1.39"));
        productWithVariants.addProductVariant(productVariantThatDoesNotHaveMatchingZone);

        productWithVariants.accept(productPriceVisitor);

        assertThat(productPriceVisitor.getPriceInfo()).isEqualTo(expectedProductPriceMap(true, true));
    }

    @Test
    public void shouldIgnoreAbsentPriceZoneId(){
        Product product = createProductWithVariants();

        ProductPriceBuilder productPriceBuilder = new ProductPriceBuilder(Optional.<Integer>absent(), Optional.of(promoZoneId), currency);
        product.accept(productPriceBuilder);

        assertThat(productPriceBuilder.getPriceInfo()).isEqualTo(expectedProductPriceMap(false, true));
    }

    @Test
    public void shouldIgnoreAbsentPromoZoneId(){
        Product product = createProductWithVariants();

        ProductPriceBuilder productPriceBuilder = new ProductPriceBuilder(Optional.of(priceZoneId), Optional.<Integer>absent(), currency);
        product.accept(productPriceBuilder);

        assertThat(productPriceBuilder.getPriceInfo()).isEqualTo(expectedProductPriceMap(true, false));
    }

    private Map<String, Object> expectedProductPriceMap(boolean includePrice, boolean includePromoPrice) {
        Map<String, String> variantInfo1 = new LinkedHashMap<>();
        variantInfo1.put("tpnc", tpnc1);
        variantInfo1.put("currency", "GBP");
        if (includePrice) variantInfo1.put("price", "1.40");
        if (includePromoPrice) variantInfo1.put("promoPrice", "1.30");

        Map<String, String> variantInfo2 = new LinkedHashMap<>();
        variantInfo2.put("tpnc", tpnc2);
        variantInfo2.put("currency", "GBP");
        if (includePrice) variantInfo2.put("price", "1.39");
        if (includePromoPrice) variantInfo2.put("promoPrice", "1.20");

        ArrayList<Map<String, String>> variants = new ArrayList<>();
        variants.add(variantInfo1);
        variants.add(variantInfo2);

        Map<String, Object> productPriceMap = new LinkedHashMap<>();
        productPriceMap.put("tpnb", tpnb);
        productPriceMap.put("variants", variants);

        return productPriceMap;
    }

    private Product createProductWithVariants() {

        ProductVariant productVariant1 = new ProductVariant(tpnc1);
        productVariant1.addSaleInfo(new SaleInfo(1, "1.40"));
        productVariant1.addSaleInfo(new SaleInfo(5, "1.30"));

        ProductVariant productVariant2 = new ProductVariant(tpnc2);
        productVariant2.addSaleInfo(new SaleInfo(1, "1.39"));
        productVariant2.addSaleInfo(new SaleInfo(2, "1.38"));
        productVariant2.addSaleInfo(new SaleInfo(5, "1.20"));
        productVariant2.addSaleInfo(new SaleInfo(14, "1.10"));


        Product product = new Product(tpnb);
        product.addProductVariant(productVariant1);
        product.addProductVariant(productVariant2);

        return product;
    }
}
