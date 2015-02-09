package org.tango.web.server.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.esrf.TangoApi.AttributeInfo;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DeviceInfo;
import org.tango.web.rest.DeviceState;
import org.tango.web.rest.Response;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 24.06.14
 */
@Produces("application/json")
@Provider
public class JsonResponseWriter implements MessageBodyWriter<Object> {
    private final Set<Class<?>> supportedClasses = new HashSet<>();

    {
        Collections.addAll(supportedClasses, DeviceInfo.class, CommandInfo.class, AttributeInfo.class, Response.class,
                DeviceState.class);
    }

    private final Gson gson = new GsonBuilder()
//            .serializeNulls()
            .create();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return mediaType.equals(MediaType.APPLICATION_JSON_TYPE) && supportedClasses.contains(type);
    }

    public long getSize(Object any, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public void writeTo(Object any, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(entityStream));
        gson.toJson(any, writer);
        writer.flush();
    }
}
