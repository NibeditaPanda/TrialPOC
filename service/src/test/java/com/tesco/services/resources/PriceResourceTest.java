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
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class PriceResourceTest extends ResourceTest {

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
    }

    @Test
    public void shouldReturnPricesAndPromotionsForStoreWhenSearchingForItemAtAParticularStore() throws IOException, ItemNotFoundException {
        storeCollection.insert(new TestStoreDBObject("randomStore").withZoneId("2").build());
        DBObject dbPromotion = new TestPromotionDBObject("offer1").withStartDate("date1").withEndDate("date2").withName("zone2 promo").withDescription1("blah").withDescription2("blah").build();
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("2.00").withPromotionPrice("1.33").addPromotion(dbPromotion).inZone("2").build());

        WebResource resource = client().resource("/price/itemNumber/randomItem?store=randomStore");
        ClientResponse response = resource.get(ClientResponse.class);
        DBObject priceInfo = (DBObject) JSON.parse(resource.get(String.class));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(priceInfo.get("itemNumber")).isEqualTo("randomItem");
        assertThat(priceInfo.get("price")).isEqualTo("2.00");
        assertThat(priceInfo.get("promoPrice")).isEqualTo("1.33");
        assertThat(priceInfo.get("currency")).isEqualTo("GBP");

        DBObject promotion = ((List<DBObject>) priceInfo.get("promotions")).get(0);
        assertThat(promotion.get("offerId")).isEqualTo("offer1");
        assertThat(promotion.get("offerName")).isEqualTo("zone2 promo");
        assertThat(promotion.get("startDate")).isEqualTo("date1");
        assertThat(promotion.get("endDate")).isEqualTo("date2");
        assertThat(promotion.get("cfDescription1")).isEqualTo("blah");
        assertThat(promotion.get("cfDescription2")).isEqualTo("blah");
    }

    @Test
    public void shouldReturnPricesAndPromotionsFromNationalZoneWhenSearchingForItemAtNoParticularStore() throws IOException, ItemNotFoundException {
        DBObject dbPromotion = new TestPromotionDBObject("offer1").withStartDate("date1").withEndDate("date2").withName("zone5 promo").withDescription1("blah").withDescription2("blah").build();
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").addPromotion(dbPromotion).inZone("5").build());

        WebResource resource = client().resource("/price/itemNumber/randomItem");
        ClientResponse response = resource.get(ClientResponse.class);
        DBObject priceInfo = (DBObject) JSON.parse(resource.get(String.class));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(priceInfo.get("itemNumber")).isEqualTo("randomItem");
        assertThat(priceInfo.get("price")).isEqualTo("3.00");
        assertThat(priceInfo.get("promoPrice")).isEqualTo("2.33");
        assertThat(priceInfo.get("currency")).isEqualTo("GBP");

        DBObject promotion = ((List<DBObject>) priceInfo.get("promotions")).get(0);
        assertThat(promotion.get("offerName")).isEqualTo("zone5 promo");
        assertThat(promotion.get("startDate")).isEqualTo("date1");
        assertThat(promotion.get("endDate")).isEqualTo("date2");
        assertThat(promotion.get("cfDescription1")).isEqualTo("blah");
        assertThat(promotion.get("cfDescription2")).isEqualTo("blah");
    }

    @Test
    public void shouldReturnPriceWithoutPromotionInformationIfPromotionDoesNotExist() throws IOException, ItemNotFoundException {
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/itemNumber/randomItem");
        ClientResponse response = resource.get(ClientResponse.class);
        DBObject priceInfo = (DBObject) JSON.parse(resource.get(String.class));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(priceInfo.get("itemNumber")).isEqualTo("randomItem");
        assertThat(priceInfo.get("price")).isEqualTo("3.00");
        assertThat(priceInfo.keySet()).doesNotContain("promotions");
    }

    @Test
    public void shouldIgnoreOtherInvalidQueryParamsIfPassedStoreParam() throws IOException, ItemNotFoundException {
        storeCollection.insert(new TestStoreDBObject("randomStore").withZoneId("5").build());
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/itemNumber/randomItem?store=randomStore&someinvalidparam=blah");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldReturn404IfItemExistsAndStoreExistsButItemIsNotAssociatedWithThatZone() throws IOException, ItemNotFoundException {
        storeCollection.insert(new TestStoreDBObject("zone2Store").withZoneId("2").build());
        priceCollection.insert(new TestProductPriceDBObject("zone1Item").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/itemNumber/zone1Item?store=zone2Store");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).isEqualTo("Product not found");
    }

    @Test
    public void shouldReturn404ResponseWhenItemIsNotFound() throws ItemNotFoundException {
        WebResource resource = client().resource("/price/itemNumber/some_non_existent");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).isEqualTo("Product not found");
    }

    @Test
    public void shouldReturn404ResponseWhenStoreIsNotFound() throws IOException, ItemNotFoundException {
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/itemNumber/randomItem?store=some_non_existent_store");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).isEqualTo("Store not found");
    }

    @Test
    public void shouldReturn400WhenReachingInvalidResource() {
        WebResource resource = client().resource("/non-existant");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

    @Test
    public void shouldReturn400WhenNoQueryTypeGiven() {
        WebResource resource = client().resource("/price");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

    @Test
    public void shouldReturn400WhenNoQueryGivenForItem() {
        WebResource resource = client().resource("/price/itemNumber");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

    @Test
    public void shouldReturn400ResponseWhenPassedInvalidQueryParam() throws IOException, ItemNotFoundException {
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/itemNumber/randomItem?someInvalidQuery=blah");
        ClientResponse response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");

        resource = client().resource("/price/itemNumber/randomItem?someInvalidQuery=blah&callback=blah");
        response = resource.get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

    @Test
    public void shouldReturn400ResponseWhenTryingToAccessRootURL() {
        WebResource resource = client().resource("/");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

    @Test
    public void shouldReturn400ResponseWhenAppendingInvalidPath() throws IOException, ItemNotFoundException {
        priceCollection.insert(new TestProductPriceDBObject("randomItem").withPrice("3.00").withPromotionPrice("2.33").inZone("5").build());

        WebResource resource = client().resource("/price/itemNumber/randomItem/blah");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

}
