package org.tango.web.server.rest;

import fr.esrf.TangoApi.DeviceInfo;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.06.14
 */
@Provider
@Produces("application/json")
public class DeviceInfoResponseWriter extends AbsJsonResponseWriter<DeviceInfo> {
    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.equals(mediaType) && aClass.isAssignableFrom(DeviceInfo.class);
    }
}
