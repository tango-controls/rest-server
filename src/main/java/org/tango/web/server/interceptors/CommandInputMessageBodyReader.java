package org.tango.web.server.interceptors;

import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.command.CommandInput;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/8/16
 */
@Provider
public class CommandInputMessageBodyReader implements MessageBodyReader<CommandInput> {
    @Context
    private UriInfo uriInfo;
    @Context
    private TangoProxy proxy;
    @Context
    private Providers providers;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == CommandInput.class;
    }

    @Override
    public CommandInput readFrom(Class<CommandInput> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        String cmdName = uriInfo.getPathParameters().getFirst("cmd");

        try {
            Class<?> arginType = proxy.getCommandInfo(cmdName).getArginType();
            if (arginType == Void.class) return new CommandInput(cmdName, Void.class, null);

            MessageBodyReader<Object> bodyReader = (MessageBodyReader<Object>) providers.getMessageBodyReader(arginType, arginType, annotations, mediaType);

            Object converted = bodyReader.readFrom((Class<Object>) arginType, arginType, annotations, mediaType, httpHeaders, entityStream);

            return new CommandInput(cmdName, arginType, converted);
        } catch (TangoProxyException | NoSuchCommandException e) {
            throw new WebApplicationException(e);
        }
    }
}
