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
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class PriceResourceOfferTest extends ResourceTest {

    private PriceDAO priceDAO;
    private Configuration testConfiguration = new TestConfiguration();
    private DBCollection priceCollection;
    private DBCollection storeCollection;

    @Override
    protected void setUpResources() throws Exception {
        priceDAO = new PriceDAO(testConfiguration);
        PriceResource priceResource = new PriceResource(priceDAO);
        RootResource rootResource = new RootResource();
        addResource(priceResource);
        addResource(rootResource);
    }

    @Before
    public void setUp() throws IOException {
        DBFactory dbFactory = new DBFactory(testConfiguration);
        dbFactory.getCollection("prices").drop();
        dbFactory.getCollection("stores").drop();
        priceCollection = dbFactory.getCollection("prices");
        storeCollection = dbFactory.getCollection("stores");
        priceCollection.insert(getFixture("price_1"));
        priceCollection.insert(getFixture("price_2"));
        storeCollection.insert(getFixture("store_1"));
        storeCollection.insert(getFixture("store_2"));
    }

    public DBObject getFixture(String fixtureName) throws IOException {
        String workingDir = System.getProperty("user.dir");
        String filePath = String.format("%s/src/test/java/com/tesco/services/resources/fixtures/%s.json", workingDir, fixtureName);
        return toDBObject(new String(Files.readAllBytes(Paths.get(filePath))));
    }

    public DBObject toDBObject(String data) {
        return (DBObject) JSON.parse(data);
    }

    @Test
    @Ignore
    public void shouldReturnPricesAndPromotionsWhenFetchingByOfferIdAtAParticularStore() throws IOException, ItemNotFoundException {
        WebResource resource = client().resource("/price/offer/offer1?store=randomStore");
        ClientResponse response = resource.get(ClientResponse.class);
        DBObject priceInfo = toDBObject(resource.get(String.class));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(priceInfo.get("itemNumber")).isEqualTo("randomItem");
        assertThat(priceInfo.get("price")).isEqualTo("2.00");
        assertThat(priceInfo.get("promoPrice")).isEqualTo("1.33");
        assertThat(priceInfo.get("currency")).isEqualTo("GBP");

        DBObject promotion = ((List<DBObject>) priceInfo.get("promotions")).get(0);
        assertThat(promotion.get("offerId")).isEqualTo("123");
        assertThat(promotion.get("offerName")).isEqualTo("zone2 promo");
        assertThat(promotion.get("startDate")).isEqualTo("date1");
        assertThat(promotion.get("endDate")).isEqualTo("date2");
        assertThat(promotion.get("cfDescription1")).isEqualTo("blah");
        assertThat(promotion.get("cfDescription2")).isEqualTo("blah");
    }

    @Test
    @Ignore
    public void shouldReturnPricesAndPromotionsFromNationalZoneWhenFetchingByOfferIdAtNoParticularStore() throws IOException, ItemNotFoundException {
        WebResource resource = client().resource("/price/offer/offer1");
        ClientResponse response = resource.get(ClientResponse.class);
        DBObject priceInfo = toDBObject(resource.get(String.class));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(priceInfo.get("itemNumber")).isEqualTo("randomItem");
        assertThat(priceInfo.get("price")).isEqualTo("3.00");
        assertThat(priceInfo.get("promoPrice")).isEqualTo("2.33");
        assertThat(priceInfo.get("currency")).isEqualTo("GBP");

        DBObject promotion = ((List<DBObject>) priceInfo.get("promotions")).get(0);
        assertThat(promotion.get("offerId")).isEqualTo("456");
        assertThat(promotion.get("offerName")).isEqualTo("zone5 promo");
        assertThat(promotion.get("startDate")).isEqualTo("date1");
        assertThat(promotion.get("endDate")).isEqualTo("date2");
        assertThat(promotion.get("cfDescription1")).isEqualTo("blah");
        assertThat(promotion.get("cfDescription2")).isEqualTo("blah");
    }

    @Test
    @Ignore
    public void shouldReturn404ResponseWhenOfferIsNotFound() throws ItemNotFoundException {
        WebResource resource = client().resource("/price/offer/some_non_existent");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).isEqualTo("Product not found");
    }

    @Test
    @Ignore
    public void shouldReturn404ResponseWhenStoreIsNotFoundForOffer() throws IOException, ItemNotFoundException {
        WebResource resource = client().resource("/price/offer/randomOffer?store=some_non_existent_store");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).isEqualTo("Store not found");
    }

    @Test
    @Ignore
    public void shouldReturn400WhenNoQueryGivenForOffer() {
        WebResource resource = client().resource("/price/offer");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

    @Test
    @Ignore
    public void shouldReturn400ResponseWhenPassedInvalidQueryParamExcludingCallbackForOffer() throws IOException, ItemNotFoundException {
        WebResource resource = client().resource("/price/offer/randomOffer?someInvalidQuery=blah");
        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");

        resource = client().resource("/price/offer/randomOffer?someInvalidQuery=blah&callback=blah");
        response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");

        resource = client().resource("/price/offer/randomOffer?callback=blah");
        response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @Ignore
    public void shouldReturn400ResponseWhenAppendingInvalidPathForOffer() throws IOException, ItemNotFoundException {
        WebResource resource = client().resource("/price/offer/randomOffer/blah");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

}
