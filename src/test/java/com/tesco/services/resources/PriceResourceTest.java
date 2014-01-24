package com.tesco.services.resources;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.services.Configuration;
import com.tesco.services.dao.PriceDAO;
import com.tesco.services.dao.DBFactory;
import com.tesco.services.exceptions.ItemNotFoundException;
import com.tesco.services.resources.fixtures.TestProductPriceDBObject;
import com.tesco.services.resources.fixtures.TestPromotionDBObject;
import com.tesco.services.resources.fixtures.TestStoreDBObject;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class PriceResourceTest extends ResourceTest {

    private static Configuration testConfiguration = new TestConfiguration();
    private PriceDAO priceDAO;
    private static DBCollection priceCollection;
    private static DBCollection storeCollection;

    @Override
    protected void setUpResources() throws Exception {
        priceDAO = new PriceDAO(testConfiguration);
        PriceResource priceResource = new PriceResource(priceDAO);
        addResource(priceResource);
    }

    @BeforeClass
    public static void setUp() throws IOException {
        DBFactory dbFactory = new DBFactory(testConfiguration);
        priceCollection = dbFactory.getCollection("prices");
        storeCollection = dbFactory.getCollection("stores");
    }

    @Before
    public void beforeEachTest(){
        priceCollection.drop();
        storeCollection.drop();
    }

    @Test
    public void returnsPricesAndPromotionsForMultipleItems() throws IOException, ItemNotFoundException {
        DBObject dbPromotion1 = new TestPromotionDBObject("offer1").build();
        DBObject dbPromotion2 = new TestPromotionDBObject("offer2").build();
        String item1 = "randomItem1";
        String item2 = "randomItem2";
        priceCollection.insert(new TestProductPriceDBObject(item1).withPrice("2.00").withPromotionPrice("1.33").addPromotion(dbPromotion1).inZone("5").build());
        priceCollection.insert(new TestProductPriceDBObject(item2).withPrice("4.00").withPromotionPrice("2.00").addPromotion(dbPromotion2).inZone("5").build());

        WebResource resource = client().resource("/price/randomItem1,randomItem2");
        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
        List<DBObject> priceInfos = (List<DBObject>) JSON.parse(resource.get(String.class));


        DBObject actualPrice1 = priceInfos.get(0);
        DBObject actualPrice2 = priceInfos.get(1);

        assertThat(actualPrice1.get("itemNumber")).isEqualTo(item1);
        assertThat(actualPrice1.get("price")).isEqualTo("2.00");
        assertThat(actualPrice1.get("promoPrice")).isEqualTo("1.33");
        assertThat(actualPrice1.get("currency")).isEqualTo("GBP");
        assertThat(((List<DBObject>) actualPrice1.get("promotions")).get(0).get("offerId")).isEqualTo("offer1");

        assertThat(actualPrice2.get("itemNumber")).isEqualTo(item2);
        assertThat(actualPrice2.get("price")).isEqualTo("4.00");
        assertThat(actualPrice2.get("promoPrice")).isEqualTo("2.00");
        assertThat(actualPrice2.get("currency")).isEqualTo("GBP");
        assertThat(((List<DBObject>) actualPrice2.get("promotions")).get(0).get("offerId")).isEqualTo("offer2");
    }

    @Test
    public void returnsPricesAndPromotionsForMultipleItemsInAStore() throws IOException, ItemNotFoundException {
        DBObject dbPromotion1 = new TestPromotionDBObject("offer1").build();
        DBObject dbPromotion2 = new TestPromotionDBObject("offer2").build();
        String item1 = "randomItem1";
        String item2 = "randomItem2";
        String item3 = "randomItem3";
        priceCollection.insert(new TestProductPriceDBObject(item1).withPrice("2.00").withPromotionPrice("1.33").addPromotion(dbPromotion1).inZone("2").build());
        priceCollection.insert(new TestProductPriceDBObject(item2).withPrice("4.00").withPromotionPrice("2.00").addPromotion(dbPromotion2).inZone("5").build());
        priceCollection.insert(new TestProductPriceDBObject(item3).withPrice("4.00").withPromotionPrice("2.00").addPromotion(dbPromotion2).inZone("2").build());
        storeCollection.insert(new TestStoreDBObject("randomStore").withZoneId("2").build());

        WebResource resource = client().resource("/price/randomItem1,randomItem2,randomItem3?store=randomStore");
        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
        List<DBObject> priceInfos = (List<DBObject>) JSON.parse(resource.get(String.class));

        assertThat(priceInfos.size()).isEqualTo(2);

        DBObject actualPrice1 = priceInfos.get(0);
        DBObject actualPrice2 = priceInfos.get(1);

        assertThat(actualPrice1.get("itemNumber")).isEqualTo(item1);
        assertThat(actualPrice1.get("price")).isEqualTo("2.00");
        assertThat(actualPrice1.get("promoPrice")).isEqualTo("1.33");
        assertThat(actualPrice1.get("currency")).isEqualTo("GBP");
        assertThat(((List<DBObject>) actualPrice1.get("promotions")).get(0).get("offerId")).isEqualTo("offer1");

        assertThat(actualPrice2.get("itemNumber")).isEqualTo(item3);
        assertThat(actualPrice2.get("price")).isEqualTo("4.00");
        assertThat(actualPrice2.get("promoPrice")).isEqualTo("2.00");
        assertThat(actualPrice2.get("currency")).isEqualTo("GBP");
        assertThat(((List<DBObject>) actualPrice2.get("promotions")).get(0).get("offerId")).isEqualTo("offer2");
    }

    @Test
    public void ignoresPricesAndPromotionsNotForThatZoneWithMultipleItems() throws IOException, ItemNotFoundException {
        DBObject dbPromotion = new TestPromotionDBObject("offer1").withStartDate("date1").withEndDate("date2").withName("zone2 promo").withDescription1("blah").withDescription2("blah").build();
        String item1 = "randomItem1";
        String item2 = "randomItem2";
        priceCollection.insert(new TestProductPriceDBObject(item1).withPrice("2.00").withPromotionPrice("1.33").addPromotion(dbPromotion).inZone("5").build());
        priceCollection.insert(new TestProductPriceDBObject(item2).withPrice("4.00").withPromotionPrice("2.00").addPromotion(dbPromotion).inZone("1").build());

        WebResource resource = client().resource("/price/randomItem1,randomItem2");
        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
        List<DBObject> priceInfos = (List<DBObject>) JSON.parse(resource.get(String.class));

        assertThat(priceInfos.size()).isEqualTo(1);
        assertThat(priceInfos.get(0).get("itemNumber")).isEqualTo(item1);
    }

    @Test
    public void shouldReturnPricesAndPromotionsForStoreWhenSearchingForItemAtAParticularStore() throws IOException, ItemNotFoundException {
        storeCollection.insert(new TestStoreDBObject("randomStore").withZoneId("2").build());
        DBObject dbPromotion = new TestPromotionDBObject("offer1").withStartDate("date1").withEndDate("date2").withName("zone2 promo").withDescription1("blah").withDescription2("blah").build();
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("2.00").withPromotionPrice("1.33").addPromotion(dbPromotion).inZone("2").build());

        WebResource resource = client().resource("/price/randomItem?store=randomStore");
        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
        List<DBObject> priceInfo = (List<DBObject>) JSON.parse(resource.get(String.class));


        assertThat(priceInfo.get(0).get("itemNumber")).isEqualTo("randomItem");
        assertThat(priceInfo.get(0).get("price")).isEqualTo("2.00");
        assertThat(priceInfo.get(0).get("promoPrice")).isEqualTo("1.33");
        assertThat(priceInfo.get(0).get("currency")).isEqualTo("GBP");

        DBObject promotion = ((List<DBObject>) priceInfo.get(0).get("promotions")).get(0);
        assertThat(promotion.get("offerId")).isEqualTo("offer1");
        assertThat(promotion.get("offerName")).isEqualTo("zone2 promo");
        assertThat(promotion.get("startDate")).isEqualTo("date1");
        assertThat(promotion.get("endDate")).isEqualTo("date2");
        assertThat(promotion.get("CFDescription1")).isEqualTo("blah");
        assertThat(promotion.get("CFDescription2")).isEqualTo("blah");
    }

    @Test
    public void shouldReturnPricesAndPromotionsFromNationalZoneWhenSearchingForItemAtNoParticularStore() throws IOException, ItemNotFoundException {
        DBObject dbPromotion = new TestPromotionDBObject("offer1").withStartDate("date1").withEndDate("date2").withName("zone5 promo").withDescription1("blah").withDescription2("blah").build();
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").addPromotion(dbPromotion).inZone("5").build());

        WebResource resource = client().resource("/price/randomItem");
        ClientResponse response = resource.get(ClientResponse.class);
        List<DBObject> priceInfo = (List<DBObject>) JSON.parse(resource.get(String.class));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(priceInfo.get(0).get("itemNumber")).isEqualTo("randomItem");
        assertThat(priceInfo.get(0).get("price")).isEqualTo("3.00");
        assertThat(priceInfo.get(0).get("promoPrice")).isEqualTo("2.33");
        assertThat(priceInfo.get(0).get("currency")).isEqualTo("GBP");

        DBObject promotion = ((List<DBObject>) priceInfo.get(0).get("promotions")).get(0);
        assertThat(promotion.get("offerName")).isEqualTo("zone5 promo");
        assertThat(promotion.get("startDate")).isEqualTo("date1");
        assertThat(promotion.get("endDate")).isEqualTo("date2");
        assertThat(promotion.get("CFDescription1")).isEqualTo("blah");
        assertThat(promotion.get("CFDescription2")).isEqualTo("blah");
    }

    @Test
    public void shouldReturnPriceWithoutPromotionInformationIfPromotionDoesNotExist() throws IOException, ItemNotFoundException {
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/randomItem");
        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);

        List<DBObject> priceInfo = (List<DBObject>) JSON.parse(resource.get(String.class));

        assertThat(priceInfo.get(0).get("itemNumber")).isEqualTo("randomItem");
        assertThat(priceInfo.get(0).get("price")).isEqualTo("3.00");
        assertThat(priceInfo.get(0).keySet()).doesNotContain("promotions");
    }

    @Test
    public void shouldIgnoreOtherInvalidQueryParamsIfPassedStoreParam() throws IOException, ItemNotFoundException {
        storeCollection.insert(new TestStoreDBObject("randomStore").withZoneId("5").build());
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/randomItem?store=randomStore&someinvalidparam=blah");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldReturn404IfItemExistsAndStoreExistsButItemIsNotAssociatedWithThatZone() throws IOException, ItemNotFoundException {
        storeCollection.insert(new TestStoreDBObject("zone2Store").withZoneId("2").build());
        priceCollection.insert(new TestProductPriceDBObject("zone1Item").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/zone1Item?store=zone2Store");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Product not found");
    }

    @Test
    public void shouldReturn404ResponseWhenItemIsNotFound() throws ItemNotFoundException {
        WebResource resource = client().resource("/price/some_non_existent");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Price cannot be retrieved because product not found");
    }

    @Test
    public void shouldReturn404ResponseWhenStoreIsNotFound() throws IOException, ItemNotFoundException {
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/randomItem?store=some_non_existent_store");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).contains("Store not found");
    }

    @Test
    public void shouldReturn400WhenReachingInvalidResource() {
        WebResource resource = client().resource("/non-existent");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturn400WhenNoQueryTypeGiven() {
        WebResource resource = client().resource("/price");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).contains("Invalid request");
    }

    @Test
    public void shouldReturn400ResponseWhenPassedInvalidQueryParam() throws IOException, ItemNotFoundException {
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/randomItem?someInvalidQuery=blah");
        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).contains("Invalid request");

        resource = client().resource("/price/randomItem?someInvalidQuery=blah&callback=blah");
        response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).contains("Invalid request");
    }

    @Test
    public void shouldReturn400ResponseWhenTryingToAccessRootURL() {
        WebResource resource = client().resource("/");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturn400ResponseWhenAppendingInvalidPath() throws IOException, ItemNotFoundException {
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/randomItem/blah");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).contains("Invalid request");
    }

}
