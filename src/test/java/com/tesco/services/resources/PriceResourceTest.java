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

    String priceForProduct = "{\"itemNumber\": \"053752428\", \"zones\": {\"5\": {\"price\": \"3\"}}}";
    private DBCollection prices;

    @Override
    protected void setUpResources() throws Exception {
        Configuration configuration = new TestConfiguration();
        DBFactory dbFactory = new DBFactory(configuration);
        dbFactory.getCollection("prices").drop();

        prices = dbFactory.getCollection("prices");
        prices.insert((DBObject) JSON.parse(priceForProduct));

        addResource(new PriceResource(new PriceDAO(configuration)));
    }

    @Test
    public void shouldReturn200ResponseWhenItemIsFound() {
        WebResource resource = client().resource("/price?item_number=053752428");
        ClientResponse response = resource.get(ClientResponse.class);
        String stringResponse = resource.get(String.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stringResponse).contains("\"itemNumber\":\"053752428\"");
        assertThat(stringResponse).contains("\"price\":\"3\"");
    }

    @Test
    public void shouldReturn404ResponseWhenItemIsNotFound() {
        WebResource resource = client().resource("/price?item_number=some_non_existant");
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
