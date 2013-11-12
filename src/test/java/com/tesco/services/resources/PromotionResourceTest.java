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
import com.tesco.core.UUIDGenerator;
import com.tesco.services.Promotion;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.resources.fixtures.TestStoreDBObject;
import com.tesco.services.resources.model.PromotionRequest;
import com.tesco.services.resources.model.PromotionRequestList;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.tesco.core.PriceKeys.PROMOTION_COLLECTION;
import static com.tesco.core.PriceKeys.STORE_COLLECTION;
import static com.tesco.services.builder.PromotionBuilder.aPromotion;
import static com.tesco.services.builder.PromotionRequestBuilder.aPromotionRequest;
import static com.yammer.dropwizard.testing.JsonHelpers.asJson;
import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.util.Lists.newArrayList;

public class PromotionResourceTest extends ResourceTest {

    private static final String PROMOTION_FIND_ENDPOINT = "/promotion/find";
    private static Configuration testConfiguration = new TestConfiguration();
    private static DataGridResource dataGridResource;

    @Override
    protected void setUpResources() throws Exception {
        PromotionResource offerResource = new PromotionResource(new PromotionRepository(new UUIDGenerator(), dataGridResource.getPromotionCache()));
        addResource(offerResource);
    }

    @BeforeClass
    public static void setUp() throws IOException {
        DBFactory dbFactory = new DBFactory(testConfiguration);
        dbFactory.getCollection(PROMOTION_COLLECTION).drop();
        dbFactory.getCollection(STORE_COLLECTION).drop();

        DBCollection storeCollection = dbFactory.getCollection(STORE_COLLECTION);
        storeCollection.insert(new TestStoreDBObject("2000").withZoneId("5").build());

        Promotion promotion = aPromotion()
                .offerId("123")
                .itemNumber("1234")
                .zoneId("5")
                .startDate("date1")
                .endDate("date2")
                .offerName("name of promotion")
                .description1("blah")
                .description2("blah")
                .shelfTalker("OnSale.png")
                .build();

        Promotion promotion1 = aPromotion()
                .offerId("123")
                .itemNumber("5678")
                .zoneId("4")
                .startDate("date1")
                .endDate("date2")
                .offerName("name of promotion")
                .description1("blah")
                .description2("blah")
                .build();

        Promotion promotion2 = aPromotion()
                .offerId("567")
                .itemNumber("5678")
                .zoneId("4")
                .startDate("date1")
                .endDate("date2")
                .offerName("name of promotion")
                .description1("blah")
                .description2("blah")
                .build();

        Promotion promotion3 = aPromotion().offerId("345").build();

        dataGridResource = new DataGridResource();
        dataGridResource.getPromotionCache().put(UUID.randomUUID().toString(), promotion);
        dataGridResource.getPromotionCache().put(UUID.randomUUID().toString(), promotion1);
        dataGridResource.getPromotionCache().put(UUID.randomUUID().toString(), promotion2);
        dataGridResource.getPromotionCache().put(UUID.randomUUID().toString(), promotion3);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        dataGridResource.stop();
    }

