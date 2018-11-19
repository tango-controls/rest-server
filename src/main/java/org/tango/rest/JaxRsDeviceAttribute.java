package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import fr.esrf.Tango.AttrQuality;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.DbAttribute;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceDataHistory;
import org.apache.commons.beanutils.ConvertUtils;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.*;
import org.tango.client.ez.proxy.NoSuchAttributeException;
import org.tango.client.ez.proxy.ReadAttributeException;
import org.tango.client.ez.proxy.TangoAttributeInfoWrapper;
import org.tango.client.ez.proxy.TangoEvent;
import org.tango.rest.entities.AttributeValue;
import org.tango.rest.entities.Failures;
import org.tango.web.server.attribute.AttributeConfig;
import org.tango.web.server.attribute.AttributeProperty;
import org.tango.web.server.attribute.EventBuffer;
import org.tango.web.server.binding.*;
import org.tango.web.server.proxy.TangoAttributeProxy;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.proxy.TangoDeviceProxy;
import org.tango.web.server.response.TangoRestAttribute;
import org.tango.web.server.util.TangoRestEntityUtils;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.CompletableFuture;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/attributes/{attr}")
@Produces("application/json")
@RequiresTangoAttribute
public class JaxRsDeviceAttribute {
    @PathParam("attr") public  String name;
    @Context public TangoDatabaseProxy databaseProxy;
    @Context public TangoDeviceProxy deviceProxy;
    @Context public TangoAttributeProxy tangoAttribute;

    @GET
    @RequiresTangoAttribute
    @StaticValue
    public TangoRestAttribute get(@Context UriInfo uriInfo) {
        return TangoRestEntityUtils.fromTangoAttribute(tangoAttribute, uriInfo);
    }

