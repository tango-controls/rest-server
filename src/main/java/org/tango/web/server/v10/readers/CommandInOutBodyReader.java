package org.tango.web.server.v10.readers;

import com.google.gson.*;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.v10.entities.CommandInOut;
import org.tango.web.server.proxy.TangoDeviceProxy;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author ingvord
 * @since 23.04.2020
 */
@Provider
public class CommandInOutBodyReader implements MessageBodyReader<CommandInOut<Object, Object>> {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CommandInOut.class, new CommandInOutDeserializer())
            .create();
    @Context
    private UriInfo uriInfo;
    @Context
    private TangoDeviceProxy proxy;
    @Context
    private Providers providers;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == CommandInOut.class;
    }

    @Override
    public CommandInOut<Object, Object> readFrom(Class<CommandInOut<Object, Object>> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return gson.fromJson(new BufferedReader(new InputStreamReader(entityStream)), CommandInOut.class);
    }

    private class CommandInOutDeserializer implements JsonDeserializer<CommandInOut<Object, Object>> {

        @Override
        public CommandInOut<Object, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                Class<?> arginType = proxy.getProxy().getCommandInfo(getCommandName(json)).getArginType();
                return new CommandInOut<>(
                        getOrNull("host", json),
                        getOrNull("device", json),
                        getOrNull("name", json),
                        context.deserialize(json.getAsJsonObject().get("input"), arginType)
                );
            } catch (TangoProxyException | NoSuchCommandException e) {
                throw new JsonParseException(e);
            }
        }

        private String getCommandName(JsonElement json) {
            return uriInfo.getPathParameters().containsKey("cmd") ? uriInfo.getPathParameters().getFirst("cmd") : json.getAsJsonObject().get("name").getAsString();
        }

        private String getOrNull(String key, JsonElement json) {
            return json.getAsJsonObject().get(key).isJsonNull() ? null : json.getAsJsonObject().get(key).getAsString();
        }
    }
}
