package com.tesco.services.adapters.core;

import com.mongodb.DBObject;
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

import static com.tesco.services.adapters.core.TestFiles.RPM_PRICE_ZONE_PRICE_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PRICE_ZONE_TO_UPDATE_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMOTION_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMOTION_DESC_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMO_DESC_EXTRACT_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMO_EXTRACT_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_PROMO_ZONE_PRICE_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.RPM_STORE_ZONE_CSV_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.SONETTO_PROMOTIONS_XML_FILE_PATH;
import static com.tesco.services.adapters.core.TestFiles.SONETTO_PROMOTIONS_XSD_FILE_PATH;
import static com.tesco.services.core.PriceKeys.PRICE;
import static com.tesco.services.core.PriceKeys.PROMO_PRICE;
import static org.fest.assertions.api.Assertions.assertThat;

public class PriceImportIntegrationTest extends ImportJobIntegrationTestBase {
    private String oldTpnb;

    @Override
    protected void preImportCallBack() {
        oldTpnb = "01212323";
//        importCouchbaseConnectionManager.getProductPriceCache().put(oldTpnb, new Product(oldTpnb));
    }

    @Test
    public void shouldFindPriceFromOneZone() throws IOException {
        DBObject prices = findPricesFromZone("050925811", "5");

        assertThat(prices.get(PRICE)).isEqualTo("1.33");
        assertThat(prices.get(PROMO_PRICE)).isEqualTo("2.33");
    }

    @Test
    public void shouldFindPricesGivenMultipleZones() throws IOException {

        DBObject pricesFromZoneFive = findPricesFromZone("050940579", "5");
        assertThat(pricesFromZoneFive.get(PRICE)).isEqualTo("5.33");
        assertThat(pricesFromZoneFive.get(PROMO_PRICE)).isEqualTo("5.33");

        DBObject pricesFromZoneThree = findPricesFromZone("050940579", "3");
        assertThat(pricesFromZoneThree.get(PRICE)).isEqualTo("2.33");
        assertThat(pricesFromZoneThree.get(PROMO_PRICE)).isEqualTo("2.33");
    }

    @Test
    public void shouldUpdatePrices() throws Exception {
        TestConfiguration testConfiguration = new TestConfiguration();
        ImportJob importJob = new ImportJob(
                RPM_PRICE_ZONE_TO_UPDATE_CSV_FILE_PATH,
                RPM_STORE_ZONE_CSV_FILE_PATH,
                RPM_PROMOTION_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XML_FILE_PATH,
                RPM_PROMOTION_DESC_CSV_FILE_PATH,
                SONETTO_PROMOTIONS_XSD_FILE_PATH,
                testConfiguration.getSonettoShelfImageUrl(),
                RPM_PRICE_ZONE_PRICE_CSV_FILE_PATH,
                RPM_PROMO_ZONE_PRICE_CSV_FILE_PATH,
                RPM_PROMO_EXTRACT_CSV_FILE_PATH,
                RPM_PROMO_DESC_EXTRACT_CSV_FILE_PATH,
                dbFactory,
                importCouchbaseConnectionManager);
        importJob.run();

        DBObject prices = findPricesFromZone("050925811", "5");

        assertThat(prices.get(PRICE)).isEqualTo("20.33");
        assertThat(prices.get(PROMO_PRICE)).isEqualTo("12.33");
    }

    // =========
    // DataGrid
    // =========
    @Test
    public void shouldUpdatePriceZonePricesToReplacedCache() throws URISyntaxException, IOException, InterruptedException {
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
                buildForDataGrid();

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
                buildForDataGrid();

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

        ProductRepository productRepository = new ProductRepository(new CouchbaseConnectionManager(null).getCouchbaseClient());
        assertThat(productRepository.getByTPNB(tpnb).get()).isEqualTo(product);
        assertThat(productRepository.getByTPNB(oldTpnb).isPresent()).isFalse();
    }
}
