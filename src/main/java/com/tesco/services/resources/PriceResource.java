package com.tesco.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/price")
@Produces(MediaType.APPLICATION_JSON)
public class PriceResource {

    @GET
    public Response get() {
        return Response.status(200).build();
    }
}
