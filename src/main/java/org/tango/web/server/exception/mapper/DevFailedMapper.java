package org.tango.web.server.exception.mapper;

import fr.esrf.Tango.DevFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.utils.DevFailedUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static org.tango.web.server.exception.mapper.Helper.getResponse;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.02.2015
 */
@Provider
public class DevFailedMapper implements ExceptionMapper<DevFailed> {
    private final Logger logger = LoggerFactory.getLogger(DevFailedMapper.class);

    @Override
    public Response toResponse(DevFailed exception) {
        logger.warn(exception.getLocalizedMessage());
        return getResponse(exception, Response.Status.BAD_REQUEST);
    }
}
