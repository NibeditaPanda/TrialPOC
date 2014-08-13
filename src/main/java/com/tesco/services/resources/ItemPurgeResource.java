package com.tesco.services.resources;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.listeners.CreateDesignDocListener;
import com.tesco.couchbase.listeners.GetViewListener;
import com.tesco.couchbase.listeners.Listener;
import com.tesco.services.Configuration;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import static com.tesco.services.resources.HTTPResponses.badRequest;
import static com.tesco.services.resources.HTTPResponses.ok;
import static com.tesco.services.resources.HTTPResponses.serverError;

/**
 * Created by QT00 on 06/08/2014.
 * PS-114 to get view information from couchbase and process those products which are not update for more than 2 days
 */
@Path("/itempurge")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemPurgeResource {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private Configuration configuration;
    private CouchbaseWrapper couchbaseWrapper;
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;
    private ObjectMapper mapper;
    private CouchbaseClient couchbaseClient;

    public ItemPurgeResource(Configuration configuration, CouchbaseWrapper couchbaseWrapper,AsyncCouchbaseWrapper asyncCouchbaseWrapper,ObjectMapper mapper,CouchbaseClient couchbaseClient){
        this.configuration = configuration;
        this.couchbaseWrapper = couchbaseWrapper;
        this.asyncCouchbaseWrapper = asyncCouchbaseWrapper;
        this.mapper = mapper;
        this.couchbaseClient = couchbaseClient;
    }

    @POST
    @Path("/")
    public Response getRoot(@Context UriInfo uriInfo) {
        logger.info("message : {"+uriInfo.getRequestUri().toString()+"} "+ HttpServletResponse.SC_BAD_REQUEST+"- {"+HTTPResponses.INVALID_REQUEST+"}");
        return badRequest();
    }

    @POST
    @Path("/purge")
    public Response purgeUnUpdatedItems() {
        try {
            ProductRepository productRepository = new ProductRepository(couchbaseWrapper,asyncCouchbaseWrapper,mapper);
            productRepository.purgeUnUpdatedItems(couchbaseClient,configuration);
        }catch(InvalidViewException e){
            logger.error("error : Item purge failed due to error "+e);
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity("{\"message\":\"Item purge failed as View not found\"}").build();
        } catch (Exception e) {
            logger.error("error : Item purge failed due to error "+e);
            e.printStackTrace();
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity("{\"message\":\"Item purge failed due to error\"}").build();
        }
        logger.info("message : Purge operation completed");
        return Response.ok("{\"message\":\"Purge Completed\"}").build();
    }



}
