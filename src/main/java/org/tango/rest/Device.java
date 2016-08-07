package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import fr.esrf.Tango.AttributeConfig;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.*;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.TangoDataType;
import org.tango.client.ez.data.type.TangoDataTypes;
import org.tango.client.ez.data.type.UnknownTangoDataType;
import org.tango.client.ez.data.type.ValueExtractionException;
import org.tango.client.ez.proxy.*;
import org.tango.client.ez.util.TangoUtils;
import org.tango.rest.entities.AttributeValue;
import org.tango.rest.entities.DeviceState;
import org.tango.rest.entities.NamedEntity;
import org.tango.rest.rc2.Rc2ApiImpl;
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
import java.util.Arrays;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/6/16
 */
@Path("/{domain}/{family}/{member}")
@Produces("application/json")
public class Device extends Rc2ApiImpl {
    @GET
    @StaticValue
    public Object device(@PathParam("domain") String domain,
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
    public DeviceAttribute deviceAttribute() throws Exception {
        return new DeviceAttribute();
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
    public DeviceCommand deviceCommand() {
        return new DeviceCommand();
    }

    @Override
    public Object devicePipeGet(String pipeName, @Context UriInfo uriInfo, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePipeGet(pipeName, uriInfo, proxy);
    }

    @Override
    public Object devicePipePut(String pipeName, boolean async, @Context UriInfo info, @Context TangoProxy proxy, PipeBlob blob) throws DevFailed {
        return super.devicePipePut(pipeName, async, info, proxy, blob);
    }

    @Override
    public Object devicePipes(@Context UriInfo uriInfo, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePipes(uriInfo, proxy);
    }

    @Override
    public Object deviceProperties(@Context TangoProxy proxy) throws DevFailed {
        return super.deviceProperties(proxy);
    }

    @Override
    public Object devicePropertiesPost(@Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePropertiesPost(request, proxy);
    }

    @Override
    public Object devicePropertiesPut(@Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePropertiesPut(request, proxy);
    }

    @Override
    public Object deviceProperty(String propName, @Context TangoProxy proxy) throws DevFailed {
        return super.deviceProperty(propName, proxy);
    }

    @Override
    public void devicePropertyDelete(String propName, @Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        super.devicePropertyDelete(propName, request, proxy);
    }

    @Override
    public Object devicePropertyPost(String propName, @Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePropertyPost(propName, request, proxy);
    }

    @Override
    public Object devicePropertyPut(String propName, @Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePropertyPut(propName, request, proxy);
    }


}