    @GET
    @Path("/{event:change|periodic|archive|user}")
    public Object deviceAttributeEvent(@PathParam("event") String eventAsString,
                                       @DefaultValue("3000") @QueryParam("timeout") long timeout,
                                       @DefaultValue("0") @QueryParam("last") long last,
                                       @Context ServletContext context,
                                       @Context UriInfo uriInfo
    ) throws Exception {
        TangoEvent event = null;
        try {
            event = TangoEvent.valueOf(eventAsString.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AssertionError("Can not happen! event must be one of change|periodic|archive|user but was " + eventAsString);
        }
        EventBuffer.EventKey eventKey = new EventBuffer.EventKey(deviceProxy.getProxy(), name, event);

        EventBuffer buffer = (EventBuffer) context.getAttribute(EventBuffer.class.getName());

        //subscribe this buffer if not yet done
        buffer.subscribe(eventKey, deviceProxy.getProxy());

        if (last > 0) {
            NavigableSet<?> result = buffer.getTail(eventKey, last);
            if (!result.isEmpty()) {
                return result.toArray(new Object[result.size()]);
            }
        }

        return buffer.createEvent(eventKey, deviceProxy.getProxy()).get(timeout);
    }

    @GET
    @Partitionable
    @Path("/history")
    public Object deviceAttributeHistory(@Context ServletContext context) throws DevFailed {
        return Lists.transform(Arrays.asList(deviceProxy.getProxy().toDeviceProxy().attribute_history(name)), new Function<DeviceDataHistory, Object>() {
            @Override
            public Object apply(DeviceDataHistory input) {
                if (!input.hasFailed()) {
                    try {
                        TangoDataWrapper wrapper = TangoDataWrapper.create(input);

                        TangoDataType<?> type = TangoDataTypes.forTangoDevDataType(input.getType());

                        return new AttributeValue<Object>(input.getName(), databaseProxy.getTangoHost(), deviceProxy.getName(), type.extract(wrapper), AttrQuality.ATTR_VALID.toString(), input.getTime());
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
    public AttributeInfoEx deviceAttributeInfo() throws DevFailed {
        return deviceProxy.getProxy().toDeviceProxy().get_attribute_info_ex(name);
    }

    @PUT
    @Consumes("application/json")
    @Path("/info")
    public AttributeInfoEx deviceAttributeInfoPut(@QueryParam("async") boolean async, AttributeConfig config) throws DevFailed {
        deviceProxy.getProxy().toDeviceProxy().set_attribute_info(new AttributeInfoEx[]{new AttributeInfoEx(config.wrapped)});
        if (async) return null;
        return deviceProxy.getProxy().toDeviceProxy().get_attribute_info_ex(name);
    }

    @GET
    @StaticValue
    @Path("/properties")
    public List<AttributeProperty> deviceAttributeProperties() throws DevFailed {
        return Lists.transform(deviceProxy.getProxy().toDeviceProxy().get_attribute_property(name), new Function<DbDatum, AttributeProperty>() {
            @Override
            public AttributeProperty apply(DbDatum input) {
                return new AttributeProperty(input);
            }
        });
    }

    @GET
    @StaticValue
    @Path("/properties/{prop}")
    public AttributeProperty deviceAttributeProperties(@PathParam("prop") final String property) throws DevFailed {
        Iterable<? extends DbDatum> datumCollection = Iterables.filter(deviceProxy.getProxy().toDeviceProxy().get_attribute_property(name), new Predicate<DbDatum>() {
            @Override
            public boolean apply(DbDatum input) {
                return property.equalsIgnoreCase(input.name);
            }
        });
        if (!datumCollection.iterator().hasNext()) throw new NotFoundException(
                String.format("Device attribute [%s/%s/%s] has no property[%s]", databaseProxy.getTangoHost(),deviceProxy.getName(), name, property));
        return new AttributeProperty(datumCollection.iterator().next());
    }

    @PUT
    @Path("/properties/{prop}")
    public DbAttribute deviceAttributePropertyPut(@PathParam("prop") final String propName,
                                                  @QueryParam("value") final String propValue,
                                                  @QueryParam("async") boolean async) throws DevFailed {
        DbAttribute dbAttribute = deviceProxy.getProxy().toDeviceProxy().get_attribute_property(name);

        DbDatum datum = dbAttribute.datum(propName);

        if(datum != null) datum.insert(propValue);
        else dbAttribute.add(propName, propValue);

        deviceProxy.getProxy().toDeviceProxy().put_attribute_property(dbAttribute);

        if(async) return null;
        return dbAttribute;
    }

    @DELETE
    @Path("/properties/{prop}")
    public void deviceAttributePropertyDelete(@PathParam("prop") final String propName) throws DevFailed {
        DbAttribute dbAttribute = deviceProxy.getProxy().toDeviceProxy().get_attribute_property(name);

        for (Iterator<DbDatum> iterator = dbAttribute.iterator(); iterator.hasNext(); ) {
            DbDatum dbDatum = iterator.next();
            if (dbDatum.name.equalsIgnoreCase(propName)) {
                iterator.remove();
                break;
            }
        }
        deviceProxy.getProxy().toDeviceProxy().put_attribute_property(dbAttribute);
    }

    @GET
    @DynamicValue
    @Path("/value")
    public AttributeValue<Object> deviceAttributeValueGet() throws DevFailed, NoSuchAttributeException, ReadAttributeException {
        Object result;
        if(tangoAttribute.isImage())
            result = deviceProxy.getProxy().readAttribute(name);
        else
            result = tangoAttribute.read();
        return new AttributeValue<Object>(
                JaxRsDeviceAttribute.this.name,
                databaseProxy.getTangoHost() ,
                deviceProxy.getName(), result,
                tangoAttribute.getQuality().toString(),
                tangoAttribute.getTimestamp());
    }

    @PUT
    @Path("/value")
    public Object deviceAttributeValuePut(@QueryParam("v") String value, @QueryParam("async") boolean async) throws Exception {
        TangoAttributeInfoWrapper attributeInfo = deviceProxy.getProxy().getAttributeInfo(name);
        Class<?> targetType = attributeInfo.getClazz();
        Object converted = ConvertUtils.convert(value, targetType);

        tangoAttribute.write(converted);
        if (async) {
            CompletableFuture.runAsync(() -> {
                try {
                    tangoAttribute.write(converted);
                } catch (DevFailed ignored) {
                }
            });
            return null;
        } else {
            tangoAttribute.write(converted);
            return deviceAttributeValueGet();
        }
    }

    @GET
    @DynamicValue
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/value")
    public Object deviceAttributeGetValuePlain() throws Exception {
        tangoAttribute.update();
        return tangoAttribute.extract();
    }

    @GET
    @DynamicValue
    @Produces("image/jpeg")
    @Path("/value")
    @EmbeddedImage
    public ImageAttributeValue deviceAttributeGetValueImage() throws Exception {
        final TangoImage image =  deviceProxy.getProxy().readAttribute(name);
        return new ImageAttributeValue(image);
    }

    public class ImageAttributeValue {
        public TangoImage value;

        public ImageAttributeValue(TangoImage image) {
            this.value = image;
        }
    }
}
