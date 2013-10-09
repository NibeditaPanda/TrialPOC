package com.tesco.services.resources;

import com.mongodb.MongoException;
import com.mongodb.MongoInternalException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;

public class MongoUnavailableProviderTest {

    @Test
    public void Test500ReturnedIfRuntimeExceptionWhenMongoUnavailable(){

        MongoUnavailableProvider mongoUnavailableProvider = new MongoUnavailableProvider();
        MongoException exeception = new MongoInternalException("Internal Server Error.");
        Response response = mongoUnavailableProvider.toResponse(exeception);

        assertThat(response.getEntity().toString()).isEqualTo("{\"message\":\"Internal Server Error.\"}");
        assertThat(response.getStatus() == 500);
    }
}
