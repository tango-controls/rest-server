package org.tango.web.server.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.esrf.TangoApi.DeviceInfo;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.06.14
 */
@Provider
@Produces("application/json")
public class DeviceInfoResponseWriter implements MessageBodyWriter<DeviceInfo> {
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .create();

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return mediaType == MediaType.APPLICATION_JSON_TYPE && aClass.isAssignableFrom(DeviceInfo.class);
    }

    @Override
    public long getSize(DeviceInfo deviceInfo, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(DeviceInfo deviceInfo, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> stringObjectMultivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        gson.toJson(deviceInfo, new OutputStreamWriter(outputStream));
    }
}
