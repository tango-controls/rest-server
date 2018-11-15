package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DbDatum;
import org.apache.commons.beanutils.ConvertUtils;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.TangoDataType;
import org.tango.client.ez.data.type.ValueInsertionException;
import org.tango.client.ez.proxy.NoSuchAttributeException;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.client.ez.util.TangoUtils;
import org.tango.rest.entities.DeviceState;
import org.tango.rest.entities.Failures;
import org.tango.rest.entities.NamedEntity;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.StaticValue;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/6/16
 */
@Path("/{domain}/{family}/{member}")
@Produces("application/json")
public class Device {
    @GET
    @StaticValue
    public Object get(@PathParam("domain") String domain,
                         @PathParam("family") String family,
                         @PathParam("member") String member,
                         @Context DatabaseDs db,
                         @Context UriInfo uriInfo){
        try {
            final String devname = domain + "/" + family + "/" + member;
            return DeviceHelper.deviceToResponse(devname, db.getFullTangoHost(), db.getDeviceInfo(devname), uriInfo.getAbsolutePath());
        } catch (TangoProxyException e) {
            return Failures.createInstance(e);
        }
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/attributes")
    public Object deviceAttributes(@Context TangoProxy proxy, @Context ServletContext context, @Context UriInfo uriInfo) throws Exception {
        final String href = uriInfo.getAbsolutePath().toString();

        return Lists.transform(
                Arrays.asList(proxy.toDeviceProxy().get_attribute_info_ex()), new Function<AttributeInfoEx, Object>() {
                    @Override
                    public Object apply(final AttributeInfoEx input) {
                        return DeviceHelper.attributeInfoExToResponse(input.name, href);
                    }
                });
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/attributes/info")
    public fr.esrf.TangoApi.AttributeInfoEx[] deviceAttributeInfos(@QueryParam("attr") String[] attrs,
                                                                    @Context TangoProxy proxy,
                                                                    @Context ServletContext context,
                                                                    @Context UriInfo uriInfo) throws DevFailed {
        return proxy.toDeviceProxy().get_attribute_info_ex(attrs);
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/attributes/value")
    public fr.esrf.TangoApi.DeviceAttribute[] deviceAttributeValues(@QueryParam("attr") String[] attrs,
                                        @Context TangoProxy proxy,
                                        @Context ServletContext context,
                                        @Context UriInfo uriInfo) throws DevFailed {
        return proxy.toDeviceProxy().read_attribute(attrs);
    }

    @PUT
    @Partitionable
    @DynamicValue
    @Path("/attributes/value")
    public fr.esrf.TangoApi.DeviceAttribute[] deviceAttributeValuesPut(@Context final TangoProxy proxy,
                                                                        @Context ServletContext context,
                                                                        @Context UriInfo uriInfo) throws Exception {
        boolean async = uriInfo.getQueryParameters().containsKey("async");
        //TODO split into good and bad attributes: write good ones; report bad ones (if present)
        fr.esrf.TangoApi.DeviceAttribute[] attrs =
                Iterables.toArray(
                        Iterables.filter(
                                Iterables.transform(
                                        uriInfo.getQueryParameters().entrySet(), new Function<Map.Entry<String, List<String>>, fr.esrf.TangoApi.DeviceAttribute>() {
                                            @Override
                                            public fr.esrf.TangoApi.DeviceAttribute apply(Map.Entry<String, List<String>> stringListEntry) {
                                                String attrName = stringListEntry.getKey();
                                                String[] value = stringListEntry.getValue().toArray(new String[stringListEntry.getValue().size()]);
                                                fr.esrf.TangoApi.DeviceAttribute result;

                                                try {
                                                    result = new fr.esrf.TangoApi.DeviceAttribute(attrName);
                                                    TangoDataType<Object> dataType = (TangoDataType<Object>) proxy.getAttributeInfo(attrName).getType();
                                                    Class<?> type = dataType.getDataTypeClass();
                                                    Object converted = ConvertUtils.convert(value.length == 1 ? value[0] : value, type);

                                                    dataType.insert(TangoDataWrapper.create(result, null), converted);

                                                    return result;
                                                } catch (TangoProxyException | NoSuchAttributeException | ValueInsertionException e) {
                                                    return null;
                                                }
                                            }
                                        }), new Predicate<fr.esrf.TangoApi.DeviceAttribute>() {

                                    @Override
                                    public boolean apply(@Nullable fr.esrf.TangoApi.DeviceAttribute input) {
                                        return input != null;
                                    }
                                }

                        ), fr.esrf.TangoApi.DeviceAttribute.class);




        if(async) {
            proxy.toDeviceProxy().write_attribute_asynch(attrs);
            return null;
        } else {
            String[] readNames = Lists.transform(Arrays.asList(attrs), new Function<fr.esrf.TangoApi.DeviceAttribute, String>() {
                @Override
                public String apply(fr.esrf.TangoApi.DeviceAttribute deviceAttribute) {
                    try {
                        return deviceAttribute.getName();
                    } catch (DevFailed devFailed) {
                        throw new AssertionError("Must not happen!", TangoUtils.convertDevFailedToException(devFailed));
                    }
                }
            }).toArray(new String[attrs.length]);
            proxy.toDeviceProxy().write_attribute(attrs);
            return proxy.toDeviceProxy().read_attribute(readNames);
        }
    }

    @Path("/attributes/{attr}")
    public DeviceAttribute deviceAttribute(@PathParam("attr") String attrName, @Context TangoProxy proxy) throws Exception {
        return new DeviceAttribute(attrName, proxy);
    }

    @GET
    @DynamicValue
    @Path("/state")
    public Object deviceState(@Context TangoProxy proxy, @Context ServletContext context, @Context UriInfo uriInfo) {
        try {
            final String href = uriInfo.getAbsolutePath().resolve("..").toString();
            final fr.esrf.TangoApi.DeviceAttribute[] ss = proxy.toDeviceProxy().read_attribute(new String[]{"State", "Status"});
            DeviceState result = new DeviceState(ss[0].extractDevState().toString(), ss[1].extractString());

            return result;
        } catch (DevFailed devFailed) {
            return new DeviceState(DevState.UNKNOWN.toString(), String.format("Failed to read state&status from %s", proxy.getName()));
        }
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/commands")
    public Object deviceCommands(@Context TangoProxy proxy,
                                 @Context UriInfo uriInfo) throws DevFailed {
        final String href = uriInfo.getAbsolutePath().toString();
        return Lists.transform(Arrays.asList(proxy.toDeviceProxy().command_list_query()), new Function<CommandInfo, Object>() {
            @Override
            public Object apply(final CommandInfo input) {
                return DeviceHelper.commandInfoToResponse(input, href);
            }
        });
    }


    @Path("/commands/{cmd}")
    public DeviceCommand deviceCommand(@PathParam("cmd") String cmdName, @Context TangoProxy proxy) {
        return new DeviceCommand(proxy, cmdName);
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/properties")
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

    @POST
    @DynamicValue
    @Path("/properties")
    public Object devicePropertiesPost(@Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        return devicePropertiesPut(request, proxy);
    }

    @PUT
    @DynamicValue
    @Path("/properties")
    public Object devicePropertiesPut(@Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        Map<String, String[]> parametersMap = new HashMap<>(request.getParameterMap());
        boolean async = parametersMap.remove("async") != null;

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

    @Path("/properties/{prop}")
    public DeviceProperty deviceProperty(@PathParam("prop") String propName, @Context UriInfo uriInfo) {
        return new DeviceProperty(propName, uriInfo);
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/pipes")
    public Object devicePipes(@Context UriInfo uriInfo, @Context TangoProxy proxy) throws DevFailed {
        final URI href = uriInfo.getAbsolutePath();
        return Lists.transform(proxy.toDeviceProxy().getPipeNames(), new Function<String, Object>() {
            @Override
            public Object apply(final String input) {
                return new NamedEntity(input, href + "/" + input);
            }
        });
    }

    @Path("/pipes/{pipe}")
    public DevicePipe getPipe(@PathParam("pipe") String name, @Context TangoProxy proxy){
        return new DevicePipe(proxy, name);
    }
}
