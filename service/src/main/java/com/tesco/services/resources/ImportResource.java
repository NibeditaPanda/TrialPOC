package com.tesco.services.resources;

import com.tesco.services.Configuration;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/admin")
@Api(value = "/admin", description = "Price API administrative endpoints")
@Produces(ResourceResponse.RESPONSE_TYPE)
public class ImportResource {
    private Configuration configuration;
    private RuntimeWrapper runtimeWrapper;

    public ImportResource(Configuration configuration, RuntimeWrapper runtimeWrapper) {
        this.configuration = configuration;
        this.runtimeWrapper = runtimeWrapper;
    }


    @GET
    @Path("/import")
    @ApiOperation(value = "Reload Price and Promotion data")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Invalid Import Configuration")})
    @Metered(name="postImport-Meter",group="PriceServices")
    @Timed(name="postImport-Timer",group="PriceServices")
    @ExceptionMetered(name="postImport-Failures",group="PriceServices")
    public Response getPIMHierarchy() {

        try {
            runtimeWrapper.exec(configuration.getImportScript());
        } catch (IOException e) {
            Response.serverError();
        }

        return Response.ok("{\"message\":\"Import Started.\"}").build();
    }
}
