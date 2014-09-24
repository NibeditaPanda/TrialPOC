package com.tesco.services.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ImportInProgressException extends WebApplicationException {
    public ImportInProgressException() {
        super(Response.status(Response.Status.CONFLICT).entity("{\"message\":\"There is already an import in progress.\"}").type(MediaType.APPLICATION_JSON).build());
    }
}
