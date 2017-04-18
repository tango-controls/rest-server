package org.tango.web.server.exception.mapper;

import fr.esrf.Tango.DevFailed;
import org.tango.rest.entities.Failures;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/18/17
 */
public class Helper {
    private Helper() {
    }

    public static Response getResponse(DevFailed exception, Response.Status status) {
        return Response.status(status).entity(Failures.createInstance(exception)).type(MediaType.APPLICATION_JSON).build();
    }

    public static Response getResponse(Exception exception, Response.Status status) {
        return Response.status(status).entity(Failures.createInstance(exception)).type(MediaType.APPLICATION_JSON).build();
    }
}
