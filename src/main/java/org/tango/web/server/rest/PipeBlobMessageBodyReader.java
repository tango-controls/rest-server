package org.tango.web.server.rest;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gson.*;
import fr.esrf.TangoApi.PipeBlob;
import fr.esrf.TangoApi.PipeBlobBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 09.12.2015
 */
@Provider
@Consumes("application/json")
public class PipeBlobMessageBodyReader implements MessageBodyReader<PipeBlob> {
    private static final Logger logger = LoggerFactory.getLogger(PipeBlobMessageBodyReader.class);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(PipeBlob.class, new JsonDeserializer<PipeBlob>() {
                @Override
                public PipeBlob deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    PipeBlobBuilder bld = new PipeBlobBuilder(json.getAsJsonObject().get("name").getAsString());

                    for (JsonElement dataItem : json.getAsJsonObject().get("data").getAsJsonArray()) {
                        String dataItemName = dataItem.getAsJsonObject().get("name").getAsString();
                        JsonElement dataItemValue = dataItem.getAsJsonObject().get("value");

                        deserializeArray(bld, dataItemValue.getAsJsonArray(), dataItemName, context);
                    }

                    return bld.build();
                }

                private void deserializeArray(PipeBlobBuilder bld, JsonArray jsonArray, String name, JsonDeserializationContext context) {
                    if(jsonArray.get(0).isJsonObject()){
                        bld.add(name, context.deserialize(jsonArray.get(0), PipeBlob.class));
                    } else if (jsonArray.get(0).getAsJsonPrimitive().isBoolean())
                        bld.add(name, Iterables.toArray(
                                Iterables.transform(jsonArray, new Function<JsonElement, Boolean>() {
                                    @Override
                                    public Boolean apply(JsonElement input) {
                                        return input.getAsJsonPrimitive().getAsBoolean();
                                    }
                                }), Boolean.class));
                    else if (jsonArray.get(0).getAsJsonPrimitive().isNumber())
                        bld.add(name, Iterables.toArray(
                                Iterables.transform(jsonArray, new Function<JsonElement, Double>() {
                                    @Override
                                    public Double apply(JsonElement input) {
                                        return input.getAsJsonPrimitive().getAsDouble();
                                    }
                                }), Double.class));
                    else
                        bld.add(name, Iterables.toArray(
                                Iterables.transform(jsonArray, new Function<JsonElement, String>() {
                                    @Override
                                    public String apply(JsonElement input) {
                                        return input.getAsJsonPrimitive().getAsString();
                                    }
                                }), String.class));
                }
            })
            .create();


    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == PipeBlob.class;
    }

    @Override
    public PipeBlob readFrom(Class<PipeBlob> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return GSON.fromJson(new InputStreamReader(entityStream), PipeBlob.class);
    }
}
