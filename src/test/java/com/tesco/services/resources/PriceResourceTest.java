package com.tesco.services.resources;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class PriceResourceTest extends ResourceTest {

    @Override
    protected void setUpResources() throws Exception {
        addResource(new PriceResource());
    }

    @Test
    public void shouldReturn200Response() {
        WebResource resource = client().resource("/price");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
