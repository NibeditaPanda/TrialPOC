package com.tesco.services.resources;

import com.mongodb.MongoException;
import com.tesco.services.HTTPResponses;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MongoUnavailableProvider implements ExceptionMapper<MongoException> {

    @Override
    public Response toResponse(MongoException runtimeException) {
        return HTTPResponses.serverError();
    }
}
