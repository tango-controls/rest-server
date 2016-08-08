package org.tango.rest.rc2;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import fr.esrf.Tango.AttributeConfig;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.AttributeInfo;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoApi.CommandInfo;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.TangoDataType;
import org.tango.client.ez.data.type.TangoDataTypes;
import org.tango.client.ez.data.type.UnknownTangoDataType;
import org.tango.client.ez.data.type.ValueExtractionException;
import org.tango.client.ez.proxy.*;
import org.tango.rest.DeviceHelper;
import org.tango.rest.SupportedAuthentication;
import org.tango.rest.entities.*;
import org.tango.rest.response.Response;
import org.tango.rest.response.Responses;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.EventHelper;
import org.tango.web.server.providers.Partitionable;
import org.tango.web.server.providers.StaticValue;
import org.tango.web.server.util.DeviceInfos;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.11.2015
 */
@Path("/rc2")
@Produces("application/json")
public class Rc2ApiImpl {
    private final Logger logger = LoggerFactory.getLogger(Rc2ApiImpl.class);

    @Context
    protected UriInfo uriInfo;

    public static final String ASYNC = "async";
    public static final String REST_PREFIX = "/rest/rc2";

    @GET
    public Map<String, String> authentication(@Context ServletContext context){
        Map<String, String> result = new HashMap<>();

        result.put("devices", context.getContextPath() + REST_PREFIX + "/devices");
        result.put("x-auth-method", SupportedAuthentication.VALUE);

        return result;
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("devices")
    public Object devices(@QueryParam("wildcard") String wildcard,
                          @Context DatabaseDs db,
                          @Context final ServletContext context) {
        try {
            List<String> result = db.getDeviceList(wildcard == null ? "*" : wildcard);
            List<NamedEntity> transform = Lists.transform(result, new Function<String, NamedEntity>() {
                @Override
                public NamedEntity apply(final String input) {
                    return new NamedEntity(input, context.getContextPath() + REST_PREFIX + "/devices/" + input);
                }
            });
            return transform;
        } catch (NoSuchCommandException | TangoProxyException e) {
            return Responses.createFailureResult(e);
        }
    }

    @GET
    @StaticValue
    @Path("devices/{domain}/{family}/{member}")
    public Object device(@Context TangoProxy proxy,
                         @Context DatabaseDs db,
                         @Context final ServletContext context) {
        try {
            final String href = uriInfo.getPath();
            return new Device(proxy.getName(),
                    DeviceInfos.fromDeviceInfo(db.getDeviceInfo(proxy.getName())),
                    href + "/attributes",
                    href + "/commands",
                    href + "/pipes",
                    href + "/properties", uriInfo.getAbsolutePath());
        } catch (NoSuchCommandException | TangoProxyException e) {
            return Responses.createFailureResult(e);
        }
    }

    @GET
    @org.tango.web.server.providers.AttributeValue
    @Path("devices/{domain}/{family}/{member}/state")
    public Object deviceState(@Context TangoProxy proxy,
                              @Context ServletContext context) {
        try {
            final String href = context.getContextPath() + "/rest/rc1/" + proxy.getName();
            final DeviceAttribute[] ss = proxy.toDeviceProxy().read_attribute(new String[]{"State", "Status"});
            DeviceState result = new DeviceState(ss[0].extractDevState().toString(), ss[1].extractString(), new Object() {
                public String _state = href + "/State";
                public String _status = href + "/Status";
                public String _parent = href;
                public String _self = href + "/state";
            });

            return result;
        } catch (DevFailed devFailed) {
            return new DeviceState(DevState.UNKNOWN.toString(), String.format("Failed to read state&status from %s", proxy.getName()));
        }
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("devices/{domain}/{family}/{member}/attributes")
    public Object deviceAttributes(@Context final TangoProxy proxy,
                                   @Context ServletContext context) throws Exception {
        final String href = uriInfo.getPath();

        return Lists.transform(
                Arrays.asList(proxy.toDeviceProxy().get_attribute_info_ex()), new Function<AttributeInfoEx, Object>() {
                    @Override
                    public Object apply(final AttributeInfoEx input) {
                        return DeviceHelper.attributeInfoExToResponse(input.name, href);
                    }
                });
    }

    @GET
    @StaticValue
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}")
    public Object deviceAttribute(@PathParam("attr") final String attrName,
                                  @Context UriInfo uriInfo,
                                  @Context TangoProxy proxy,
                                  @Context ServletContext context) throws Exception {
        final String href = uriInfo.getPath();

        return DeviceHelper.attributeInfoExToResponse(proxy.toDeviceProxy().get_attribute_info_ex(attrName).name, href);
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}/history")
    public Object deviceAttributeHistory(@PathParam("attr") final String attrName,
                                  @Context UriInfo uriInfo,
                                  @Context TangoProxy proxy,
                                         @Context ServletContext context) throws Exception {
        return Lists.transform(Arrays.asList(proxy.toDeviceProxy().attribute_history(attrName)), new Function<DeviceDataHistory, Object>() {
            @Override
            public Object apply(DeviceDataHistory input) {
                if(!input.hasFailed()){
                    try {
                        TangoDataWrapper wrapper = TangoDataWrapper.create(input);

                        TangoDataType<?> type = TangoDataTypes.forTangoDevDataType(input.getType());

                        return new AttributeValue<Object>(input.getName(), type.extract(wrapper), "VALID", input.getTime());
                    } catch (UnknownTangoDataType|DevFailed|ValueExtractionException e) {
                        return Responses.createFailureResult(e);
                    }
                } else {
                    return Responses.createFailureResult(new DevFailed(input.getErrStack()));
                }
            }
        });
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}/history")
    public Object deviceAttributeHistory(@PathParam("attr") final String attrName,
                                         @Context TangoProxy proxy) throws DevFailed {
        return proxy.toDeviceProxy().attribute_history(attrName);
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}/info")
    public AttributeInfo deviceAttributeInfo(@PathParam("attr") final String attrName,
                                               @Context TangoProxy proxy) throws DevFailed {
        return proxy.toDeviceProxy().get_attribute_info(attrName);
    }

    @PUT
    @Consumes("application/json")
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}/info")
    public AttributeInfo deviceAttributeInfoPut(@PathParam("attr") final String attrName,
                                               @QueryParam(ASYNC) boolean async,
                                               @Context TangoProxy proxy,
                                               AttributeConfig config) throws DevFailed {
        proxy.toDeviceProxy().set_attribute_info(new AttributeInfo[]{new AttributeInfo(config)});
        if (async) return null;
        return proxy.toDeviceProxy().get_attribute_info(attrName);
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}/properties")
    public DbAttribute deviceAttributeProperties(@PathParam("attr") final String attrName,
                                               @Context TangoProxy proxy) throws DevFailed {
        return proxy.toDeviceProxy().get_attribute_property(attrName);
    }

