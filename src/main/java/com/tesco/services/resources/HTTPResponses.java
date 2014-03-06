package com.tesco.services.resources;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

public class HTTPResponses {
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String INVALID_REQUEST = "Invalid request";

    public static Response ok(Object entity) {
        return Response.status(HttpServletResponse.SC_OK).entity(entity).build();
    }

    public static Response notFound(String message) {
        return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("{\"message\":\"" + message + "\"}").build();
    }

    public static Response badRequest() {
        return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(String.format("{\"error\":\"%s\"}", INVALID_REQUEST)).build();
    }

    public static Response serverError() {
        return Response.serverError().entity(String.format("{\"message\":\"%s\"}", INTERNAL_SERVER_ERROR)).build();
    }
}
