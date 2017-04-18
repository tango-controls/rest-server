package org.tango.web.server.exception.mapper;

import org.tango.client.ez.proxy.NoSuchCommandException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static org.tango.web.server.exception.mapper.Helper.getResponse;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/18/17
 */
@Provider
public class NoSuchCommand implements ExceptionMapper<NoSuchCommandException> {
    @Override
    public Response toResponse(NoSuchCommandException exception) {
        return getResponse(exception, Response.Status.NOT_FOUND);
    }
}
