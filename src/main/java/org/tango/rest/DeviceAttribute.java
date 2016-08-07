package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.AttributeConfig_5;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import org.apache.commons.beanutils.ConvertUtils;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.TangoDataType;
import org.tango.client.ez.data.type.TangoDataTypes;
import org.tango.client.ez.data.type.UnknownTangoDataType;
import org.tango.client.ez.data.type.ValueExtractionException;
import org.tango.client.ez.proxy.*;
import org.tango.rest.entities.AttributeValue;
import org.tango.rest.response.Responses;
import org.tango.web.server.EventHelper;
import org.tango.web.server.providers.Partitionable;
import org.tango.web.server.providers.StaticValue;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/attributes/{attr}")
@Produces("application/json")
public class DeviceAttribute {
    private final String name;
    private final TangoProxy proxy;

    public DeviceAttribute(String name, TangoProxy proxy) {
        this.name = name;
        this.proxy = proxy;
    }

    @GET
    @StaticValue
    public Object get(@PathParam("attr") String attrName, @Context UriInfo uriInfo, @Context TangoProxy proxy, @Context ServletContext context) throws DevFailed{
        final String href = uriInfo.getAbsolutePath().toString();

        return DeviceHelper.attributeInfoExToResponse(proxy.toDeviceProxy().get_attribute_info_ex(attrName).name, href);
    }

    @GET
    @Path("/{event}")
    public Object deviceAttributeEvent(@PathParam("attr") final String attrName,
                                       @PathParam("event") String event,
                                       @QueryParam("timeout") long timeout,
                                       @QueryParam("state") EventHelper.State state,
                                       @Context ServletContext context,
                                       @Context TangoProxy proxy, @Context UriInfo uriInfo) throws InterruptedException, URISyntaxException {
        TangoEvent tangoEvent;
        try {
            tangoEvent = TangoEvent.valueOf(event.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Responses.createFailureResult("Unsupported event: " + event);
        }
        try {
            return EventHelper.handleEvent(attrName, timeout, state, proxy, tangoEvent);
        } catch (NoSuchAttributeException | TangoProxyException e) {
            return Responses.createFailureResult("Failed to subscribe to event " + uriInfo.getAbsolutePath(), e);
        }
    }

    @GET
    @Partitionable
    @Path("/history")
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
                    } catch (UnknownTangoDataType | DevFailed | ValueExtractionException e) {
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
    @Path("/info")
    public AttributeInfo deviceAttributeInfo(@PathParam("attr") String attrName, @Context TangoProxy proxy) throws DevFailed {
        return proxy.toDeviceProxy().get_attribute_info_ex(attrName);
    }

    @PUT
    @Consumes("application/json")
    @Path("/info")
    public AttributeInfo deviceAttributeInfoPut(@PathParam("attr")  String attrName, @QueryParam("async") boolean async, @Context TangoProxy proxy, AttributeConfig_5 config) throws DevFailed {
        proxy.toDeviceProxy().set_attribute_info(new AttributeInfoEx[]{new AttributeInfoEx(config)});
        if (async) return null;
        return proxy.toDeviceProxy().get_attribute_info(attrName);
    }

    @GET
    @Path("/properties")
    public DbAttribute deviceAttributeProperties(@PathParam("attr")  String attrName, @Context TangoProxy proxy) throws DevFailed {
        return proxy.toDeviceProxy().get_attribute_property(attrName);
    }

    @PUT
    @Path("/properties/{prop}")
    public DbAttribute deviceAttributePropertyPut(@PathParam("attr") final String attrName,
                                                  @PathParam("prop") final String propName,
                                                  @QueryParam("value") final String propValue,
                                                  @QueryParam("async") boolean async,
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
    @Path("/properties/{prop}")
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
    @Path("/value")
    public Object deviceAttributeValueGet(@PathParam("attr") final String attrName, @Context TangoProxy proxy) throws Exception {
        final ValueTimeQuality<Object> result = proxy.readAttributeValueTimeQuality(attrName);

        //TODO move to Jackson
        return new Object() {
            public String name = attrName;
            public Object value = result.getValue();
            public String quality = result.getQuality().name();
            public long timestamp = result.getTime();
        };
    }

    @PUT
    @org.tango.web.server.providers.AttributeValue
    @Path("/value")
    public Object deviceAttributeValuePut(@PathParam("attr") String attrName, @QueryParam("v") String value, @QueryParam("async") boolean async,
                                          @Context TangoProxy proxy) throws Exception {
        TangoAttributeInfoWrapper attributeInfo = proxy.getAttributeInfo(attrName);
        Class<?> targetType = attributeInfo.getClazz();
        Object converted = ConvertUtils.convert(value, targetType);

        proxy.writeAttribute(attrName, converted);
        if (!async)
            return deviceAttributeValueGet(attrName, proxy);
        else return null;
    }
}
