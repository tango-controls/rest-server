package org.tango.web.server.rest;

import fr.esrf.Tango.DevFailed;
import org.tango.rest.response.Responses;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.02.2015
 */
@Provider
public class DevFailedMapper implements ExceptionMapper<DevFailed> {
    @Override
    public Response toResponse(DevFailed exception) {
        return Response.ok().entity(
                Responses.createFailureResult(exception)).type(MediaType.APPLICATION_JSON).build();
    }
}