    @PUT
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}/properties/{prop}")
    public DbAttribute deviceAttributePropertyPut(@PathParam("attr") final String attrName,
                                                 @PathParam("prop") final String propName,
                                                 @QueryParam("value") final String propValue,
                                                 @QueryParam(ASYNC) boolean async,
                                                 @Context TangoProxy proxy) throws DevFailed {
        DbAttribute dbAttribute = proxy.toDeviceProxy().get_attribute_property(attrName);

        DbDatum datum = dbAttribute.datum(propName);

        if(datum != null) datum.insert(propValue);
        else dbAttribute.add(propName, propValue);

        proxy.toDeviceProxy().put_attribute_property(dbAttribute);

        if(async) return null;
        return dbAttribute;
    }

    @DELETE
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}/properties/{prop}")
    public void deviceAttributePropertyDelete(@PathParam("attr") final String attrName,
                                               @PathParam("prop") final String propName,
                                               @Context TangoProxy proxy) throws DevFailed {
        DbAttribute dbAttribute = proxy.toDeviceProxy().get_attribute_property(attrName);

        for (Iterator<DbDatum> iterator = dbAttribute.iterator(); iterator.hasNext(); ) {
            DbDatum dbDatum = iterator.next();
            if (dbDatum.name.equalsIgnoreCase(propName)) {
                iterator.remove();
                break;
            }
        }
        proxy.toDeviceProxy().put_attribute_property(dbAttribute);
    }

    @GET
    @org.tango.web.server.providers.AttributeValue
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}/value")
    public Object deviceAttributeValueGet(@PathParam("attr") final String attrName,
                                          @Context TangoProxy proxy) throws Exception {
        final ValueTimeQuality<Object> result = proxy.readAttributeValueTimeQuality(attrName);


        return new Object() {
            public String name = attrName;
            public Object value = result.getValue();
            public String quality = result.getQuality().name();
            public long timestamp = result.getTime();
            public Object _links;
        };
    }

    @PUT
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}")
    public Object deviceAttributeValuePut(@PathParam("attr") String attrName,
                                          @QueryParam("value") String value,
                                          @QueryParam(ASYNC) boolean async,
                                          @Context TangoProxy proxy) throws Exception {
        TangoAttributeInfoWrapper attributeInfo = proxy.getAttributeInfo(attrName);
        Class<?> targetType = attributeInfo.getClazz();
        Object converted = ConvertUtils.convert(value, targetType);

        proxy.writeAttribute(attrName, converted);
        if (!async)
            return deviceAttributeValueGet(attrName, proxy);
        else return null;
    }

    @PUT
    @Path("devices/{domain}/{family}/{member}/attributes")
    public Object deviceAttributesPut(@Context final TangoProxy proxy,
                                      @Context UriInfo uriInfo,
                                      @Context ServletContext context,
                                      @Context HttpServletRequest request) throws DevFailed {
        final String href = uriInfo.getPath();
        Map<String, String[]> parametersMap = new HashMap<>(request.getParameterMap());
        boolean async = parametersMap.remove(ASYNC) != null;
        String[] attrNames = Iterables.toArray(parametersMap.keySet(), String.class);
        DeviceAttribute[] attrs = Iterables.toArray(
                Iterables.transform(parametersMap.entrySet(), new Function<Map.Entry<String, String[]>, DeviceAttribute>() {
                    @Override
                    public DeviceAttribute apply(Map.Entry<String, String[]> input) {
                        try {
                            DeviceAttribute result = new DeviceAttribute(input.getKey());

                            Class<?> clazz = proxy.getAttributeInfo(input.getKey()).getClazz();
                            TangoDataType<?> tangoDataType = TangoDataTypes.forClass(clazz);

                            ((TangoDataType<Object>) tangoDataType).insert(
                                    TangoDataWrapper.create(result), ConvertUtils.convert(input.getValue()[0], clazz));

                            return result;
                        } catch (Exception e) {
                            logger.warn("Failed to transform input {}={} into DeviceAttribute[{}/{}]:{}",
                                    input.getKey(), input.getValue()[0], proxy.getName(), input.getKey(), e.getMessage());
                            return null;
                        }
                    }
                })
                , DeviceAttribute.class);

        proxy.toDeviceProxy().write_attribute(attrs);
        if (!async)
            return Iterables.transform(Arrays.asList(proxy.toDeviceProxy().read_attribute(attrNames)), new Function<DeviceAttribute, Object>() {
                @Override
                public Object apply(final DeviceAttribute input) {
                    try {
                        return new Object() {
                            public String name = input.getName();
                            public Object value = TangoDataTypes.forTangoDevDataType(input.getType()).extract(TangoDataWrapper.create(input));
                            public String quality = input.getQuality().toString();
                            public long timestamp = input.getTime();
                            public Object _links = new Object() {
                                public String _parent = href + "/" + name;
                                public String _self = href + "/" + name + "/value";
                            };
                        };
                    } catch (DevFailed devFailed) {
                        return Responses.createFailureResult(devFailed);
                    } catch (ValueExtractionException e) {
                        return Responses.createFailureResult(e);
                    } catch (UnknownTangoDataType unknownTangoDataType) {
                        return Responses.createFailureResult(unknownTangoDataType);
                    }
                }
            });
        else return null;
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}/{event}")
    public Object deviceAttributeEvent(@PathParam("domain") String domain,
                                       @PathParam("family") String family,
                                       @PathParam("member") String member,
                                       @PathParam("attr") final String attrName,
                                       @PathParam("event") String event,
                                       @QueryParam("timeout") long timeout,
                                       @QueryParam("state") EventHelper.State state,
                                       @Context ServletContext context,
                                       @Context TangoProxy proxy) throws InterruptedException, URISyntaxException {
        TangoEvent tangoEvent;
        try {
            tangoEvent = TangoEvent.valueOf(event.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Responses.createFailureResult("Unsupported event: " + event);
        }
        try {
            final Response<?> result = EventHelper.handleEvent(attrName, timeout, state, proxy, tangoEvent);
            return new AttributeValue(attrName, result.argout, result.quality, result.timestamp);
        } catch (NoSuchAttributeException | TangoProxyException e) {
            return Responses.createFailureResult("Failed to subscribe to event " + uriInfo.getPath(), e);
        }
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("devices/{domain}/{family}/{member}/commands")
    public Object deviceCommands(@Context TangoProxy proxy,
                                 @Context UriInfo uriInfo) throws DevFailed {
        final String href = uriInfo.getPath();
        return Lists.transform(Arrays.asList(proxy.toDeviceProxy().command_list_query()), new Function<CommandInfo, Object>() {
            @Override
            public Object apply(final CommandInfo input) {
                return DeviceHelper.commandInfoToResponse(input, href);
            }
        });
    }

    @GET
    @StaticValue
    @Path("devices/{domain}/{family}/{member}/commands/{command}")
    public Object deviceCommand(@PathParam("command") String cmdName,
                                @Context TangoProxy proxy,
                                @Context UriInfo uriInfo) throws DevFailed {
        return DeviceHelper.commandInfoToResponse(proxy.toDeviceProxy().command_query(cmdName), uriInfo.getPath());
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/commands/{command}/history")
    public Object deviceCommandHistory(@PathParam("command") String cmdName,
                                @Context TangoProxy proxy,
                                @Context UriInfo uriInfo) throws DevFailed {
        return Lists.transform(
                Arrays.asList(proxy.toDeviceProxy().command_history(cmdName)), new Function<DeviceDataHistory, Object>() {
            @Override
            public Object apply(DeviceDataHistory input) {
                if(!input.hasFailed()) {
                    try {
                        TangoDataWrapper wrapper = TangoDataWrapper.create(input);

                        TangoDataType type = TangoDataTypes.forTangoDevDataType(input.getType());

                        CommandResult<Object, Object> result = new CommandResult<>();

                        result.name = input.getName();
                        result.input = null;
                        result.output = type.extract(wrapper);


                        return result;
                    } catch (UnknownTangoDataType|DevFailed|ValueExtractionException e) {
                        return Responses.createFailureResult(e);
                    }
                } else {
                    return Responses.createFailureResult(new DevFailed(input.getErrStack()));
                }
            }
        });
    }

    @PUT
    @Path("devices/{domain}/{family}/{member}/commands/{command}")
    public Object deviceCommandPut(@PathParam("command") final String cmdName,
                                   @QueryParam("input") String[] value,
                                   @QueryParam("async") boolean async,
                                   @Context TangoProxy proxy,
                                   @Context UriInfo uriInfo) throws Exception {
        final String href = uriInfo.getPath();

        Class<?> type = proxy.getCommandInfo(cmdName).getArginType();

        final Object converted;
        if(type == Void.class) converted = null;
        else converted = ConvertUtils.convert(value.length == 1 ? value[0]: value, type);
        if (async) {
            DeviceData data = new DeviceData();

            ((TangoDataType<Object>) TangoDataTypes.forClass(type)).insert(TangoDataWrapper.create(data), converted);

            proxy.toDeviceProxy().command_inout_asynch(cmdName, data);
            return null;
        } else {
            final Object result = proxy.executeCommand(cmdName, converted);
            return new Object() {
                public String name = cmdName;
                public Object input = converted;
                public Object output = result;
                public Object _links = new Object() {
                    public String _self = href;
                };
            };
        }
    }

    @GET
    @Partitionable
    @org.tango.web.server.providers.AttributeValue
    @Path("devices/{domain}/{family}/{member}/properties")
    public Object deviceProperties(@Context TangoProxy proxy) throws DevFailed {
        String[] propnames = proxy.toDeviceProxy().get_property_list("*");
        if(propnames.length == 0) return propnames;
        return Iterables.transform(
                Arrays.asList(proxy.toDeviceProxy().get_property(propnames)),
                new Function<DbDatum, Object>() {
                    @Override
                    public Object apply(final DbDatum input) {
                        return DeviceHelper.dbDatumToResponse(input);
                    }
                });
    }

    @PUT
    @Path("devices/{domain}/{family}/{member}/properties")
    public Object devicePropertiesPut(@Context HttpServletRequest request,
                                      @Context TangoProxy proxy) throws DevFailed {
        Map<String, String[]> parametersMap = new HashMap<>(request.getParameterMap());
        boolean async = parametersMap.remove(ASYNC) != null;

        DbDatum[] input = Iterables.toArray(Iterables.transform(parametersMap.entrySet(), new Function<Map.Entry<String, String[]>, DbDatum>() {
            @Override
            public DbDatum apply(Map.Entry<String, String[]> input) {
                return new DbDatum(input.getKey(), input.getValue());
            }
        }), DbDatum.class);

        proxy.toDeviceProxy().put_property(input);

        if (async)
            return null;
        else return deviceProperties(proxy);
    }

    @POST
    @Path("devices/{domain}/{family}/{member}/properties")
    public Object devicePropertiesPost(@Context HttpServletRequest request,
                                       @Context TangoProxy proxy) throws DevFailed {
        return devicePropertiesPut(request, proxy);
    }

    @GET
    @org.tango.web.server.providers.AttributeValue
    @Path("devices/{domain}/{family}/{member}/properties/{property}")
    public Object deviceProperty(@PathParam("property") String propName,
                                 @Context TangoProxy proxy) throws DevFailed {
        return DeviceHelper.dbDatumToResponse(proxy.toDeviceProxy().get_property(propName));
    }

    @PUT
    @Path("devices/{domain}/{family}/{member}/properties/{property}")
    public Object devicePropertyPut(@PathParam("property") String propName,
                                    @Context HttpServletRequest request,
                                    @Context TangoProxy proxy) throws DevFailed {
        Map<String, String[]> parametersMap = request.getParameterMap();
        boolean async = parametersMap.containsKey(ASYNC);

        DbDatum input = new DbDatum(propName, parametersMap.get("value"));

        proxy.toDeviceProxy().put_property(input);

        if (async)
            return null;
        else return DeviceHelper.dbDatumToResponse(proxy.toDeviceProxy().get_property(propName));
    }

    @POST
    @Path("devices/{domain}/{family}/{member}/properties/{property}")
    public Object devicePropertyPost(@PathParam("property") String propName,
                                     @Context HttpServletRequest request,
                                     @Context TangoProxy proxy) throws DevFailed {
        return devicePropertyPut(propName, request, proxy);
    }

    @DELETE
    @Path("devices/{domain}/{family}/{member}/properties/{property}")
    public void devicePropertyDelete(@PathParam("property") String propName,
                                     @Context HttpServletRequest request,
                                     @Context TangoProxy proxy) throws DevFailed {
        proxy.toDeviceProxy().delete_property(propName);
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("devices/{domain}/{family}/{member}/pipes")
    public Object devicePipes(@Context UriInfo uriInfo,
                              @Context TangoProxy proxy) throws DevFailed {
        final String href = uriInfo.getPath();
        return Lists.transform(proxy.toDeviceProxy().getPipeNames(), new Function<String, Object>() {
            @Override
            public Object apply(final String input) {
                return new Object() {
                    public String name = input;
                    public String value = href + "/" + name + "/value";
                    public Object _links = new Object() {
                        public String _self = href + "/" + name;
                    };
                };
            }


        });
    }

    @GET
    @org.tango.web.server.providers.AttributeValue
    @Path("devices/{domain}/{family}/{member}/pipes/{pipe}")
    public Object devicePipeGet(@PathParam("pipe") final String pipeName,
                                @Context UriInfo uriInfo,
                                @Context TangoProxy proxy) throws DevFailed {
        final String href = uriInfo.getPath();
        final DevicePipe result = proxy.toDeviceProxy().readPipe(pipeName);
        return new Object() {
            public String name = pipeName;
            public int size = result.getPipeBlob().size();
            public long timestamp = result.getTimeValMillisSec();
            public Object data = result.getPipeBlob();
            public Object _links = new Object() {
                public String _self = href;
            };
        };
    }

    @PUT
    @Path("devices/{domain}/{family}/{member}/pipes/{pipe}")
    @Consumes("application/json")
    public Object devicePipePut(@PathParam("pipe") String pipeName,
                                @QueryParam("async") boolean async,
                                @Context UriInfo info,
                                @Context TangoProxy proxy,
                                PipeBlob blob) throws DevFailed {
        proxy.toDeviceProxy().writePipe(pipeName, blob);
        if (async) return null;
        else return devicePipeGet(pipeName, info, proxy);
    }
}
