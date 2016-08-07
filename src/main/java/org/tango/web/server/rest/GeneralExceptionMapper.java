package org.tango.web.server.rest;

import org.tango.client.ez.proxy.TangoProxyException;
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
public class GeneralExceptionMapper implements ExceptionMapper<Exception>{
    @Override
    public Response toResponse(Exception exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                Responses.createFailureResult(exception)).type(MediaType.APPLICATION_JSON).build();
    }
}
