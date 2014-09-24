package com.tesco.services.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
/** Added by Salman for PS-242 to handle exception if import is in progress **/
public class ImportInProgressException extends WebApplicationException {
    public ImportInProgressException() {
        super(Response.status(Response.Status.CONFLICT).entity("{\"message\":\"There is already an import in progress.\"}").type(MediaType.APPLICATION_JSON).build());
    }
}
