package com.tesco.services.mappers;

import com.sun.jersey.api.NotFoundException;
import com.tesco.services.resources.HTTPResponses;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class InvalidUrlMapper implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException e) {
        return HTTPResponses.badRequest();
    }
}
