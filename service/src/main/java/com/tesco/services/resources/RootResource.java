package com.tesco.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RootResource {

    @GET
    public Response getRoot(){
        return Response.status(400).build();
    }

    @GET
    @Path("/{invalidPath}")
    public Response get(){
        return Response.status(400).build();
    }
}
