package com.tesco.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.tesco.services.HTTPResponses.badRequest;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RootResource {

    @GET
    public Response getRoot(){
        return badRequest();
    }

    @GET
    @Path("/{invalidPath}")
    public Response get(){
        return badRequest();
    }
}
