package com.tesco.services.resources;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.services.Configuration;
import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.DAO.PriceKeys;
import com.tesco.services.DAO.PromotionDAO;
import com.tesco.services.DBFactory;
import com.tesco.services.Exceptions.ItemNotFoundException;
import com.tesco.services.resources.fixtures.TestProductPriceDBObject;
import com.tesco.services.resources.fixtures.TestPromotionDBObject;
import com.tesco.services.resources.fixtures.TestStoreDBObject;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

public class PromotionResourceTest extends ResourceTest {

    private Configuration testConfiguration = new TestConfiguration();
    private DBCollection promotionCollection;

    @Override
    protected void setUpResources() throws Exception {
        PromotionResource offerResource = new PromotionResource(new PromotionDAO(testConfiguration));
        addResource(offerResource);
    }

    @Before
    public void setUp() throws IOException {
        DBFactory dbFactory = new DBFactory(testConfiguration);
        dbFactory.getCollection(PriceKeys.PROMOTION_COLLECTION).drop();
        promotionCollection = dbFactory.getCollection(PriceKeys.PROMOTION_COLLECTION);

        DBObject dbPromotion = new TestPromotionDBObject("offer1").withStartDate("date1").withEndDate("date2").withName("name of promotion").withDescription1("blah").withDescription2("blah").build();
        promotionCollection.insert(dbPromotion);
    }

    @Test
    public void shouldReturnPromotionByOfferId() throws IOException, ItemNotFoundException {
        WebResource resource = client().resource("/promotion/offer1");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
        DBObject promotion = (DBObject) JSON.parse(resource.get(String.class));

        assertThat(promotion.get("offerId")).isEqualTo("offer1");
        assertThat(promotion.get("offerName")).isEqualTo("name of promotion");
        assertThat(promotion.get("startDate")).isEqualTo("date1");
        assertThat(promotion.get("endDate")).isEqualTo("date2");
        assertThat(promotion.get("cfDescription1")).isEqualTo("blah");
        assertThat(promotion.get("cfDescription2")).isEqualTo("blah");
    }

    @Test
    public void shouldReturn404ResponseWhenOfferIsNotFound() throws ItemNotFoundException {
        WebResource resource = client().resource("/promotion/a_non_existent_offer_id");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).isEqualTo("Promotion not found");
    }

    @Test
    public void shouldReturn400WhenNoQueryGivenForOffer() {
        WebResource resource = client().resource("/promotion");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

    @Test
    public void shouldReturn400ResponseWhenAppendingInvalidPathForOffer() throws IOException, ItemNotFoundException {
        WebResource resource = client().resource("/promotion/randomOffer/blah");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

}
