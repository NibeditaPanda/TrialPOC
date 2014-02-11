package com.tesco.services.mappers;

import com.sun.jersey.api.NotFoundException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;

public class InvalidUrlMapperTest {

    @Test
    public void shouldReturnCustom404Error(){
        InvalidUrlMapper invalidUrlMapper = new InvalidUrlMapper();
        NotFoundException exception = new NotFoundException("Not Found.");
        Response response = invalidUrlMapper.toResponse(exception);

        assertThat(response.getEntity().toString()).isEqualTo("{\"error\":\"Invalid request\"}");
        assertThat(response.getStatus() == 400);
    }
}
