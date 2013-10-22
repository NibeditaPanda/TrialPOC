package com.tesco.services.resources;

import com.google.common.base.Optional;
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
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;

import static com.google.common.io.Files.readLines;
import static com.tesco.services.HTTPResponses.*;
import static java.nio.charset.Charset.defaultCharset;

@Path("/price/version")
@Api(value = "/price/version", description = "Price API Version")
@Produces(ResourceResponse.RESPONSE_TYPE)
public class VersionResource {

    @GET
    @ApiOperation(value = "Find version of Price API")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Version JSON not found")})
    @Metered(name = "getVersionNumber-Meter", group = "PriceServices")
    @Timed(name = "getVersionNumber-Timer", group = "PriceServices")
    @ExceptionMetered(name = "getVersionNumber-Failures", group = "PriceServices")
    public Response get(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        Optional<String> callback = Optional.fromNullable(queryParameters.getFirst("callback"));

        try {
            String version = readLines(new File("version"), defaultCharset()).get(0);
            String versionJson = String.format("{\"version\": \"%s\"}", version);

            if (callback.isPresent()) {
                return ok((callback.get() + "(" + versionJson + ")"));
            }
            return ok(versionJson);

        } catch (IOException e) {
            return notFound(e.getMessage());
        }

    }
}
