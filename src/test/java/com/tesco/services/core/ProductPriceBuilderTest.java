package com.tesco.services.core;

import com.google.common.base.Optional;
import com.tesco.services.builder.PromotionBuilder;
import com.tesco.services.resources.model.ProductPriceBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
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
    public void shouldNotAddProductVariantInfoWhenVariantDoesNotHaveSaleInfo(){
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

    /**
     * @modified by Sushil for PS-118
     * Test case modify to accommodate new change as per IDL to construct tpnb, variants and promotions JSON data.
     */
    private Map<String, Object> expectedProductPriceMap(boolean includePrice, boolean includePromoPrice) {
        Map<String, Object> variantInfo1 = new LinkedHashMap<>();
        List<Map<String, String>> promotions = new ArrayList<>();
        variantInfo1.put("tpnc", tpnc1);
        variantInfo1.put("currency", "GBP");
        if (includePrice) variantInfo1.put("price", "1.40");
        if (includePromoPrice) {
            variantInfo1.put("promoprice", "1.30");
            promotions = Arrays.asList(createPromotionInfo("A30718669"), createPromotionInfo("A30718670"));
            //variantInfo1.put("promotions", promotions);
        }else if(includePromoPrice == false){
            variantInfo1.put("promoprice", null);
            promotions =  Arrays.asList();
        }

        Map<String, Object> variantInfo2 = new LinkedHashMap<>();
        variantInfo2.put("tpnc", tpnc2);
        variantInfo2.put("currency", "GBP");
        if (includePrice) variantInfo2.put("price", "1.39");
        if (includePromoPrice) variantInfo2.put("promoprice", "1.20");

        ArrayList<Map<String, Object>> variants = new ArrayList<>();
        variants.add(variantInfo1);
        variants.add(variantInfo2);

        Map<String, Object> productPriceMap = new LinkedHashMap<>();
        productPriceMap.put("tpnb", tpnb);
        productPriceMap.put("variants", variants);
        productPriceMap.put("promotions", promotions);

        return productPriceMap;
    }

    private Product createProductWithVariants() {
        SaleInfo saleInfoWithPromotion = new SaleInfo(5, "1.30");
        saleInfoWithPromotion.addPromotion(createPromotion("A30718670"));
        saleInfoWithPromotion.addPromotion(createPromotion("A30718669"));

        ProductVariant productVariant1 = new ProductVariant(tpnc1);
        productVariant1.addSaleInfo(new SaleInfo(1, "1.40"));
        productVariant1.addSaleInfo(saleInfoWithPromotion);

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

    private Promotion createPromotion(String offerId) {
        return new PromotionBuilder().
                    offerId(offerId).
                    offerName("Test Offer Name " + offerId).
                    startDate("20130729").
                    endDate("20130819").
                    description1("Test Description 1 " + offerId).
                    description2("Test Description 2 " + offerId).
                createPromotion();
    }

    private Map<String, String> createPromotionInfo(String offerId) {
        Promotion promotion = createPromotion(offerId);
        Map<String, String> promotionInfo = new LinkedHashMap<>();
        promotionInfo.put("offerName", promotion.getOfferName());
        promotionInfo.put("effectiveDate", promotion.getEffectiveDate());
        promotionInfo.put("endDate", promotion.getEndDate());
        promotionInfo.put("customerFriendlyDescription1", promotion.getCFDescription1());
        promotionInfo.put("customerFriendlyDescription2", promotion.getCFDescription2());
        return promotionInfo;
    }
}
