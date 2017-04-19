package org.tango.web.server.exception.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static org.tango.web.server.exception.mapper.Helper.getResponse;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.02.2015
 */
@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception>{
    @Override
    public Response toResponse(Exception exception) {
        return getResponse(exception, Response.Status.INTERNAL_SERVER_ERROR);
    }
}
