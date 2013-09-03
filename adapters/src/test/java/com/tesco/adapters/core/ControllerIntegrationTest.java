package com.tesco.adapters.core;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.commons.configuration.ConfigurationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;
import static java.lang.String.format;
import static org.fest.assertions.api.Assertions.assertThat;

public class ControllerIntegrationTest {


    protected DBCollection priceCollection;
    protected DBCollection storeCollection;
    protected DBCollection promotionCollection;
    private String sonettoPromotionsXMLFilePath = "./src/test/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml";

    @BeforeMethod
    public void setUp() throws IOException, ParserConfigurationException, SAXException, ConfigurationException {
        DBFactory.getCollection(PRICE_COLLECTION).drop();
        priceCollection = DBFactory.getCollection(PRICE_COLLECTION);

        DBFactory.getCollection(STORE_COLLECTION).drop();
        storeCollection = DBFactory.getCollection(STORE_COLLECTION);

        DBFactory.getCollection(PROMOTION_COLLECTION).drop();
        promotionCollection = DBFactory.getCollection(PROMOTION_COLLECTION);

        String rpmPriceZoneCsvFilePath = "./src/test/resources/com/tesco/adapters/rpm/fixtures/price_zone.csv";
        String rpmStoreZoneCsvFilePath = "./src/test/resources/com/tesco/adapters/rpm/fixtures/store_zone.csv";
        String rpmPromotionCsvFilePath = "./src/test/resources/com/tesco/adapters/rpm/fixtures/prom_extract.csv";
        String sonettoPromotionsXMLFilePath = "./src/test/resources/com/tesco/adapters/sonetto/PromotionsDataExport.xml";
        new Controller(priceCollection, storeCollection, promotionCollection, rpmPriceZoneCsvFilePath, rpmStoreZoneCsvFilePath, rpmPromotionCsvFilePath, sonettoPromotionsXMLFilePath).fetchAndSavePriceDetails();
    }

    @Test
    public void shouldImportPriceFromRPMPriceDump() throws IOException {
        DBObject query = QueryBuilder.start(ITEM_NUMBER).is("050925811").and(
                QueryBuilder.start(format("%s.%s.%s", ZONES, "5", PRICE)).is("1.33").get()).get();
        List<DBObject> priceResults = priceCollection.find(query).toArray();
        assertThat(priceResults.size()).isEqualTo(1);
    }

    @Test
    public void shouldImportAndUpdateZonePricesFromRPMPriceDump() throws IOException {
        DBObject query = QueryBuilder.start(ITEM_NUMBER).is("050940579").and(
                QueryBuilder.start(format("%s.%s.%s", ZONES, "5", PRICE)).is("5.33").get()).get();
        List<DBObject> priceResults = priceCollection.find(query).toArray();
        assertThat(priceResults.size()).isEqualTo(1);

        query = QueryBuilder.start(ITEM_NUMBER).is("050940579").and(
                QueryBuilder.start(format("%s.%s.%s", ZONES, "3", PRICE)).is("2.33").get()).get();
        priceResults = priceCollection.find(query).toArray();
        assertThat(priceResults.size()).isEqualTo(1);
    }

    @Test
    public void shouldImportAndUpdateZonePromotionalPricesFromRPMPriceDump() throws IOException {
        DBObject query = QueryBuilder.start(ITEM_NUMBER).is("050925811").get();
        DBObject price = priceCollection.find(query).toArray().get(0);
        DBObject zones = (DBObject) price.get(format("%s", ZONES));
        DBObject prices = (DBObject) zones.get("5");
        assertThat(prices.get(PROMO_PRICE)).isEqualTo("2.33");
    }

    @Test
    public void shouldImportZonePriceAndPromoPriceFromRPMPriceDumpsOnRefresh() throws IOException, ParserConfigurationException, SAXException, ConfigurationException {
        String rpmPriceZoneCsvFilePath = "./src/test/resources/com/tesco/adapters/rpm/fixtures/price_zone_to_update.csv";
        String rpmStoreZoneUpdateFilePath = "./src/test/resources/com/tesco/adapters/rpm/fixtures/store_zone.csv";
        String rpmPromotionCsvFilePath = "./src/test/resources/com/tesco/adapters/rpm/fixtures/prom_extract.csv";
        new Controller(priceCollection, storeCollection, promotionCollection, rpmPriceZoneCsvFilePath, rpmStoreZoneUpdateFilePath, rpmPromotionCsvFilePath, sonettoPromotionsXMLFilePath).fetchAndSavePriceDetails();

        DBObject query = QueryBuilder.start(ITEM_NUMBER).is("050925811").get();
        DBObject price = priceCollection.find(query).toArray().get(0);
        DBObject zones = (DBObject) price.get(format("%s", ZONES));
        DBObject prices = (DBObject) zones.get("5");
        assertThat(prices.get(PRICE)).isEqualTo("20.33");
        assertThat(prices.get(PROMO_PRICE)).isEqualTo("12.33");
    }

    @Test
    public void shouldImportStoreAndZoneMapping() {
        List<DBObject> stores = storeCollection.find((DBObject) JSON.parse(format("{\"%s\": \"2002\"}", STORE_ID))).toArray();
        DBObject productWithPrice = stores.get(0);

        assertThat(stores.size()).isEqualTo(1);
        assertThat(productWithPrice.get(CURRENCY)).isEqualTo("GBP");
        assertThat(productWithPrice.get(PRICE_ZONE_ID)).isEqualTo("1");
        assertThat(productWithPrice.get(PROMOTION_ZONE_ID)).isEqualTo("5");
        assertThat(productWithPrice.get(STORE_ID)).isEqualTo("2002");
    }

    @Test
    public void shouldImportStoreAndZoneMappingOnRefresh() throws IOException, ParserConfigurationException, SAXException, ConfigurationException {
        String rpmPriceZoneCsvFilePath = "./src/test/resources/com/tesco/adapters/rpm/fixtures/price_zone.csv";
        String rpmStoreZoneUpdateFilePath = "./src/test/resources/com/tesco/adapters/rpm/fixtures/store_zone_to_update.csv";
        String rpmPromotionCsvFilePath = "./src/test/resources/com/tesco/adapters/rpm/fixtures/prom_extract.csv";
        new Controller(priceCollection, storeCollection, promotionCollection, rpmPriceZoneCsvFilePath, rpmStoreZoneUpdateFilePath, rpmPromotionCsvFilePath, sonettoPromotionsXMLFilePath).fetchAndSavePriceDetails();

        List<DBObject> stores = storeCollection.find((DBObject) JSON.parse(format("{\"%s\": \"2002\"}", STORE_ID))).toArray();
        DBObject productWithPrice = stores.get(0);

        assertThat(stores.size()).isEqualTo(1);
        assertThat(productWithPrice.get(PRICE_ZONE_ID)).isEqualTo("1");
        assertThat(productWithPrice.get(STORE_ID)).isEqualTo("2002");
    }

}
