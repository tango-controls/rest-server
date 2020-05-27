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
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author ingvord
 * @since 23.04.2020
 */
@Provider
public class CommandInOutBodyReader implements MessageBodyReader<CommandInOut<Object, Object>> {
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
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CommandInOut.class, new JsonDeserializer<CommandInOut<Object, Object>>() {
                    @Override
                    public CommandInOut<Object, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        try {
                            Class<?> arginType = proxy.getProxy().getCommandInfo(getCommandName(json)).getArginType();
                            JsonElement input = Optional.ofNullable(json.getAsJsonObject().get("input")).orElseGet(() -> JsonNull.INSTANCE);
                            Object parsedInput;
                            if (input.isJsonPrimitive() || input.isJsonArray()) {
                                parsedInput = context.deserialize(input, arginType);
                            } else if (input.isJsonObject()) {
                                MessageBodyReader<Object> bodyReader = (MessageBodyReader<Object>) providers.getMessageBodyReader(arginType, arginType, annotations, mediaType);

                                InputStream is = new ByteArrayInputStream(json.getAsJsonObject().get("input").toString().getBytes("UTF-8"));//TODO from mediaType

                                parsedInput = bodyReader.readFrom((Class<Object>) arginType, arginType, annotations, mediaType, httpHeaders, is);
                            } else {
                                parsedInput = null;
                            }


                            return new CommandInOut<Object, Object>(
                                    getOrNull("host", json),
                                    getOrNull("device", json),
                                    getOrNull("name", json),
                                    parsedInput
                            );
                        } catch (TangoProxyException | NoSuchCommandException | IOException e) {
                            throw new JsonParseException(e);
                        }
                    }
                })
                .create();
        return gson.fromJson(new BufferedReader(new InputStreamReader(entityStream)), CommandInOut.class);
    }

    private String getCommandName(JsonElement json) {
        return uriInfo.getPathParameters().containsKey("cmd") ? uriInfo.getPathParameters().getFirst("cmd") : json.getAsJsonObject().get("name").getAsString();
    }

    private String getOrNull(String key, JsonElement json) {
        return json.getAsJsonObject().get(key).isJsonNull() ? null : json.getAsJsonObject().get(key).getAsString();
    }
}
