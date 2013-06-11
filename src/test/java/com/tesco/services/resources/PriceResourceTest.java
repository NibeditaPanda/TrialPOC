package com.tesco.services.resources;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.services.Configuration;
import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.DBFactory;
import com.tesco.services.TestConfiguration;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class PriceResourceTest extends ResourceTest {

    String priceOne = "{\"itemNumber\": \"053752428\", \"zones\": {\"5\": {\"price\": \"3\"}, \"2\": {\"price\": \"1\"}}}";
    String priceTwo = "{\"itemNumber\": \"053752429\", \"zones\": {\"2\": {\"price\": \"3\"}}}";
    String storeOne = "{\"storeId\": \"2002\",\"zoneId\": \"5\", \"currency\": \"GBP\" }";
    String storeTwo = "{\"storeId\": \"2006\",\"zoneId\": \"2\", \"currency\": \"GBP\" }";
    private DBCollection prices;
    private DBCollection stores;

    @Override
    protected void setUpResources() throws Exception {
        Configuration configuration = new TestConfiguration();
        DBFactory dbFactory = new DBFactory(configuration);
        dbFactory.getCollection("prices").drop();
        dbFactory.getCollection("stores").drop();

        prices = dbFactory.getCollection("prices");
        prices.insert((DBObject) JSON.parse(priceOne));
        prices.insert((DBObject) JSON.parse(priceTwo));

        stores = dbFactory.getCollection("stores");
        stores.insert((DBObject) JSON.parse(storeOne));
        stores.insert((DBObject) JSON.parse(storeTwo));

        addResource(new PriceResource(new PriceDAO(configuration)));
    }

    @Test
    public void shouldReturn200ResponseWhenSearchingForItemAndStore() {
        WebResource resource = client().resource("/price?item_number=053752428&store=2006");
        ClientResponse response = resource.get(ClientResponse.class);
        String stringResponse = resource.get(String.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stringResponse).contains("\"itemNumber\":\"053752428\"");
        assertThat(stringResponse).contains("\"price\":\"1\"");
        assertThat(stringResponse).contains("\"currency\":\"GBP\"");
    }

    @Test
    public void shouldReturn200ResponseWithNationalPriceWhenSearchingForItemAndNoStore() {
        WebResource resource = client().resource("/price?item_number=053752428");
        ClientResponse response = resource.get(ClientResponse.class);
        String stringResponse = resource.get(String.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stringResponse).contains("\"itemNumber\":\"053752428\"");
        assertThat(stringResponse).contains("\"price\":\"3\"");
        assertThat(stringResponse).contains("\"currency\":\"GBP\"");
    }

    @Test
    public void shouldReturn404ResponseWhenItemIsNotFound() {
        WebResource resource = client().resource("/price?item_number=some_non_existant");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturn404ResponseWhenStoreIsNotFound() {
        WebResource resource = client().resource("/price?item_number=053752428&store=some_non_existant");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturn404WhenNoQueryGiven() {
        WebResource resource = client().resource("/price");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturn404WhenInvalidQueryParamGiven() {
        WebResource resource = client().resource("/price?something=blah");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

}
