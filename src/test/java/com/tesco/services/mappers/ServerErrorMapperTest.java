package com.tesco.services.mappers;

import com.mongodb.MongoException;
import com.mongodb.MongoInternalException;
import com.tesco.services.resources.MongoUnavailableProvider;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;

public class ServerErrorMapperTest {

    @Test
    public void shouldReturnCustom500Error(){
        ServerErrorMapper serverErrorMapper = new ServerErrorMapper();
        Exception exception = new Exception("Internal Server Error.");
        Response response = serverErrorMapper.toResponse(exception);

        assertThat(response.getEntity().toString()).isEqualTo("{\"message\":\"Internal Server Error.\"}");
        assertThat(response.getStatus() == 500);
    }
}
