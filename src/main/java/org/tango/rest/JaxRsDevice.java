package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DbDatum;
import fr.soleil.tango.clientapi.TangoAttribute;
import org.apache.commons.beanutils.ConvertUtils;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.TangoDataType;
import org.tango.client.ez.data.type.ValueInsertionException;
import org.tango.client.ez.proxy.NoSuchAttributeException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.client.ez.util.TangoUtils;
import org.tango.rest.entities.*;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.RequiresTangoAttribute;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.proxy.Proxies;
import org.tango.web.server.proxy.TangoAttributeProxyImpl;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.proxy.TangoDeviceProxy;
import org.tango.web.server.response.TangoRestAttribute;
import org.tango.web.server.response.TangoRestCommand;
import org.tango.web.server.response.TangoRestDevice;
import org.tango.web.server.util.TangoRestEntityUtils;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/6/16
 */
@Path("/{domain}/{family}/{member}")
@Produces("application/json")
public class JaxRsDevice {
    @Context private TangoDatabaseProxy tangoDatabase;
    @Context private TangoDeviceProxy tangoDevice;

    @GET
    @StaticValue
    public Device get(@Context UriInfo uriInfo) throws DevFailed{
        return new TangoRestDevice(
                tangoDatabase.getTangoHost() + "/" + tangoDevice.getName(),
                tangoDevice.getName(),
                tangoDatabase.getTangoHost(),
                tangoDatabase.asEsrfDatabase().get_device_info(tangoDevice.getName()),
                uriInfo.getAbsolutePath());
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/attributes")
    public List<Attribute> deviceAttributes(@Context UriInfo uriInfo) throws DevFailed {
        return Arrays.stream(tangoDevice.getProxy().toDeviceProxy().get_attribute_info_ex(tangoDevice.getProxy().toDeviceProxy().get_attribute_list()))
                .map(infoEx -> new TangoRestAttribute(
                        tangoDatabase.getTangoHost(),
                        tangoDevice.getName(),
                        infoEx.name,
                        infoEx,
                        uriInfo.getAbsolutePathBuilder().path(infoEx.name).build() ,
                        null))
                .collect(Collectors.toList());
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/attributes/info")
    public fr.esrf.TangoApi.AttributeInfoEx[] deviceAttributeInfos(@QueryParam("attr") String[] attrs) throws DevFailed {
        return tangoDevice.getProxy().toDeviceProxy().get_attribute_info_ex(attrs);
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/attributes/value")
    public fr.esrf.TangoApi.DeviceAttribute[] deviceAttributeValues(@QueryParam("attr") String[] attrs) throws DevFailed {
        return tangoDevice.getProxy().toDeviceProxy().read_attribute(attrs);
    }

    @PUT
    @Partitionable
    @DynamicValue
    @Path("/attributes/value")
    public fr.esrf.TangoApi.DeviceAttribute[] deviceAttributeValuesPut(@Context ServletContext context,
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
                                                    TangoDataType<Object> dataType = (TangoDataType<Object>) tangoDevice.getProxy().getAttributeInfo(attrName).getType();
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
            tangoDevice.getProxy().toDeviceProxy().write_attribute_asynch(attrs);
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
            tangoDevice.getProxy().toDeviceProxy().write_attribute(attrs);
            return tangoDevice.getProxy().toDeviceProxy().read_attribute(readNames);
        }
    }

    ;

    @Path("/attributes/{attr}")
    @RequiresTangoAttribute
    public JaxRsDeviceAttribute deviceAttribute(@Context
                                                            ResourceContext rc, @PathParam("attr") String attrName, @Context TangoAttribute tangoAttribute) throws Exception {
        return rc.getResource(JaxRsDeviceAttribute.class);
    }

    @GET
    @DynamicValue
    @Path("/state")
    public Object deviceState(@Context ServletContext context, @Context UriInfo uriInfo) {
        try {
            final fr.esrf.TangoApi.DeviceAttribute[] ss = tangoDevice.getProxy().toDeviceProxy().read_attribute(new String[]{"State", "Status"});
            DeviceState result = new DeviceState(ss[0].extractDevState().toString(), ss[1].extractString());

            return result;
        } catch (DevFailed devFailed) {
            return new DeviceState(DevState.UNKNOWN.toString(), String.format("Failed to read state&status from %s", tangoDevice.getName()));
        }
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/commands")
    public List<Command> deviceCommands(@Context UriInfo uriInfo) throws DevFailed {
        return Arrays.stream(tangoDevice.getProxy().toDeviceProxy().command_list_query())
                .map(commandInfo -> new TangoRestCommand(
                        commandInfo.cmd_name,
                        tangoDatabase.getTangoHost(),
                        tangoDevice.getName(),
                        commandInfo,
                        uriInfo.getAbsolutePathBuilder().path(commandInfo.cmd_name).build(),
                        null))
                .collect(Collectors.toList());
    }


    @Path("/commands/{cmd}")
    public JaxRsTangoCommand deviceCommand(@Context ResourceContext rc) {
        return rc.getResource(JaxRsTangoCommand.class);
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/properties")
    public Object deviceProperties() throws DevFailed {
        String[] propnames = tangoDevice.getProxy().toDeviceProxy().get_property_list("*");
        if(propnames.length == 0) return propnames;
        return Iterables.transform(
                Arrays.asList(tangoDevice.getProxy().toDeviceProxy().get_property(propnames)),
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
    public Object devicePropertiesPost(@Context HttpServletRequest request) throws DevFailed {
        return devicePropertiesPut(request);
    }

    @PUT
    @DynamicValue
    @Path("/properties")
    public Object devicePropertiesPut(@Context HttpServletRequest request) throws DevFailed {
        Map<String, String[]> parametersMap = new HashMap<>(request.getParameterMap());
        boolean async = parametersMap.remove("async") != null;

        DbDatum[] input = Iterables.toArray(Iterables.transform(parametersMap.entrySet(), new Function<Map.Entry<String, String[]>, DbDatum>() {
            @Override
            public DbDatum apply(Map.Entry<String, String[]> input) {
                return new DbDatum(input.getKey(), input.getValue());
            }
        }), DbDatum.class);

        tangoDevice.getProxy().toDeviceProxy().put_property(input);

        if (async)
            return null;
        else return deviceProperties();
    }

    @Path("/properties/{prop}")
    public DeviceProperty deviceProperty(@PathParam("prop") String propName, @Context UriInfo uriInfo) {
        return new DeviceProperty(propName, uriInfo);
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/pipes")
    public Object devicePipes(@Context UriInfo uriInfo) throws DevFailed {
        final URI href = uriInfo.getAbsolutePath();
        return Lists.transform(tangoDevice.getProxy().toDeviceProxy().getPipeNames(), new Function<String, Object>() {
            @Override
            public Object apply(final String input) {
                return new NamedEntity(input, href + "/" + input);
            }
        });
    }

    @Path("/pipes/{pipe}")
    public JaxRsTangoPipe getPipe(@Context ResourceContext rc){
        return rc.getResource(JaxRsTangoPipe.class);
    }
}
