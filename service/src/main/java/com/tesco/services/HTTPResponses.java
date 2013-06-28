package com.tesco.services;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

public class HTTPResponses {

    public static Response ok(Object entity) {
        return Response.status(HttpServletResponse.SC_OK).entity(entity).build();
    }

    public static Response notFound(String message) {
        return Response.status(HttpServletResponse.SC_NOT_FOUND).entity(message).build();
    }

    public static Response badRequest() {
        return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity("Invalid request").build();
    }
}
