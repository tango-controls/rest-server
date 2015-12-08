package org.tango.rest.rc1;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DeviceAttribute;
import org.apache.commons.beanutils.ConvertUtils;
import org.javatuples.Triplet;
import org.jboss.resteasy.annotations.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.attribute.Quality;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.*;
import org.tango.client.ez.proxy.DeviceProxyWrapper;
import org.tango.client.ez.proxy.TangoAttributeInfoWrapper;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.client.ez.util.TangoUtils;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.Responses;
import org.tango.web.server.providers.TangoDatabaseBackend;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.11.2015
 */
@Path("/rc1")
@Produces("application/json")
public class Rc1ApiImpl {
    private static final Logger logger = LoggerFactory.getLogger(Rc1ApiImpl.class);


    public static final String ASYNC = "async";

    @GET
    @Cache(maxAge = 10)
    @TangoDatabaseBackend
    @Path("devices")
    public Object devices(@QueryParam("wildcard") String wildcard,
                          @Context DatabaseDs db,
                          @Context final ServletContext context) {
        try {
            Iterable<String> result = db.getDeviceList(wildcard == null ? "*" : wildcard);
            Iterable<Object> transform = Iterables.transform(result, new Function<String, Object>() {
                @Override
                public Object apply(final String input) {
                    return new Object() {
                        public final String name = input;
                        public final String href = context.getContextPath() + "/rest/rc1/devices/" + input;
                    };
                }
            });
            return transform;
        } catch (TangoProxyException e) {
            return Response.ok(Responses.createFailureResult(e)).build();
        }
    }

    @GET
    @Cache(maxAge = 10)
    @Path("devices/{domain}/{family}/{member}")
    public Object device(@Context TangoProxy proxy,
                         @Context UriInfo uriInfo,
                         @Context final ServletContext context) {
        try {
            DatabaseDs db = (DatabaseDs) context.getAttribute(DatabaseDs.TANGO_DB);
            final String href = uriInfo.getPath();
            return new Device(proxy.getName(), db.getDeviceInfo(proxy.getName()),
                    Collections2.transform(Arrays.asList(proxy.toDeviceProxy().get_attribute_info_ex()), new Function<AttributeInfoEx, NamedEntity>() {
                        @Override
                        public NamedEntity apply(AttributeInfoEx input) {
                            return new NamedEntity(input.name, href + "/attributes/" + input.name);
                        }
                    }),
                    Collections2.transform(Arrays.asList(proxy.toDeviceProxy().command_list_query()), new Function<CommandInfo, NamedEntity>() {
                        @Override
                        public NamedEntity apply(CommandInfo input) {
                            return new NamedEntity(input.cmd_name, href + "/commands/" + input.cmd_name);
                        }
                    }),
                    Collections2.transform(Arrays.asList(proxy.toDeviceProxy().get_property_list("*")), new Function<String, NamedEntity>() {
                        @Override
                        public NamedEntity apply(String input) {
                            return new NamedEntity(input, href + "/properties/" + input);
                        }
                    }), href);
        } catch (DevFailed devFailed) {
            return Responses.createFailureResult(TangoUtils.convertDevFailedToException(devFailed));
        } catch (TangoProxyException e) {
            return Responses.createFailureResult(e);
        }
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/state")
    public Object deviceState(@Context TangoProxy proxy,
                              @Context ServletContext context) {
        try {
            final String href = context.getContextPath() + "/rest/rc1/" + proxy.getName();
            final DeviceAttribute[] ss = proxy.toDeviceProxy().read_attribute(new String[]{"State", "Status"});
            Object result = new Object() {
                public String state = ss[0].extractDevState().toString();
                public String status = ss[1].extractString();
                public Object _links = new Object() {
                    public String _state = href + "/State";
                    public String _status = href + "/Status";
                    public String _parent = href;
                    public String _self = href + "/state";
                };
            };

            return result;
        } catch (DevFailed devFailed) {
            return Responses.createFailureResult(TangoUtils.convertDevFailedToException(devFailed));
        }
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/attributes")
    public Object deviceAttributes(@Context final TangoProxy proxy,
                                   @Context UriInfo uriInfo,
                                   @Context ServletContext context) throws Exception {
        final String href = uriInfo.getPath();

        return Collections2.transform(
                Arrays.asList(proxy.toDeviceProxy().get_attribute_info_ex()), new Function<AttributeInfoEx, Object>() {
                    @Override
                    public Object apply(final AttributeInfoEx input) {
                        return attributeInfoExToResponse(input, proxy, href);
                    }
                });
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}")
    public Object deviceAttribute(@PathParam("attr") final String attrName,
                                  @Context UriInfo uriInfo,
                                  @Context TangoProxy proxy,
                                  @Context ServletContext context) throws Exception {
        final String href = uriInfo.getPath();

        return attributeInfoExToResponse(proxy.toDeviceProxy().get_attribute_info_ex(attrName), proxy, href);
    }

    private static Object attributeInfoExToResponse(final AttributeInfoEx input, final TangoProxy proxy, final String href) {
        try {
            return new Object() {
                public String name = input.name;
                public String value = href + "/attributes/" + name + "/value";
                public Object info = input;
                public Object properties = proxy.toDeviceProxy().get_attribute_property(name);
                public Object _links = new Object() {
                    public String _parent = href + "/attributes/" + name;
                    //TODO use LinksProvider
                };
            };
        } catch (DevFailed devFailed) {
            return null; //TODO error object
        }
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/attributes/{attr}/value")
    public Object deviceAttributeValueGet(@PathParam("attr") final String attrName,
                                          @Context TangoProxy proxy) throws Exception {
        final Triplet<Object, Long, Quality> result = proxy.readAttributeValueTimeQuality(attrName);


        return new Object() {
            public String name = attrName;
            public Object value = result.getValue0();
            public String quality = result.getValue2().name();
            public long timestamp = result.getValue1();
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
        if (async)
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
        boolean async = parametersMap.containsKey(ASYNC);//check if it is true?
        if (async) {
            parametersMap.remove(ASYNC);
        }
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
        if (!async) return Iterables.transform(Arrays.asList(proxy.toDeviceProxy().read_attribute(attrNames)), new Function<DeviceAttribute, Object>() {
            @Override
            public Object apply(final DeviceAttribute input) {
                try {
                    return new Object(){
                        public String name = input.getName();
                        public Object value = TangoDataTypes.forTangoDevDataType(input.getType()).extract(TangoDataWrapper.create(input));
                        public String quality = input.getQuality().toString();
                        public long timestamp = input.getTime();
                        public Object _links = new Object(){
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
}
