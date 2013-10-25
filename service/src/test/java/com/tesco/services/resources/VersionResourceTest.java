package com.tesco.services.resources;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class VersionResourceTest extends ResourceTest {
    @Override
    protected void setUpResources() throws Exception {
        VersionResource versionResource = new VersionResource();
        addResource(versionResource);
    }

    @Test
    public void shouldGetCorrectVersion() throws Exception {
        WebResource resource = client().resource("/price/version");
        ClientResponse response = resource.get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);

    }
}
