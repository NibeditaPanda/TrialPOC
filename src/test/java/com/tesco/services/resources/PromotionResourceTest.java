package com.tesco.services.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.services.Configuration;
import com.tesco.services.dao.DBFactory;
import com.tesco.services.repositories.DataGridResource;
import com.tesco.services.repositories.UUIDGenerator;
import com.tesco.services.core.Promotion;
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

import static com.tesco.services.core.PriceKeys.PROMOTION_COLLECTION;
import static com.tesco.services.core.PriceKeys.STORE_COLLECTION;
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
                .offerId("A29721688")
                .itemNumber("070918248")
                .zoneId("5")
                .startDate("31-Apr-12")
                .endDate("04-May-13")
                .offerName("3 LIONS KICK & TRICK BALL 1.00 SPECIAL PURCHASE")
                .description1("SPECIAL PURCHASE 50p")
                .description2("3 LIONS|WATERBOTTLE")
                .shelfTalker("OnSale.png")
                .uniqueKey("uuid1")
                .build();

        Promotion promotion1 = aPromotion()
                .offerId("R29029470")
                .itemNumber("66367922")
                .zoneId("12")
                .startDate("25-Sep-10")
                .endDate("22/09/35")
                .offerName("ROI HARDWARE")
                .description1("SPECIAL PURCHASE 50p")
                .description2("3 LIONS|WATERBOTTLE")
                .uniqueKey("uuid2")
                .build();

        Promotion promotion2 = aPromotion()
                .offerId("A29721690")
                .itemNumber("70918248")
                .zoneId("4")
                .startDate("31-Jun-12")
                .endDate("04-Jul-13")
                .offerName("3 LIONS KICK & TRICK BALL 3.00 SPECIAL PURCHASE")
                .description1("SPECIAL PURCHASE 50p")
                .description2("3 LIONS|CAR FLAG")
                .uniqueKey("uuid3")
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
                .offerId("A29721688")
                .itemNumber("070918248")
                .zoneId("5")
                .build();

        PromotionRequest promotionRequest1 = aPromotionRequest()
                .offerId("A29721690")
                .itemNumber("70918248")
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
        assertThat(firstPromotion.getOfferId()).isEqualTo("A29721690");
        assertThat(firstPromotion.getItemNumber()).isEqualTo("70918248");
        assertThat(firstPromotion.getZoneId()).isEqualTo("4");
        assertThat(firstPromotion.getOfferName()).isEqualTo("3 LIONS KICK & TRICK BALL 3.00 SPECIAL PURCHASE");
        assertThat(firstPromotion.getStartDate()).isEqualTo("31-Jun-12");
        assertThat(firstPromotion.getEndDate()).isEqualTo("04-Jul-13");
        assertThat(firstPromotion.getCFDescription1()).isEqualTo("SPECIAL PURCHASE 50p");
        assertThat(firstPromotion.getCFDescription2()).isEqualTo("3 LIONS|CAR FLAG");
        assertThat(firstPromotion.getOfferText()).isEqualTo("default");

        Promotion secondPromotion = promotions.get(1);
        assertThat(secondPromotion.getOfferId()).isEqualTo("A29721688");
        assertThat(secondPromotion.getItemNumber()).isEqualTo("070918248");
        assertThat(secondPromotion.getZoneId()).isEqualTo("5");
        assertThat(secondPromotion.getOfferName()).isEqualTo("3 LIONS KICK & TRICK BALL 1.00 SPECIAL PURCHASE");
        assertThat(secondPromotion.getStartDate()).isEqualTo("31-Apr-12");
        assertThat(secondPromotion.getEndDate()).isEqualTo("04-May-13");
        assertThat(secondPromotion.getCFDescription1()).isEqualTo("SPECIAL PURCHASE 50p");
        assertThat(secondPromotion.getCFDescription2()).isEqualTo("3 LIONS|WATERBOTTLE");
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
                .offerId("A29721688")
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
                .itemNumber("070918248")
                .offerId("A29721688")
                .build();

        PromotionRequest promotionRequest1 = aPromotionRequest()
                .offerId("A29721690")
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
        assertThat(firstPromotion.getOfferId()).isEqualTo("A29721688");
        assertThat(firstPromotion.getItemNumber()).isEqualTo("070918248");
        assertThat(firstPromotion.getZoneId()).isEqualTo("5");
        assertThat(firstPromotion.getOfferName()).isEqualTo("3 LIONS KICK & TRICK BALL 1.00 SPECIAL PURCHASE");
        assertThat(firstPromotion.getStartDate()).isEqualTo("31-Apr-12");
        assertThat(firstPromotion.getEndDate()).isEqualTo("04-May-13");
        assertThat(firstPromotion.getCFDescription1()).isEqualTo("SPECIAL PURCHASE 50p");
        assertThat(firstPromotion.getCFDescription2()).isEqualTo("3 LIONS|WATERBOTTLE");
        assertThat(firstPromotion.getShelfTalkerImage()).isEqualTo("OnSale.png");
        assertThat(firstPromotion.getOfferText()).isEqualTo("default");

    }

    @Test
    public void shouldReturnOnlyOneResultGivenDuplicateRequests() throws Exception {
        PromotionRequest promotionRequest = aPromotionRequest()
                .zoneId("5")
                .itemNumber("070918248")
                .offerId("A29721688")
                .build();

        PromotionRequest promotionRequest1 = aPromotionRequest()
                .zoneId("5")
                .itemNumber("070918248")
                .offerId("A29721688")
                .build();

        PromotionRequestList promotionRequestList = new PromotionRequestList();
        promotionRequestList.setPromotions(newArrayList(promotionRequest, promotionRequest1));

        WebResource resource = client().resource(PROMOTION_FIND_ENDPOINT);
        ClientResponse response = resource.type(APPLICATION_JSON).post(ClientResponse.class, asJson(promotionRequestList));
        assertThat(response.getStatus()).isEqualTo(200);

        List<Promotion> promotions = fromJson(resource.type(APPLICATION_JSON).post(String.class, asJson(promotionRequestList)), new TypeReference<List<Promotion>>() {
        });

        Promotion firstPromotion = promotions.get(0);
        assertThat(firstPromotion.getOfferId()).isEqualTo("A29721688");
        assertThat(firstPromotion.getItemNumber()).isEqualTo("070918248");
        assertThat(firstPromotion.getZoneId()).isEqualTo("5");
        assertThat(firstPromotion.getOfferName()).isEqualTo("3 LIONS KICK & TRICK BALL 1.00 SPECIAL PURCHASE");
        assertThat(firstPromotion.getStartDate()).isEqualTo("31-Apr-12");
        assertThat(firstPromotion.getEndDate()).isEqualTo("04-May-13");
        assertThat(firstPromotion.getCFDescription1()).isEqualTo("SPECIAL PURCHASE 50p");
        assertThat(firstPromotion.getCFDescription2()).isEqualTo("3 LIONS|WATERBOTTLE");
        assertThat(firstPromotion.getShelfTalkerImage()).isEqualTo("OnSale.png");
        assertThat(firstPromotion.getOfferText()).isEqualTo("default");

    }
}
