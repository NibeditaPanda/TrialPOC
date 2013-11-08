package com.tesco.services.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.core.Configuration;
import com.tesco.core.DBFactory;
import com.tesco.core.DataGridResource;
import com.tesco.services.Promotion;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.resources.fixtures.TestPromotionDBObject;
import com.tesco.services.resources.fixtures.TestStoreDBObject;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.tesco.core.PriceKeys.PROMOTION_COLLECTION;
import static com.tesco.core.PriceKeys.STORE_COLLECTION;
import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static org.fest.assertions.api.Assertions.assertThat;

public class PromotionResourceTest extends ResourceTest {

    private static Configuration testConfiguration = new TestConfiguration();
    private static DBCollection promotionCollection;
    private static DataGridResource dataGridResource;

    @Override
    protected void setUpResources() throws Exception {
        PromotionResource offerResource = new PromotionResource(new PromotionRepository(dataGridResource.getPromotionCache()));
        addResource(offerResource);
    }

    @BeforeClass
    public static void setUp() throws IOException {
        DBFactory dbFactory = new DBFactory(testConfiguration);
        dbFactory.getCollection(PROMOTION_COLLECTION).drop();
        dbFactory.getCollection(STORE_COLLECTION).drop();

        DBCollection storeCollection = dbFactory.getCollection(STORE_COLLECTION);
        storeCollection.insert(new TestStoreDBObject("2000").withZoneId("5").build());

        promotionCollection = dbFactory.getCollection(PROMOTION_COLLECTION);

        TestPromotionDBObject testPromotionDBObject = new TestPromotionDBObject("123").withTPNB("1234").withPromotionZone("5").withStartDate("date1").withEndDate("date2").withName("name of promotion").withDescription1("blah").withDescription2("blah").withShelfTalker("OnSale.png");
        promotionCollection.insert(testPromotionDBObject.build());

        TestPromotionDBObject testPromotionDBObject1 = new TestPromotionDBObject("123").withTPNB("5678").withPromotionZone("4").withStartDate("date1").withEndDate("date2").withName("name of promotion").withDescription1("blah").withDescription2("blah");
        promotionCollection.insert(testPromotionDBObject1.build());

        TestPromotionDBObject testPromotionDBObject2 = new TestPromotionDBObject("567").withTPNB("5678").withPromotionZone("4").withStartDate("date1").withEndDate("date2").withName("name of promotion").withDescription1("blah").withDescription2("blah");
        promotionCollection.insert(testPromotionDBObject2.build());

        TestPromotionDBObject testPromotionDBObject3 = new TestPromotionDBObject("345");
        promotionCollection.insert(testPromotionDBObject3.build());

        dataGridResource = new DataGridResource();
        dataGridResource.getPromotionCache().put(UUID.randomUUID().toString(), testPromotionDBObject.buildJDG());
        dataGridResource.getPromotionCache().put(UUID.randomUUID().toString(), testPromotionDBObject1.buildJDG());
        dataGridResource.getPromotionCache().put(UUID.randomUUID().toString(), testPromotionDBObject2.buildJDG());
        dataGridResource.getPromotionCache().put(UUID.randomUUID().toString(), testPromotionDBObject3.buildJDG());
    }

