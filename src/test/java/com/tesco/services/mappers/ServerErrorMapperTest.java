package com.tesco.services.mappers;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;

public class ServerErrorMapperTest {

    @Test
    public void shouldReturnCustom500Error(){
        ServerErrorMapper serverErrorMapper = new ServerErrorMapper();
        Exception exception = new Exception("Internal Server Error.");
        Response response = serverErrorMapper.toResponse(exception);
        /*Modified by Abrar for PS-166- Start */
        assertThat(response.getEntity().toString()).isEqualTo("{\"error\":\"Internal Server Error\"}");
        /*Modified by Abrar for PS-166- End */
        assertThat(response.getStatus() == 500);
    }
}
