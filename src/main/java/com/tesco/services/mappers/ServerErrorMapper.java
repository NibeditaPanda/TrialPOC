package com.tesco.services.mappers;

import com.tesco.services.resources.HTTPResponses;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ServerErrorMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception e) {
        return HTTPResponses.serverError();
    }
}
