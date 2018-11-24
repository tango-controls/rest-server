package org.tango.web.server.resolvers;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import fr.esrf.Tango.*;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.TangoConst;
import org.apache.commons.beanutils.ConvertUtils;
import org.codehaus.jackson.*;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ser.*;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.format.TangoDataFormat;
import org.tango.client.ez.data.type.*;
import org.tango.client.ez.util.TangoImageUtils;
import org.tango.client.ez.util.TangoUtils;
import org.tango.rest.v10.JaxRsDeviceAttribute;
import org.tango.web.server.filters.TangoRestFilterProvider;

import javax.imageio.ImageIO;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 07.12.2015
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonConfiguration implements ContextResolver<ObjectMapper> {


    private ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Set human readable date format
        SimpleModule tangoModule = new SimpleModule("TangoModule", new Version(1, 9, 12, null));
        tangoModule.addSerializer(new DeviceAttributeSerializer(DeviceAttribute.class));
        tangoModule.addSerializer(new ErrSeveritySerializer(ErrSeverity.class));
        tangoModule.addSerializer(new AttrWriteTypeSerializer(AttrWriteType.class));
        tangoModule.addSerializer(new AttrDataFormatSerializer(AttrDataFormat.class));
        tangoModule.addSerializer(new AttrQualitySerializer(AttrQuality.class));
        tangoModule.addSerializer(new DispLevelSerializer(DispLevel.class));
        tangoModule.addSerializer(new PipeBlobSerializer(PipeBlob.class));
        tangoModule.addSerializer(new TangoImageSerializer(JaxRsDeviceAttribute.ImageAttributeValue.class));
        tangoModule.addSerializer(new DevStateSerializer(DevState.class));
        tangoModule.addSerializer(new PipeWriteTypeSerializer(PipeWriteType.class));
        tangoModule.addSerializer(new AttrInfoExSerializer(AttributeInfoEx.class));
        tangoModule.addSerializer(new CommandInfoSerializer(CommandInfo.class));
        tangoModule.addDeserializer(AttrWriteType.class, new AttrWriteTypeDeserializer());
        tangoModule.addDeserializer(AttrDataFormat.class, new AttrDataFormatDeserializer());
        tangoModule.addDeserializer(DispLevel.class, new DispLevelDeserializer());
        tangoModule.addDeserializer(PipeBlob.class, new PipeBlobDeserializer());

        mapper.registerModule(tangoModule);

        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public ObjectMapper getContext(Class<?> objectType) {
        TangoRestFilterProvider.JsonFieldFilter filter =
                ResteasyProviderFactory.getContextData(TangoRestFilterProvider.JsonFieldFilter.class);

        ObjectMapper mapper = newObjectMapper();

        if (filter != null) {
            FilterProvider fp = new SimpleFilterProvider().addFilter("json-response-fields-filter",
                    filter.inverse ?
                            new CustomSerializeAllExcept(filter.fieldNames) :
                            new CustomFilterOutAllExceptFilter(filter.fieldNames)
            );
            mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.getSerializationConfig().addMixInAnnotations(Object.class, JsonResponseFieldFilterMixIn.class);
            mapper.setFilters(fp);
        } else {
            mapper.setFilters(new SimpleFilterProvider());
        }


        return mapper;
    }

    public static interface PropertySerializeChecker{
        boolean serializeField(String name);
    }

    public static class CustomSerializeAllExcept extends SimpleBeanPropertyFilter.SerializeExceptFilter implements PropertySerializeChecker{
        public CustomSerializeAllExcept(Set<String> properties) {
            super(properties);
        }

        @Override
        public boolean serializeField(String name) {
            return !_propertiesToExclude.contains(name);
        }
    }

    public static class CustomFilterOutAllExceptFilter implements BeanPropertyFilter, PropertySerializeChecker {

        private Set<String> fieldNames;

        public CustomFilterOutAllExceptFilter(Set<String> fieldNames) {
            this.fieldNames = fieldNames;
        }

        @Override
        public boolean serializeField(String name) {
            return fieldNames.contains(name);
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator jgen,
                                     SerializerProvider provider, BeanPropertyWriter writer) throws Exception {
            if (!writer.getPropertyType().isPrimitive()
                    && !String.class.isAssignableFrom(writer.getPropertyType())
                    && !writer.getPropertyType().isEnum()) {
                Object o = writer.get(bean);
                if (o != null && Iterables.any(
                        Arrays.asList(o.getClass().getFields()), new Predicate<Field>() {
                            @Override
                            public boolean apply(Field input) {
                                return fieldNames.contains(input.getName());
                            }
                        })) {
                    jgen.writeFieldName(writer.getName());
                    provider.defaultSerializeValue(o, jgen);
                }

            } else if (fieldNames.contains(writer.getName()))
                writer.serializeAsField(bean, jgen, provider);
        }
    }

    public static class AttrWriteTypeSerializer extends org.codehaus.jackson.map.ser.std.SerializerBase<AttrWriteType> {
        public AttrWriteTypeSerializer(Class<AttrWriteType> t) {
            super(t);
        }

        @Override
        public void serialize(AttrWriteType value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(value.toString());
        }
    }

    public static class AttrDataFormatSerializer extends org.codehaus.jackson.map.ser.std.SerializerBase<AttrDataFormat> {
        public AttrDataFormatSerializer(Class<AttrDataFormat> t) {
            super(t);
        }

        @Override
        public void serialize(AttrDataFormat value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(value.toString());
        }
    }

    private static class AttrQualitySerializer extends org.codehaus.jackson.map.ser.std.SerializerBase<AttrQuality>  {
        public AttrQualitySerializer(Class<AttrQuality> t) {
            super(t);
        }

        @Override
        public void serialize(AttrQuality attrQuality, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
            jgen.writeString(attrQuality.toString());
        }
    }

    public static class DispLevelSerializer extends org.codehaus.jackson.map.ser.std.SerializerBase<DispLevel> {
        public DispLevelSerializer(Class<DispLevel> t) {
            super(t);
        }

        @Override
        public void serialize(DispLevel value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(value.toString());
        }
    }

    public static class DevStateSerializer extends org.codehaus.jackson.map.ser.std.SerializerBase<DevState> {
        public DevStateSerializer(Class<DevState> t) {
            super(t);
        }

        @Override
        public void serialize(DevState value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(value.toString());
        }
    }

    public static class PipeBlobSerializer extends org.codehaus.jackson.map.ser.std.SerializerBase<PipeBlob> {
        public PipeBlobSerializer(Class<PipeBlob> t) {
            super(t);
        }

        @Override
        public void serialize(PipeBlob value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            try {
                jgen.writeStartArray();
                for (PipeDataElement element : value) {
                    jgen.writeStartObject();
                    jgen.writeObjectField("name", element.getName());
                    switch (element.getType()) {
                        case TangoConst.Tango_DEV_PIPE_BLOB:
                            jgen.writeObjectField("value", element.extractPipeBlob());
                            break;
                        case TangoConst.Tango_DEV_BOOLEAN:
                            jgen.writeObjectField("value", element.extractBooleanArray());
                            break;
                        case TangoConst.Tango_DEV_SHORT:
                            jgen.writeObjectField("value", element.extractShortArray());
                            break;
                        case TangoConst.Tango_DEV_LONG:
                            jgen.writeObjectField("value", element.extractLongArray());
                            break;
                        case TangoConst.Tango_DEV_LONG64:
                            jgen.writeObjectField("value", element.extractLong64Array());
                            break;
                        case TangoConst.Tango_DEV_FLOAT:
                            jgen.writeObjectField("value", element.extractFloatArray());
                            break;
                        case TangoConst.Tango_DEV_DOUBLE:
                            jgen.writeObjectField("value", element.extractDoubleArray());
                            break;
                        case TangoConst.Tango_DEV_STRING:
                            jgen.writeObjectField("value", element.extractStringArray());
                            break;
                        case TangoConst.Tango_DEV_USHORT:
                            jgen.writeObjectField("value", element.extractUShortArray());
                            break;
                        case TangoConst.Tango_DEV_ULONG:
                            jgen.writeObjectField("value", element.extractULongArray());
                            break;
                        case TangoConst.Tango_DEV_ULONG64:
                            jgen.writeObjectField("value", element.extractULong64Array());
                            break;
                        case TangoConst.Tango_DEV_STATE:
                            jgen.writeObjectField("value", element.extractDevStateArray());
                            break;
                        //TODO ?!
                    }
                    jgen.writeEndObject();
                }
                jgen.writeEndArray();
            } catch (DevFailed devFailed) {
                throw new IOException(TangoUtils.convertDevFailedToException(devFailed));
            }
        }
    }

    private static class PipeWriteTypeSerializer extends org.codehaus.jackson.map.ser.std.SerializerBase<PipeWriteType> {
        protected PipeWriteTypeSerializer(Class<PipeWriteType> t) {
            super(t);
        }

        @Override
        public void serialize(PipeWriteType value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
             jgen.writeString(value.toString());
        }
    }

    private static class AttrInfoExSerializer extends org.codehaus.jackson.map.ser.std.SerializerBase<AttributeInfoEx> {
        Logger logger = LoggerFactory.getLogger(AttrInfoExSerializer.class);

        protected AttrInfoExSerializer(Class<AttributeInfoEx> t) {
            super(t);
        }

        @Override
        public void serialize(AttributeInfoEx value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            Optional<PropertySerializeChecker> optionalFilter = Optional.ofNullable(getFilter(provider));
            jgen.writeStartObject();

            for(Field fld : AttributeInfo.class.getDeclaredFields()){
                String fldName = fld.getName();
                if(optionalFilter.isPresent() && !optionalFilter.get().serializeField(fldName)) continue;
                writeField(value, jgen, provider, fld, fldName);
            }

            for(Field fld : AttributeInfoEx.class.getDeclaredFields()){
                String fldName = fld.getName();
                if(optionalFilter.isPresent() && !optionalFilter.get().serializeField(fldName)) continue;
                writeField(value, jgen, provider, fld, fldName);
            }

            jgen.writeEndObject();
        }

        private PropertySerializeChecker getFilter(SerializerProvider provider) {
            try {
                return (PropertySerializeChecker) provider.getFilterProvider().findFilter("json-response-fields-filter");
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        private void writeField(AttributeInfoEx value, JsonGenerator jgen, SerializerProvider provider, Field fld, String fldName) throws IOException {
            jgen.writeFieldName(fldName);
            try {
                if (fldName.equals("data_type"))
                    jgen.writeString(TangoConst.Tango_CmdArgTypeName[fld.getInt(value)]);
                else
                    provider.defaultSerializeValue(fld.get(value), jgen);
            } catch (IllegalAccessException e) {
                logger.error("Failed to deserialize field {}",fldName);
                logger.error(e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    private static class DeviceAttributeSerializer  extends org.codehaus.jackson.map.ser.std.SerializerBase<DeviceAttribute> {
        public DeviceAttributeSerializer(Class<DeviceAttribute> t) {
            super(t);
        }

        @Override
        public void serialize(DeviceAttribute value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            try {
                jgen.writeStartObject();

                if(!value.hasFailed()) {
                    TangoDataFormat<?> dataFormat = TangoDataFormat.createForAttrDataFormat(value.getDataFormat());
                    TangoDataType<?> dataType = dataFormat.getDataType(value.getType());
                    jgen.writeStringField("name", value.getName());

                    jgen.writeFieldName("value");
                    provider.defaultSerializeValue(dataType.extract(TangoDataWrapper.create(value, null)), jgen);
                    jgen.writeStringField("quality", value.getQuality().toString());
                    jgen.writeFieldName("timestamp");
                    jgen.writeNumber(value.getTime());
                }
                else {
                    jgen.writeStringField("name", value.getName());
                    jgen.writeArrayFieldStart("errors");
                    for(DevError error : value.getErrStack()){
                        provider.defaultSerializeValue(error, jgen);
                    }
                    jgen.writeEndArray();

                    jgen.writeStringField("quality", "FAILURE");
                    jgen.writeFieldName("timestamp");
                    jgen.writeNumber(System.currentTimeMillis());
                }

                jgen.writeEndObject();
            } catch (ValueExtractionException | UnknownTangoDataType e) {
                throw new JsonGenerationException(e);
            } catch (DevFailed devFailed) {
                throw new JsonGenerationException(TangoUtils.convertDevFailedToException(devFailed));
            }
        }
    }

    private static class ErrSeveritySerializer extends org.codehaus.jackson.map.ser.std.SerializerBase<ErrSeverity> {
        public ErrSeveritySerializer(Class<ErrSeverity> type) {
            super(type);
        }

        @Override
        public void serialize(ErrSeverity value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(value.toString());
        }
    }

    private static class CommandInfoSerializer extends org.codehaus.jackson.map.ser.std.SerializerBase<CommandInfo> {
        public CommandInfoSerializer(Class<CommandInfo> commandInfoClass) {
            super(commandInfoClass);
        }

        @Override
        public void serialize(CommandInfo value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            try {
                jgen.writeStartObject();

                for(Field fld : CommandInfo.class.getDeclaredFields()){
                    if("TangoTypesArray".equals(fld.getName())) continue;
                    jgen.writeFieldName(fld.getName());
                    if("in_type".equals(fld.getName()) || "out_type".equals(fld.getName()))
                        jgen.writeString(TangoConst.Tango_CmdArgTypeName[fld.getInt(value)]);
                    else
                        provider.defaultSerializeValue(fld.get(value), jgen);
                }

                jgen.writeEndObject();
            } catch (IllegalAccessException e) {
                throw new JsonGenerationException(e);
            }
        }
    }

    @JsonFilter("json-response-fields-filter")
    class JsonResponseFieldFilterMixIn {

    }

    private class TangoImageSerializer extends
            org.codehaus.jackson.map.ser.std.SerializerBase<JaxRsDeviceAttribute.ImageAttributeValue> {

        public TangoImageSerializer(Class<JaxRsDeviceAttribute.ImageAttributeValue> t) {
            super(t);
        }

        @Override
        public void serialize(
                JaxRsDeviceAttribute.ImageAttributeValue image,
                JsonGenerator jgen, SerializerProvider provider) throws IOException {
            TangoImage value = image.value;
            RenderedImage img = TangoImageUtils.toRenderedImage_sRGB(
                    (int[]) value.getData(), value.getWidth(), value.getHeight());
            jgen.flush();

            ByteArrayOutputStream bos = new ByteArrayOutputStream(value.getWidth() * value.getHeight() * 4);
            OutputStream out = new Base64.OutputStream(bos);
            //TODO write directly to output stream produces exception:
            //TODO  JsonGenerationException: Can not write a field name, expecting a value
            ImageIO.write(img, "jpeg", out);
            jgen.writeString("data:/jpeg;base64," + new String(bos.toByteArray()));

            jgen.flush();
        }


    }

    private class AttrWriteTypeDeserializer extends JsonDeserializer<AttrWriteType> {
        @Override
        public AttrWriteType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String attrWriteType = jp.readValueAs(String.class).toUpperCase();
            return TangoUtils.attrWriteTypeFromString(attrWriteType);
        }
    }

    private class AttrDataFormatDeserializer extends JsonDeserializer<AttrDataFormat> {
        @Override
        public AttrDataFormat deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String attrDataFormat = jp.readValueAs(String.class).toUpperCase();
            return TangoUtils.attrDataFormatFromString(attrDataFormat);
        }
    }

    private class DispLevelDeserializer extends JsonDeserializer<DispLevel> {
        @Override
        public DispLevel deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String dispLevel = jp.readValueAs(String.class).toUpperCase();
            return TangoUtils.displayLevelFromString(dispLevel);
        }
    }

    private class PipeBlobDeserializer extends JsonDeserializer<PipeBlob> {
        @Override
        public PipeBlob deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Iterator<JsonNode> dataElements = jp.readValuesAs(JsonNode.class);

            PipeBlobBuilder bld = new PipeBlobBuilder("");

            while(dataElements.hasNext()){
                JsonNode dataItem = dataElements.next();

                String dataItemName = dataItem.get("name").asText();
                JsonNode dataItemValue = dataItem.get("value");
                String dataItemDataType = dataItem.get("type").asText();
                try {
                    deserializeArray(bld, dataItemValue, dataItemName, dataItemDataType, ctxt);
                } catch (UnknownTangoDataType unknownTangoDataType) {
                    throw new JsonParseException("Failed to deserialize pipe data:" + dataItemValue.asText(), jp.getCurrentLocation());
                }
            }

            return bld.build();
        }

        private void deserializeArray(PipeBlobBuilder bld, final JsonNode dataItemValue, String dataItemName, String dataItemDataType, final DeserializationContext ctxt) throws IOException, UnknownTangoDataType {
            switch (dataItemDataType) {
                case "DevPipeBlob":
                    for (JsonNode json : dataItemValue) {
                        bld.add(dataItemName, ctxt.getParser().getCodec().treeAsTokens(json).readValueAs(PipeBlob.class));
                    }
                    return;
                case "DevString":
                    bld.add(dataItemName, Iterables.toArray(Iterables.transform(dataItemValue, new Function<JsonNode, String>() {
                        @Override
                        public String apply(JsonNode input) {
                            return input.asText();
                        }
                    }), String.class));
                    return;
                case "DevState":
                    bld.add(dataItemName, Iterables.toArray(
                            Iterables.transform(dataItemValue, new Function<JsonNode, DevState>() {
                                @Override
                                public DevState apply(JsonNode input) {
                                    return StateUtilities.getStateForName(input.asText());
                                }
                            }),
                            DevState.class));
                    return;
                case "DevBoolean":
                    boolean[] result = new boolean[dataItemValue.size()];

                    for (int i = 0, size = dataItemValue.size(); i < size; i++) {
                        result[i] = dataItemValue.get(i).asBoolean();
                    }

                    bld.add(dataItemName, result);
                    return;
                default:
                    final TangoDataType tangoDataType = TangoDataTypes.forString(dataItemDataType);
                    bld.add(dataItemName, (Object) (Iterables.toArray(
                            Iterables.transform(dataItemValue, new Function<JsonNode, Object>() {
                                @Override
                                public Object apply(JsonNode input) {
                                    return ConvertUtils.convert(input.asText(), tangoDataType.getDataTypeClassBoxed());
                                }
                            }),
                            tangoDataType.getDataTypeClassBoxed())));
            }
        }
    }
}
