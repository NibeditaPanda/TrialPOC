package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.services.DAO.PriceDAO;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PriceResourceTest extends ResourceTest {

    private PriceDAO priceDAO;

    @Override
    protected void setUpResources() throws Exception {
        priceDAO = mock(PriceDAO.class);
        PriceResource priceResource = new PriceResource(priceDAO);
        RootResource rootResource = new RootResource();
        addResource(priceResource);
        addResource(rootResource);
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
    public void shouldReturnPricesForZoneCorrespondingToStoreWhenSearchingForItemAtAParticularStore() throws IOException {
        when(priceDAO.getPrice("randomItem")).thenReturn(Optional.fromNullable(getFixture("price_1")));
        when(priceDAO.getStore("randomStore")).thenReturn(Optional.fromNullable(getFixture("store_1")));

        WebResource resource = client().resource("/price/randomItem?store=randomStore");
        ClientResponse response = resource.get(ClientResponse.class);
        DBObject priceInfo = toDBObject(resource.get(String.class));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(priceInfo.get("itemNumber")).isEqualTo("randomItem");
        assertThat(priceInfo.get("price")).isEqualTo("2.00");
        assertThat(priceInfo.get("promoPrice")).isEqualTo("1.33");
        assertThat(priceInfo.get("currency")).isEqualTo("GBP");

        DBObject promotion = ((List<DBObject>) priceInfo.get("promotions")).get(0);
        assertThat(promotion.get("offerName")).isEqualTo("promo1");
        assertThat(promotion.get("startDate")).isEqualTo("date1");
        assertThat(promotion.get("endDate")).isEqualTo("date2");
        assertThat(promotion.get("cfDescription1")).isEqualTo("blah");
        assertThat(promotion.get("cfDescription2")).isEqualTo("blah");
    }

    @Test
    public void shouldReturnNationalPricesWhenSearchingForItemAtNoParticularStore() throws IOException {
        when(priceDAO.getPrice("randomItem")).thenReturn(Optional.fromNullable(getFixture("price_1")));
        when(priceDAO.getStore("some_non_existent_store")).thenReturn(Optional.<DBObject>absent());

        WebResource resource = client().resource("/price/randomItem");
        ClientResponse response = resource.get(ClientResponse.class);
        DBObject priceInfo = toDBObject(resource.get(String.class));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(priceInfo.get("itemNumber")).isEqualTo("randomItem");
        assertThat(priceInfo.get("price")).isEqualTo("3.00");
        assertThat(priceInfo.get("promoPrice")).isEqualTo("2.33");
        assertThat(priceInfo.get("currency")).isEqualTo("GBP");
    }

    @Test
    public void shouldIgnoreOtherInvalidQueryParamsIfPassedStoreParam() throws IOException {
        when(priceDAO.getPrice("randomItem")).thenReturn(Optional.fromNullable(getFixture("price_1")));
        when(priceDAO.getStore("randomStore")).thenReturn(Optional.fromNullable(getFixture("store_1")));

        WebResource resource = client().resource("/price/randomItem?store=randomStore&someinvalidparam=blah");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldReturn404ResponseWhenItemIsNotFound() {
        when(priceDAO.getPrice("some_non_existent")).thenReturn(Optional.<DBObject>absent());

        WebResource resource = client().resource("/price/some_non_existent");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).isEqualTo("Product not found");
    }

    @Test
    public void shouldReturn404ResponseWhenStoreIsNotFound() throws IOException {
        when(priceDAO.getPrice("randomItem")).thenReturn(Optional.fromNullable(getFixture("price_1")));
        when(priceDAO.getStore("some_non_existent_store")).thenReturn(Optional.<DBObject>absent());

        WebResource resource = client().resource("/price/randomItem?store=some_non_existent_store");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).isEqualTo("Store not found");
    }

    @Test
    public void shouldReturn400WhenReachingInvalidResource(){
        WebResource resource = client().resource("/non-existant");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

    @Test
    public void shouldReturn400WhenNoQueryGiven() {
        WebResource resource = client().resource("/price");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(String.class)).isEqualTo("Invalid request");
    }

    @Test
    public void shouldReturn400ResponseWhenPassedInvalidQueryParam() throws IOException {
        when(priceDAO.getPrice("randomItem")).thenReturn(Optional.fromNullable(getFixture("price_1")));

        WebResource resource = client().resource("/price/randomItem?someInvalidQuery=blah");
        ClientResponse response = resource.get(ClientResponse.class);

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

}
