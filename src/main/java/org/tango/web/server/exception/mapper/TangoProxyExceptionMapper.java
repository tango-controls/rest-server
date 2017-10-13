package org.tango.web.server.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.TangoProxyException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static org.tango.web.server.exception.mapper.Helper.getResponse;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.02.2015
 */
@Provider
public class TangoProxyExceptionMapper implements ExceptionMapper<TangoProxyException> {
    private final Logger logger = LoggerFactory.getLogger(TangoProxyExceptionMapper.class);
    @Override
    public Response toResponse(TangoProxyException exception) {
        logger.error(exception.getLocalizedMessage(), exception);
        return getResponse(exception, Response.Status.INTERNAL_SERVER_ERROR);
    }
}
