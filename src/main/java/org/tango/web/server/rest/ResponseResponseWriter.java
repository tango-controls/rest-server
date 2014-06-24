package org.tango.web.server.rest;

import org.tango.web.server.Responses;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 24.06.14
 */
@Provider
@Produces("application/json")
public class ResponseResponseWriter extends AbsJsonResponseWriter<Responses> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.equals(mediaType) && type.isAssignableFrom(Responses.class);
    }
}
