package com.tesco.services.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

public class ProductPriceBuilderTest {
    @Test
    public void shouldBuildProductPrice(){
        String tpnb = "01234";
        String tpnc1 = "82345";
        String tpnc2 = "94553";
        Product productWithVariants = createProductWithVariants(tpnb, tpnc1, tpnc2);
        ProductPriceBuilder productPriceVisitor = new ProductPriceBuilder(1);

        productWithVariants.accept(productPriceVisitor);

        assertThat(productPriceVisitor.getPriceInfo()).isEqualTo(expectedProductPriceMap(tpnb, tpnc1, tpnc2));
    }

    private Map<String, Object> expectedProductPriceMap(String tpnb, String tpnc1, String tpnc2) {
        Map<String, String> variantInfo1 = new LinkedHashMap<>();
        variantInfo1.put("tpnc", tpnc1);
        variantInfo1.put("price", "1.40");

        Map<String, String> variantInfo2 = new LinkedHashMap<>();
        variantInfo2.put("tpnc", tpnc2);
        variantInfo2.put("price", "1.39");

        ArrayList<Map<String, String>> variants = new ArrayList<>();
        variants.add(variantInfo1);
        variants.add(variantInfo2);

        Map<String, Object> productPriceMap = new LinkedHashMap<>();
        productPriceMap.put("tpnb", tpnb);
        productPriceMap.put("variants", variants);

        return productPriceMap;
    }

    private Product createProductWithVariants(String tpnb, String tpnc1, String tpnc2) {

        ProductVariant productVariant1 = new ProductVariant(tpnc1);
        productVariant1.addSaleInfo(new SaleInfo(1, "1.40"));

        ProductVariant productVariant2 = new ProductVariant(tpnc2);
        productVariant2.addSaleInfo(new SaleInfo(1, "1.39"));
        productVariant2.addSaleInfo(new SaleInfo(6, "1.38"));

        Product product = new Product(tpnb);
        product.addProductVariant(productVariant1);
        product.addProductVariant(productVariant2);

        return product;
    }
}