    @Test
    public void shouldReturnMultiplePromotions() throws Exception {
        String jsonRequest = "{\n" +
                "    \"promotions\":\n" +
                "        [\n" +
                "            { \"offerId\": \"123\", \"itemNumber\": \"1234\", \"zoneId\": \"5\"},\n" +
                "            { \"offerId\": \"567\", \"itemNumber\": \"5678\", \"zoneId\": \"4\"}\n" +
                "        ]\n" +
                "}";

        WebResource resource = client().resource("/promotion/find");
        ClientResponse response = resource.type("application/json").post(ClientResponse.class, jsonRequest);
        assertThat(response.getStatus()).isEqualTo(200);

        List<Promotion> promotions = fromJson(resource.type("application/json").post(String.class, jsonRequest), new TypeReference<List<Promotion>>() {});
        assertThat(promotions).hasSize(2);

        com.tesco.services.Promotion firstPromotion = promotions.get(0);
        assertThat(firstPromotion.getOfferId()).isEqualTo("567");
        assertThat(firstPromotion.getItemNumber()).isEqualTo("5678");
        assertThat(firstPromotion.getZoneId()).isEqualTo("4");
        assertThat(firstPromotion.getOfferName()).isEqualTo("name of promotion");
        assertThat(firstPromotion.getStartDate()).isEqualTo("date1");
        assertThat(firstPromotion.getEndDate()).isEqualTo("date2");
        assertThat(firstPromotion.getCFDescription1()).isEqualTo("blah");
        assertThat(firstPromotion.getCFDescription2()).isEqualTo("blah");
        assertThat(firstPromotion.getOfferText()).isEqualTo("default");

        com.tesco.services.Promotion secondPromotion = promotions.get(1);
        assertThat(secondPromotion.getOfferId()).isEqualTo("123");
        assertThat(secondPromotion.getItemNumber()).isEqualTo("1234");
        assertThat(secondPromotion.getZoneId()).isEqualTo("5");
        assertThat(secondPromotion.getOfferName()).isEqualTo("name of promotion");
        assertThat(secondPromotion.getStartDate()).isEqualTo("date1");
        assertThat(secondPromotion.getEndDate()).isEqualTo("date2");
        assertThat(secondPromotion.getCFDescription1()).isEqualTo("blah");
        assertThat(secondPromotion.getCFDescription2()).isEqualTo("blah");
        assertThat(secondPromotion.getShelfTalkerImage()).isEqualTo("OnSale.png");
        assertThat(secondPromotion.getOfferText()).isEqualTo("default");

    }

    @Test
    public void shouldReturnEmptyList() throws Exception {
        String jsonRequest = "{\n" +
                "    \"promotions\":\n" +
                "        [\n" +
                "            { \"offerId\": \"something wrong\", \"itemNumber\": \"something wrong\", \"zoneId\": \"5\"}" +
                "        ]\n" +
                "}";

        WebResource resource = client().resource("/promotion/find");
        ClientResponse response = resource.type("application/json").post(ClientResponse.class, jsonRequest);
        assertThat(response.getStatus()).isEqualTo(200);

        List<Promotion> promotions = fromJson(resource.type("application/json").post(String.class, jsonRequest), new TypeReference<List<Promotion>>() {});

        assertThat(promotions).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListGivenMissingAttribute() throws Exception {
        String jsonRequest = "{\n" +
                "    \"promotions\":\n" +
                "        [\n" +
                "            { \"offerId\": \"123\" }" +
                "        ]\n" +
                "}";

        WebResource resource = client().resource("/promotion/find");
        ClientResponse response = resource.type("application/json").post(ClientResponse.class, jsonRequest);
        assertThat(response.getStatus()).isEqualTo(200);

        List<DBObject> promotions = (List<DBObject>) JSON.parse(resource.type("application/json").post(String.class, jsonRequest));

        assertThat(promotions).isEmpty();
    }

    @Test
    public void shouldReturnValueForCorrectRequestItemOnly() throws Exception {
        String jsonRequest = "{\n" +
                "    \"promotions\":\n" +
                "        [\n" +
                "            { \"offerId\": \"123\", \"itemNumber\": \"1234\", \"zoneId\": \"5\"},\n" +
                "            { \"offerId\": \"567\"}\n" +
                "        ]\n" +
                "}";

        WebResource resource = client().resource("/promotion/find");
        ClientResponse response = resource.type("application/json").post(ClientResponse.class, jsonRequest);
        assertThat(response.getStatus()).isEqualTo(200);

        List<Promotion> promotions = fromJson(resource.type("application/json").post(String.class, jsonRequest), new TypeReference<List<Promotion>>() {});

        assertThat(promotions.size()).isEqualTo(1);

        Promotion firstPromotion = promotions.get(0);
        assertThat(firstPromotion.getOfferId()).isEqualTo("123");
        assertThat(firstPromotion.getItemNumber()).isEqualTo("1234");
        assertThat(firstPromotion.getZoneId()).isEqualTo("5");
        assertThat(firstPromotion.getOfferName()).isEqualTo("name of promotion");
        assertThat(firstPromotion.getStartDate()).isEqualTo("date1");
        assertThat(firstPromotion.getEndDate()).isEqualTo("date2");
        assertThat(firstPromotion.getCFDescription1()).isEqualTo("blah");
        assertThat(firstPromotion.getCFDescription2()).isEqualTo("blah");
        assertThat(firstPromotion.getShelfTalkerImage()).isEqualTo("OnSale.png");
        assertThat(firstPromotion.getOfferText()).isEqualTo("default");

    }
}
