package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import fr.esrf.Tango.AttributeConfig;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.TangoDataType;
import org.tango.client.ez.data.type.TangoDataTypes;
import org.tango.client.ez.data.type.UnknownTangoDataType;
import org.tango.client.ez.data.type.ValueExtractionException;
import org.tango.client.ez.proxy.*;
import org.tango.client.ez.util.TangoUtils;
import org.tango.rest.entities.AttributeValue;
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
    @Context
    private UriInfo uriInfo;

    @GET
    @StaticValue
    public Object device(@Context TangoProxy proxy,
                         @Context DatabaseDs db,
                         @Context UriInfo uriInfo,
                         @Context final ServletContext context){
        try {
            final String href = uriInfo.getAbsolutePath().toString();
            return new org.tango.rest.entities.Device(proxy.getName(),
                    DeviceInfos.fromDeviceInfo(db.getDeviceInfo(proxy.getName())),
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
                    Collections2.transform(proxy.toDeviceProxy().getPipeNames(), new Function<String, NamedEntity>() {
                        @Override
                        public NamedEntity apply(String input) {
                            return new NamedEntity(input, href + "/pipes/" + input);
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
        } catch (NoSuchCommandException | TangoProxyException e) {
            return Responses.createFailureResult(e);
        }
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/attributes")
    public Object deviceAttributes(@Context TangoProxy proxy, @Context ServletContext context) throws Exception {
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
    @StaticValue
    @Path("/attributes/{attr}")
    public Object deviceAttribute(@PathParam("attr") String attrName, @Context UriInfo uriInfo, @Context TangoProxy proxy, @Context ServletContext context) throws Exception {
        final String href = uriInfo.getAbsolutePath().toString();

        return DeviceHelper.attributeInfoExToResponse(proxy.toDeviceProxy().get_attribute_info_ex(attrName).name, href);
    }

    @GET
    @Path("/attributes/{attr}/{event}")
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
            return new AttributeValue(attrName, result.argout, result.quality, result.timestamp, uriInfo.getPath(), "TODO");
        } catch (NoSuchAttributeException | TangoProxyException e) {
            return Responses.createFailureResult("Failed to subscribe to event " + uriInfo.getPath(), e);
        }
    }

    @GET
    @Path("/attributes/{attr}/history")
    public Object deviceAttributeHistory(@PathParam("attr") final String attrName,
                                         @Context TangoProxy proxy,
                                         @Context ServletContext context) throws DevFailed {
        return Lists.transform(Arrays.asList(proxy.toDeviceProxy().attribute_history(attrName)), new Function<DeviceDataHistory, Object>() {
            @Override
            public Object apply(DeviceDataHistory input) {
                if (!input.hasFailed()) {
                    try {
                        TangoDataWrapper wrapper = TangoDataWrapper.create(input);

                        TangoDataType<?> type = TangoDataTypes.forTangoDevDataType(input.getType());

                        return new AttributeValue<Object>(input.getName(), type.extract(wrapper), "VALID", input.getTime(), null, null);
                    } catch (UnknownTangoDataType | DevFailed |ValueExtractionException e) {
                        return Responses.createFailureResult(e);
                    }
                } else {
                    return Responses.createFailureResult(new DevFailed(input.getErrStack()));
                }
            }
        });
    }

    @GET
    @StaticValue
    @Path("/attributes/{attr}/info")
    public AttributeInfo deviceAttributeInfo(@PathParam("attr") String attrName, @Context TangoProxy proxy) throws DevFailed {
        return proxy.toDeviceProxy().get_attribute_info(attrName);
    }

    @PUT
    @Consumes("application/json")
    @Path("/attributes/{attr}/info")
    public AttributeInfo deviceAttributeInfoPut(@PathParam("attr")  String attrName, @QueryParam(ASYNC) boolean async, @Context TangoProxy proxy, AttributeConfig config) throws DevFailed {
        proxy.toDeviceProxy().set_attribute_info(new AttributeInfo[]{new AttributeInfo(config)});
        if (async) return null;
        return proxy.toDeviceProxy().get_attribute_info(attrName);
    }

    @Override
    public DbAttribute deviceAttributeProperties(@PathParam("attr")  String attrName, @Context TangoProxy proxy) throws DevFailed {
        return super.deviceAttributeProperties(attrName, proxy);
    }

    @Override
    public void deviceAttributePropertyDelete(@PathParam("attr")  String attrName, String propName, @Context TangoProxy proxy) throws DevFailed {
        super.deviceAttributePropertyDelete(attrName, propName, proxy);
    }

    @Override
    public DbAttribute deviceAttributePropertyPut(@PathParam("attr")  String attrName, String propName, String propValue, boolean async, @Context TangoProxy proxy) throws DevFailed {
        return super.deviceAttributePropertyPut(attrName, propName, propValue, async, proxy);
    }


    @Override
    public Object deviceAttributesPut(@Context TangoProxy proxy, @Context UriInfo uriInfo, @Context ServletContext context, @Context HttpServletRequest request) throws DevFailed {
        return super.deviceAttributesPut(proxy, uriInfo, context, request);
    }

    @Override
    public Object deviceAttributeValueGet(@PathParam("attr")  String attrName, @Context TangoProxy proxy) throws Exception {
        return super.deviceAttributeValueGet(attrName, proxy);
    }

    @Override
    public Object deviceAttributeValuePut(@PathParam("attr") String attrName, @QueryParam(ASYNC) String value, boolean async, @Context TangoProxy proxy) throws Exception {
        return super.deviceAttributeValuePut(attrName, value, async, proxy);
    }

    @Override
    public Object deviceCommand(String cmdName, @Context TangoProxy proxy, @Context UriInfo uriInfo) throws DevFailed {
        return super.deviceCommand(cmdName, proxy, uriInfo);
    }

    @Override
    public Object deviceCommandHistory(String cmdName, @Context TangoProxy proxy, @Context UriInfo uriInfo) throws DevFailed {
        return super.deviceCommandHistory(cmdName, proxy, uriInfo);
    }

    @Override
    public Object deviceCommandPut(String cmdName, String[] value, boolean async, @Context TangoProxy proxy, @Context UriInfo uriInfo) throws Exception {
        return super.deviceCommandPut(cmdName, value, async, proxy, uriInfo);
    }

    @Override
    public Object deviceCommands(@Context TangoProxy proxy, @Context UriInfo uriInfo) throws DevFailed {
        return super.deviceCommands(proxy, uriInfo);
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

    @Override
    public Object deviceState(@Context TangoProxy proxy, @Context ServletContext context) {
        return super.deviceState(proxy, context);
    }
}
