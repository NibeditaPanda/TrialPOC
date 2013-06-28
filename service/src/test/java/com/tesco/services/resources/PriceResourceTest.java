package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.services.DAO.PriceDAO;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

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

    @Test
    public void shouldReturnPricesForZoneCorrespondingToStoreWhenSearchingForItemAtAParticularStore() {
        DBObject price = (DBObject) JSON.parse("{" +
                "\"itemNumber\": \"randomItem\", " +
                "\"zones\": " +
                    "{\"5\": {\"price\": \"3.00\", \"promoPrice\" : \"2.33\", \"promotions\":[{\"offerName\":\"promo1\",\"startDate\":\"date1\",\"endDate\":\"date2\",\"cfDescription1\":\"blah\",\"cfDescription2\":\"blah\"}]}, " +
                    "\"2\": {\"price\": \"2.00\", \"promoPrice\" : \"1.33\", \"promotions\":[{\"offerName\":\"promo1\",\"startDate\":\"date1\",\"endDate\":\"date2\",\"cfDescription1\":\"blah\",\"cfDescription2\":\"blah\"}]}}" +
                "}");
        when(priceDAO.getPrice("randomItem")).thenReturn(Optional.fromNullable(price));
        DBObject store = (DBObject) JSON.parse("{\"storeId\": \"randomStore\",\"zoneId\": \"2\", \"currency\": \"GBP\" }");
        when(priceDAO.getStore("randomStore")).thenReturn(Optional.fromNullable(store));

        WebResource resource = client().resource("/price/randomItem?store=randomStore");
        ClientResponse response = resource.get(ClientResponse.class);
        String stringResponse = resource.get(String.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stringResponse).contains("\"itemNumber\":\"randomItem\"");
        assertThat(stringResponse).contains("\"price\":\"2.00\"");
        assertThat(stringResponse).contains("\"promoPrice\":\"1.33\"");
        assertThat(stringResponse).contains("\"currency\":\"GBP\"");
        assertThat(stringResponse).contains("\"offerName\":\"promo1\"");
        assertThat(stringResponse).contains("\"startDate\":\"date1\"");
        assertThat(stringResponse).contains("\"endDate\":\"date2\"");
        assertThat(stringResponse).contains("\"cfDescription1\":\"blah\"");
        assertThat(stringResponse).contains("\"cfDescription2\":\"blah\"");
    }

    @Test
    public void shouldReturnNationalPricesWhenSearchingForItemAtNoParticularStore() {
        DBObject price = (DBObject) JSON.parse("{\"itemNumber\": \"randomItem\", \"zones\": {\"5\": {\"price\": \"3.00\", \"promoPrice\" : \"2.33\"}, \"2\": {\"price\": \"2.00\", \"promoPrice\" : \"1.33\"}}}");
        when(priceDAO.getPrice("randomItem")).thenReturn(Optional.fromNullable(price));
        when(priceDAO.getStore("some_non_existent_store")).thenReturn(Optional.<DBObject>absent());

        WebResource resource = client().resource("/price/randomItem");
        ClientResponse response = resource.get(ClientResponse.class);
        String stringResponse = resource.get(String.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stringResponse).contains("\"itemNumber\":\"randomItem\"");
        assertThat(stringResponse).contains("\"price\":\"3.00\"");
        assertThat(stringResponse).contains("\"promoPrice\":\"2.33\"");
        assertThat(stringResponse).contains("\"currency\":\"GBP\"");
    }

    @Test
    public void shouldIgnoreOtherInvalidQueryParamsIfPassedStoreParam() {
        DBObject price = (DBObject) JSON.parse("{ \"itemNumber\": \"randomItem\", \"zones\": " +
                    "{\"5\": {\"price\": \"3.00\", \"promoPrice\" : \"2.33\", \"promotions\":[{\"offerName\":\"promo1\",\"startDate\":\"date1\",\"endDate\":\"date2\",\"cfDescription1\":\"blah\",\"cfDescription2\":\"blah\"}]}}}");
        when(priceDAO.getPrice("randomItem")).thenReturn(Optional.fromNullable(price));
        DBObject store = (DBObject) JSON.parse("{\"storeId\": \"randomStore\",\"zoneId\": \"5\", \"currency\": \"GBP\" }");
        when(priceDAO.getStore("randomStore")).thenReturn(Optional.fromNullable(store));

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
    }

    @Test
    public void shouldReturn404ResponseWhenStoreIsNotFound() {
        DBObject price = (DBObject) JSON.parse("{\"itemNumber\": \"randomItem\", \"zones\": {\"5\": {\"price\": \"3.00\", \"promoPrice\" : \"2.33\"}, \"2\": {\"price\": \"2.00\", \"promoPrice\" : \"1.33\"}}}");
        when(priceDAO.getPrice("randomItem")).thenReturn(Optional.fromNullable(price));
        when(priceDAO.getStore("some_non_existent_store")).thenReturn(Optional.<DBObject>absent());

        WebResource resource = client().resource("/price/randomItem?store=some_non_existent_store");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturn400WhenReachingInvalidResource(){
        WebResource resource = client().resource("/non-existant");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void shouldReturn400WhenNoQueryGiven() {
        WebResource resource = client().resource("/price");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void shouldReturn400ResponseWhenPassedInvalidQueryParam() {
        DBObject price = (DBObject) JSON.parse("{\"itemNumber\": \"randomItem\", \"zones\": {\"5\": {\"price\": \"3.00\", \"promoPrice\" : \"2.33\"}, \"2\": {\"price\": \"2.00\", \"promoPrice\" : \"1.33\"}}}");
        when(priceDAO.getPrice("randomItem")).thenReturn(Optional.fromNullable(price));

        WebResource resource = client().resource("/price/randomItem?someInvalidQuery=blah");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);

    }

}
