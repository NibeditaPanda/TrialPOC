package com.tesco.services.resources;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tesco.services.Configuration;
import com.tesco.services.repositories.DataGridResource;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportResourceTest extends ResourceTest {

    @Mock
    private Configuration mockConfiguration;

    @Mock
    private DataGridResource dataGridResource;

    @Override
    protected void setUpResources() throws Exception {
        when(mockConfiguration.getImportScript()).thenReturn("import.sh");

        addResource(new ImportResource(mockConfiguration, dataGridResource));
    }

    // TODO : Vyv this needs to actually verify that the import completed successfully (maybe async Servlet?)
    @Test
    @Ignore
    public void shouldStartImportScript() throws IOException {
        WebResource resource = client().resource("/admin/import");
        ClientResponse response = resource.post(ClientResponse.class);
        String responseText = response.getEntity(String.class);

        assertThat(responseText).isEqualTo("{\"message\":\"Import Started.\"}");
        assertThat(response.getStatus()).isEqualTo(200);
    }
}

