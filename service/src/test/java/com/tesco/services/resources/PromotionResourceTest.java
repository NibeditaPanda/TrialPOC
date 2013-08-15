package com.tesco.services.resources;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.services.Configuration;
import com.tesco.services.DAO.PriceDAO;
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

    private PriceDAO priceDAO;
    private Configuration testConfiguration = new TestConfiguration();
    private DBCollection priceCollection;
    private DBCollection storeCollection;

    @Override
    protected void setUpResources() throws Exception {
        priceDAO = new PriceDAO(testConfiguration);
        PromotionResource offerResource = new PromotionResource(priceDAO);
        addResource(offerResource);
    }

    @Before
    public void setUp() throws IOException {
        DBFactory dbFactory = new DBFactory(testConfiguration);
        dbFactory.getCollection("prices").drop();
        dbFactory.getCollection("stores").drop();
        priceCollection = dbFactory.getCollection("prices");
        storeCollection = dbFactory.getCollection("stores");

        //TODO: added data setup using builders but since this offer functionality doesn't work anyways, will need to re-setup data once feature is fleshed out.
        storeCollection.insert(new TestStoreDBObject("randomStore").withZoneId("5").build());
        storeCollection.insert(new TestStoreDBObject("zone2Store").withZoneId("2").build());
        DBObject dbPromotion = new TestPromotionDBObject("offer1").withStartDate("date1").withEndDate("date2").withName("promo").withDescription1("blah").withDescription2("blah").build();
        priceCollection.insert(new TestProductPriceDBObject("randomItem")
                .withPrice("2.00")
                .withPromotionPrice("1.33")
                .addPromotion(dbPromotion).inZone("2")
                .withPrice("3.00")
                .withPromotionPrice("2.33")
                .addPromotion(dbPromotion).inZone("5").build());
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("2.00").withPromotionPrice("1.33").inZone("2").build());

    }

    @Test
    @Ignore
    public void shouldReturnPromotionByOfferId() throws IOException, ItemNotFoundException {
        WebResource resource = client().resource("/promotion/offer1");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
        DBObject promotion = (DBObject) JSON.parse(resource.get(String.class));

        assertThat(promotion.get("offerId")).isEqualTo("offer1");
        assertThat(promotion.get("offerName")).isEqualTo("zone2 promo");
        assertThat(promotion.get("startDate")).isEqualTo("date1");
        assertThat(promotion.get("endDate")).isEqualTo("date2");
        assertThat(promotion.get("cfDescription1")).isEqualTo("blah");
        assertThat(promotion.get("cfDescription2")).isEqualTo("blah");
    }

    @Test
    @Ignore
    public void shouldReturn404ResponseWhenOfferIsNotFound() throws ItemNotFoundException {
        WebResource resource = client().resource("/promotion/a_non_existent_offer_id");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).isEqualTo("Promotion not found");
    }

    @Test
    @Ignore
    public void shouldReturn400WhenNoQueryGivenForOffer() {
        WebResource resource = client().resource("/promotion");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

    @Test
    @Ignore
    public void shouldReturn400ResponseWhenPassedInvalidQueryParamExcludingCallbackForOffer() throws IOException, ItemNotFoundException {
        WebResource resource = client().resource("/promotion/randomOffer?someInvalidQuery=blah");
        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");

        resource = client().resource("/promotion/randomOffer?someInvalidQuery=blah&callback=blah");
        response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");

        resource = client().resource("/promotion/randomOffer?callback=blah");
        response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @Ignore
    public void shouldReturn400ResponseWhenAppendingInvalidPathForOffer() throws IOException, ItemNotFoundException {
        WebResource resource = client().resource("/promotion/randomOffer/blah");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

}
