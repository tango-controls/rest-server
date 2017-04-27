package org.tango.web.server.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger logger = LoggerFactory.getLogger(GeneralExceptionMapper.class);
    @Override
    public Response toResponse(Exception exception) {
        logger.error(exception.getLocalizedMessage(), exception);
        return getResponse(exception, Response.Status.INTERNAL_SERVER_ERROR);
    }
}
