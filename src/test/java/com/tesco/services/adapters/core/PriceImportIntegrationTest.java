package com.tesco.services.adapters.core;

import com.tesco.services.builder.PromotionBuilder;
import com.tesco.services.core.Product;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.SaleInfo;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.repositories.ProductRepository;
import com.tesco.services.resources.TestConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.fest.assertions.api.Assertions.assertThat;

public class PriceImportIntegrationTest extends ImportJobIntegrationTestBase {
    private String oldTpnb;

    @Override
    protected void preImportCallBack() {
        oldTpnb = "01212323";
    }

    @Test
    public void shouldUpdatePriceZonePrices() throws URISyntaxException, IOException, InterruptedException {
        String tpnb,tpnc1,tpnc2;
        tpnb = tpnc1 = "050925811";
        tpnc2 = "050925811-001";

        Promotion promotion1 = new PromotionBuilder().
                offerId("A30718670").
                offerName("Test Offer Name1").
                startDate("20130729").
                endDate("20130819").
                description1("Test Description 1").
                description2("Test Description 2").
                createPromotion();

        SaleInfo saleInfo1 = new SaleInfo(5, "0.30");
        saleInfo1.addPromotion(promotion1);
        
        ProductVariant productVariant1 = new ProductVariant(tpnc1);
        productVariant1.addSaleInfo(saleInfo1);
        productVariant1.addSaleInfo(new SaleInfo(1, "1.40"));

        Promotion promotion2 = new PromotionBuilder().
                offerId("A30718671").
                offerName("Test Offer Name2").
                startDate("20130829").
                endDate("20130919").
                description1("Test Description 3").
                description2("Test Description 4").
                createPromotion();

        SaleInfo saleInfo2 = new SaleInfo(14, "0.35");
        saleInfo2.addPromotion(promotion2);
        
        ProductVariant productVariant2 = new ProductVariant(tpnc2);
        productVariant2.addSaleInfo(saleInfo2);
        productVariant2.addSaleInfo(new SaleInfo(1, "1.39"));
        productVariant2.addSaleInfo(new SaleInfo(2, "1.38"));
        productVariant2.addSaleInfo(new SaleInfo(5, "0.34"));

        Product product = new Product(tpnb);
        product.addProductVariant(productVariant1);
        product.addProductVariant(productVariant2);

        ProductRepository productRepository = new ProductRepository(new CouchbaseConnectionManager(new TestConfiguration()).getCouchbaseClient());
        assertThat(productRepository.getByTPNB(tpnb).get()).isEqualTo(product);
        assertThat(productRepository.getByTPNB(oldTpnb).isPresent()).isFalse();
    }
}
