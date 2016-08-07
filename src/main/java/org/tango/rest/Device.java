package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DbDatum;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.entities.DeviceState;
import org.tango.rest.rc2.Rc2ApiImpl;
import org.tango.rest.response.Responses;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.providers.Partitionable;
import org.tango.web.server.providers.StaticValue;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/6/16
 */
@Path("/{domain}/{family}/{member}")
@Produces("application/json")
public class Device extends Rc2ApiImpl {
    @GET
    @StaticValue
    public Object get(@PathParam("domain") String domain,
                         @PathParam("family") String family,
                         @PathParam("member") String member,
                         @Context DatabaseDs db,
                         @Context UriInfo uriInfo){
        try {
            final String href = uriInfo.getAbsolutePath().toString();
            final String devname = domain + "/" + family + "/" + member;
            return DeviceHelper.deviceToResponse(devname, db.getDeviceInfo(devname), href);
        } catch (NoSuchCommandException | TangoProxyException e) {
            return Responses.createFailureResult(e);
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

    @PUT
    @Path("/attributes")
    public Object deviceAttributesPut(@QueryParam("attr") String[] attr, @Context TangoProxy proxy, @Context UriInfo uriInfo, @Context ServletContext context, @Context HttpServletRequest request) throws DevFailed {
        return super.deviceAttributesPut(proxy, uriInfo, context, request);
    }

    @Path("/attributes/{attr}")
    public DeviceAttribute deviceAttribute(@PathParam("attr") String attrName, @Context TangoProxy proxy) throws Exception {
        return new DeviceAttribute(attrName, proxy);
    }

    @GET
    @org.tango.web.server.providers.AttributeValue
    @Path("/state")
    public Object deviceState(@Context TangoProxy proxy, @Context ServletContext context, @Context UriInfo uriInfo) {
        try {
            final String href = uriInfo.getAbsolutePath().resolve("..").toString();
            final fr.esrf.TangoApi.DeviceAttribute[] ss = proxy.toDeviceProxy().read_attribute(new String[]{"State", "Status"});
            DeviceState result = new DeviceState(ss[0].extractDevState().toString(), ss[1].extractString(), new Object() {
                public String _state = href + "attributes/State";
                public String _status = href + "attributes/Status";
                public String _parent = href;
                public String _self = href + "state";
            });

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
    @org.tango.web.server.providers.AttributeValue
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
    @org.tango.web.server.providers.AttributeValue
    @Path("/properties")
    public Object devicePropertiesPost(@Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        return devicePropertiesPut(request, proxy);
    }

    @PUT
    @org.tango.web.server.providers.AttributeValue
    @Path("/properties")
    public Object devicePropertiesPut(@Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
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

    @Path("/properties/{prop}")
    public DeviceProperty deviceProperty(@PathParam("prop") String propName) {
        return new DeviceProperty(propName);
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/pipes")
    public Object devicePipes(@Context UriInfo uriInfo, @Context TangoProxy proxy) throws DevFailed {
        final String href = uriInfo.getAbsolutePath().toString();
        return Lists.transform(proxy.toDeviceProxy().getPipeNames(), new Function<String, Object>() {
            @Override
            public Object apply(final String input) {
                return new Object() {
                    public String name = input;
                    public String value = href + name + "/value";
                    public Object _links = new Object() {
                        public String _self = href + name;
                    };
                };
            }


        });
    }

    @Path("/pipes/{pipe}")
    public DevicePipe getPipe(@PathParam("pipe") String name, @Context TangoProxy proxy){
        return new DevicePipe(proxy, name);
    }
}