    @Test
    public void shouldReturnMultiplePromotions() throws Exception {
        PromotionRequest promotionRequest = aPromotionRequest()
                .offerId("123")
                .itemNumber("1234")
                .zoneId("5")
                .build();

        PromotionRequest promotionRequest1 = aPromotionRequest()
                .offerId("567")
                .itemNumber("5678")
                .zoneId("4")
                .build();


        PromotionRequestList promotionRequestList = new PromotionRequestList();
        promotionRequestList.setPromotions(newArrayList(promotionRequest, promotionRequest1));

        WebResource resource = client().resource(PROMOTION_FIND_ENDPOINT);
        ClientResponse response = resource.type(APPLICATION_JSON).post(ClientResponse.class, asJson(promotionRequestList));
        assertThat(response.getStatus()).isEqualTo(200);

        String jsonResponse = resource.type(APPLICATION_JSON).post(String.class, asJson(promotionRequestList));
        assertThat(jsonResponse).doesNotContain("uniqueKey");

        List<Promotion> promotions = fromJson(jsonResponse, new TypeReference<List<Promotion>>() {
        });
        assertThat(promotions).hasSize(2);

        Promotion firstPromotion = promotions.get(0);
        assertThat(firstPromotion.getOfferId()).isEqualTo("567");
        assertThat(firstPromotion.getItemNumber()).isEqualTo("5678");
        assertThat(firstPromotion.getZoneId()).isEqualTo("4");
        assertThat(firstPromotion.getOfferName()).isEqualTo("name of promotion");
        assertThat(firstPromotion.getStartDate()).isEqualTo("date1");
        assertThat(firstPromotion.getEndDate()).isEqualTo("date2");
        assertThat(firstPromotion.getCFDescription1()).isEqualTo("blah");
        assertThat(firstPromotion.getCFDescription2()).isEqualTo("blah");
        assertThat(firstPromotion.getOfferText()).isEqualTo("default");

        Promotion secondPromotion = promotions.get(1);
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
        PromotionRequest promotionRequest = aPromotionRequest()
                .zoneId("5")
                .itemNumber("something wrong")
                .offerId("something wrong")
                .build();

        PromotionRequestList promotionRequestList = new PromotionRequestList();
        promotionRequestList.setPromotions(newArrayList(promotionRequest));

        WebResource resource = client().resource(PROMOTION_FIND_ENDPOINT);
        ClientResponse response = resource.type(APPLICATION_JSON).post(ClientResponse.class, asJson(promotionRequestList));
        assertThat(response.getStatus()).isEqualTo(200);

        List<Promotion> promotions = fromJson(resource.type(APPLICATION_JSON).post(String.class, asJson(promotionRequestList)), new TypeReference<List<Promotion>>() {
        });

        assertThat(promotions).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListGivenMissingAttribute() throws Exception {
        PromotionRequest promotionRequest = aPromotionRequest()
                .offerId("123")
                .build();

        PromotionRequestList promotionRequestList = new PromotionRequestList();
        promotionRequestList.setPromotions(newArrayList(promotionRequest));

        WebResource resource = client().resource(PROMOTION_FIND_ENDPOINT);
        ClientResponse response = resource.type(APPLICATION_JSON).post(ClientResponse.class, asJson(promotionRequestList));
        assertThat(response.getStatus()).isEqualTo(200);

        List<DBObject> promotions = (List<DBObject>) JSON.parse(resource.type(APPLICATION_JSON).post(String.class, asJson(promotionRequestList)));

        assertThat(promotions).isEmpty();
    }

    @Test
    public void shouldReturnValueForCorrectRequestItemOnly() throws Exception {
        PromotionRequest promotionRequest = aPromotionRequest()
                .zoneId("5")
                .itemNumber("1234")
                .offerId("123")
                .build();

        PromotionRequest promotionRequest1 = aPromotionRequest()
                .offerId("567")
                .build();


        PromotionRequestList promotionRequestList = new PromotionRequestList();
        promotionRequestList.setPromotions(newArrayList(promotionRequest, promotionRequest1));

        WebResource resource = client().resource(PROMOTION_FIND_ENDPOINT);
        ClientResponse response = resource.type(APPLICATION_JSON).post(ClientResponse.class, asJson(promotionRequestList));
        assertThat(response.getStatus()).isEqualTo(200);

        List<Promotion> promotions = fromJson(resource.type(APPLICATION_JSON).post(String.class, asJson(promotionRequestList)), new TypeReference<List<Promotion>>() {
        });

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

    @Test
    public void shouldReturnOnlyOneResultGivenDuplicateRequests() throws Exception {
        PromotionRequest promotionRequest = aPromotionRequest()
                .zoneId("5")
                .itemNumber("1234")
                .offerId("123")
                .build();

        PromotionRequest promotionRequest1 = aPromotionRequest()
                .zoneId("5")
                .itemNumber("1234")
                .offerId("123")
                .build();

        PromotionRequestList promotionRequestList = new PromotionRequestList();
        promotionRequestList.setPromotions(newArrayList(promotionRequest, promotionRequest1));

        WebResource resource = client().resource(PROMOTION_FIND_ENDPOINT);
        ClientResponse response = resource.type(APPLICATION_JSON).post(ClientResponse.class, asJson(promotionRequestList));
        assertThat(response.getStatus()).isEqualTo(200);

        List<Promotion> promotions = fromJson(resource.type(APPLICATION_JSON).post(String.class, asJson(promotionRequestList)), new TypeReference<List<Promotion>>() {
        });

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
