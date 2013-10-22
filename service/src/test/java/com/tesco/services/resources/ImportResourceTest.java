package com.tesco.services.resources;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.services.Configuration;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportResourceTest extends ResourceTest {

    @Mock
    RuntimeWrapper mockRuntimeWrapper;

    @Mock
    Configuration mockConfiguration;

    @Override
    protected void setUpResources() throws Exception {
        when(mockConfiguration.getImportScript()).thenReturn("import.sh");

        addResource(new ImportResource(mockConfiguration, mockRuntimeWrapper));
    }

    @Test
    public void shouldStartImportScript() throws IOException {
        WebResource resource = client().resource("/admin/import");
        ClientResponse response = resource.post(ClientResponse.class);
        String responseText = response.getEntity(String.class);

        assertThat(responseText).isEqualTo("{\"message\":\"Import Started.\"}");
        assertThat(response.getStatus()).isEqualTo(200);

        verify(mockRuntimeWrapper).exec("import.sh");
    }
}

