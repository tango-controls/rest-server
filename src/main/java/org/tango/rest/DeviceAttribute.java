package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.AttrQuality;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import org.apache.commons.beanutils.ConvertUtils;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.*;
import org.tango.client.ez.proxy.*;
import org.tango.rest.entities.AttributeValue;
import org.tango.rest.entities.Failures;
import org.tango.web.server.EventHelper;
import org.tango.web.server.attribute.AttributeConfig;
import org.tango.web.server.providers.Partitionable;
import org.tango.web.server.providers.StaticValue;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
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
    public Object get(@Context UriInfo uriInfo) throws DevFailed{
        final String href = uriInfo.getAbsolutePath().toString();

        return DeviceHelper.attributeInfoExToResponse(name, href);
    }

    @GET
    @Path("/{event}")
    public void deviceAttributeEvent(@PathParam("event") TangoEvent event,
                                     @QueryParam("timeout") long timeout,
                                     @QueryParam("state") EventHelper.State state,
                                     @Context ServletContext context,
                                     @Context TangoProxy proxy, @Context UriInfo uriInfo,
                                     final @Suspended AsyncResponse asyncResponse
    ) throws InterruptedException, URISyntaxException {
        try {
            proxy.subscribeToEvent(name, event);
            proxy.addEventListener(name, event, new TangoEventListener<Object>() {
                @Override
                public void onEvent(EventData<Object> data) {
                    asyncResponse.resume(new AttributeValue<Object>(name, data.getValue(), AttrQuality.ATTR_VALID.toString(), data.getTime()));
                }

                @Override
                public void onError(Exception cause) {
                    asyncResponse.resume(Failures.createInstance(cause));
                }
            });
//            return EventHelper.handleEvent(name, timeout, state, proxy, tangoEvent);
        } catch (NoSuchAttributeException | TangoProxyException e) {
//            return Responses.createFailureResult("Failed to subscribe to event " + uriInfo.getPath(), e);
        }
    }

    @GET
    @Partitionable
    @Path("/history")
    public Object deviceAttributeHistory(@Context TangoProxy proxy,
                                         @Context ServletContext context) throws DevFailed {
        return Lists.transform(Arrays.asList(proxy.toDeviceProxy().attribute_history(name)), new Function<DeviceDataHistory, Object>() {
            @Override
            public Object apply(DeviceDataHistory input) {
                if (!input.hasFailed()) {
                    try {
                        TangoDataWrapper wrapper = TangoDataWrapper.create(input);

                        TangoDataType<?> type = TangoDataTypes.forTangoDevDataType(input.getType());

                        return new AttributeValue<Object>(input.getName(), type.extract(wrapper), AttrQuality.ATTR_VALID.toString(), input.getTime());
                    } catch (UnknownTangoDataType | DevFailed | ValueExtractionException e) {
                        return Failures.createInstance(e);
                    }
                } else {
                    return Failures.createInstance(new DevFailed(input.getErrStack()));
                }
            }
        });
    }

    @GET
    @StaticValue
    @Path("/info")
    public AttributeInfo deviceAttributeInfo(@Context TangoProxy proxy) throws DevFailed {
        return proxy.toDeviceProxy().get_attribute_info_ex(name);
    }

    @PUT
    @Consumes("application/json")
    @Path("/info")
    public AttributeInfo deviceAttributeInfoPut(@QueryParam("async") boolean async, @Context TangoProxy proxy, AttributeConfig config) throws DevFailed {
        proxy.toDeviceProxy().set_attribute_info(new AttributeInfoEx[]{new AttributeInfoEx(config.wrapped)});
        if (async) return null;
        return proxy.toDeviceProxy().get_attribute_info(name);
    }

    @GET
    @Path("/properties")
    public DbAttribute deviceAttributeProperties(@Context TangoProxy proxy) throws DevFailed {
        return proxy.toDeviceProxy().get_attribute_property(name);
    }

    @PUT
    @Path("/properties/{prop}")
    public DbAttribute deviceAttributePropertyPut(@PathParam("prop") final String propName,
                                                  @QueryParam("value") final String propValue,
                                                  @QueryParam("async") boolean async,
                                                  @Context TangoProxy proxy) throws DevFailed {
        DbAttribute dbAttribute = proxy.toDeviceProxy().get_attribute_property(name);

        DbDatum datum = dbAttribute.datum(propName);

        if(datum != null) datum.insert(propValue);
        else dbAttribute.add(propName, propValue);

        proxy.toDeviceProxy().put_attribute_property(dbAttribute);

        if(async) return null;
        return dbAttribute;
    }

    @DELETE
    @Path("/properties/{prop}")
    public void deviceAttributePropertyDelete(@PathParam("prop") final String propName,
                                              @Context TangoProxy proxy) throws DevFailed {
        DbAttribute dbAttribute = proxy.toDeviceProxy().get_attribute_property(name);

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
    public Object deviceAttributeValueGet(@Context TangoProxy proxy) throws Exception {
        final ValueTimeQuality<Object> result = proxy.readAttributeValueTimeQuality(name);

        return new AttributeValue<Object>(DeviceAttribute.this.name, result.value, result.quality.toString(), result.time);
    }

    @PUT
    @org.tango.web.server.providers.AttributeValue
    @Path("/value")
    public Object deviceAttributeValuePut(@QueryParam("v") String value, @QueryParam("async") boolean async,
                                          @Context TangoProxy proxy) throws Exception {

        TangoAttributeInfoWrapper attributeInfo = proxy.getAttributeInfo(name);
        Class<?> targetType = attributeInfo.getClazz();
        Object converted = ConvertUtils.convert(value, targetType);

        proxy.writeAttribute(name, converted);
        if (async) {
            return null;
        }
        return deviceAttributeValueGet(proxy);
    }

    @GET
    @org.tango.web.server.providers.AttributeValue
    @Path("/value/plain")
    public Object deviceAttributeGetValuePlain(@Context TangoProxy proxy) throws Exception {
        return proxy.readAttribute(name);
    }

    @GET
    @org.tango.web.server.providers.AttributeValue
    @Path("/value/image")
    public Object deviceAttributeGetValueImage(@Context TangoProxy proxy) throws Exception {
        //TODO may throw ClassCast in case non image attribute is requested
        final TangoImage image =  proxy.readAttribute(name);
        return new ImageAttributeValue(image);
    }

    public class ImageAttributeValue {
        public TangoImage value;

        public ImageAttributeValue(TangoImage image) {
            this.value = image;
        }
    }
}
