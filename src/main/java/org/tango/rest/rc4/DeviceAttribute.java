/*
 * Copyright 2019 Tango Controls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tango.rest.rc4;

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
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.*;
import org.tango.client.ez.proxy.TangoAttributeInfoWrapper;
import org.tango.client.ez.proxy.TangoEvent;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.ValueTimeQuality;
import org.tango.rest.rc4.entities.AttributeValue;
import org.tango.rest.rc4.entities.Failures;
import org.tango.web.server.attribute.AttributeConfig;
import org.tango.web.server.attribute.AttributeProperty;
import org.tango.web.server.attribute.EventBuffer;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.proxy.TangoDeviceProxy;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
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
public class DeviceAttribute {
    @PathParam("attr")  String name;
    @Context TangoDeviceProxy proxy;

    @GET
    @StaticValue
    public Object get(@Context UriInfo uriInfo){
        final String href = uriInfo.getAbsolutePath().toString();

        return DeviceHelper.attributeInfoExToResponse(name, href);
    }

    @GET
    @Path("/{event:change|periodic|archive|user}")
    public void deviceAttributeEvent(@PathParam("event") String eventAsString,
                                     @DefaultValue("30000") @QueryParam("timeout") long timeout,
                                     @DefaultValue("0") @QueryParam("last") long last,
                                     @Context ServletContext context,
                                     @Context UriInfo uriInfo,
                                     @Suspended final AsyncResponse response
    ) throws Exception {
        TangoEvent event = null;
        try {
            event = TangoEvent.valueOf(eventAsString.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AssertionError("Can not happen! event must be one of change|periodic|archive|user but was " + eventAsString);
        }
        EventBuffer.EventKey eventKey = new EventBuffer.EventKey(proxy.getProxy(), name, event);

        EventBuffer buffer = (EventBuffer) context.getAttribute(EventBuffer.class.getName());

        //subscribe this buffer if not yet done
        buffer.subscribe(eventKey, proxy.getProxy());

        if (last > 0) {
            NavigableSet<?> result = buffer.getTail(eventKey, last);
            if (!result.isEmpty()) {
                response.resume(result.toArray(new Object[result.size()]));
            }
        }

        //this is required because field proxy is actually a proxy wrapper around ThreadLocal contextualData
        final TangoProxy finalTangoProxy = ResteasyProviderFactory.getContextData(TangoDeviceProxy.class).getProxy();

        CompletableFuture.runAsync(() -> {
            try {
                Object entity = buffer.createEvent(eventKey, finalTangoProxy).get(timeout);
                response.resume(entity);
            } catch (Exception e) {
                response.resume(Failures.createInstance(e));
            }
        });
    }

    @GET
    @Partitionable
    @Path("/history")
    public Object deviceAttributeHistory(@Context ServletContext context) throws DevFailed {
        return Lists.transform(Arrays.asList(proxy.getProxy().toDeviceProxy().attribute_history(name)), new Function<DeviceDataHistory, Object>() {
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
    //TODO return AttributeInfo from rest-api
    public AttributeInfoEx deviceAttributeInfo() throws DevFailed {
        return proxy.getProxy().toDeviceProxy().get_attribute_info_ex(name);
    }

    @PUT
    @Consumes("application/json")
    @Path("/info")
    //TODO return AttributeInfo from rest-api
    public AttributeInfoEx deviceAttributeInfoPut(@QueryParam("async") boolean async, AttributeConfig config) throws DevFailed {
        proxy.getProxy().toDeviceProxy().set_attribute_info(new AttributeInfoEx[]{new AttributeInfoEx(config.wrapped)});
        if (async) return null;
        return proxy.getProxy().toDeviceProxy().get_attribute_info_ex(name);
    }

    @GET
    @StaticValue
    @Path("/properties")
    public List<AttributeProperty> deviceAttributeProperties() throws DevFailed {
        return Lists.transform(proxy.getProxy().toDeviceProxy().get_attribute_property(name), new Function<DbDatum, AttributeProperty>() {
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
        Iterable<? extends DbDatum> datumCollection = Iterables.filter(proxy.getProxy().toDeviceProxy().get_attribute_property(name), new Predicate<DbDatum>() {
            @Override
            public boolean apply(DbDatum input) {
                return property.equalsIgnoreCase(input.name);
            }
        });
        if (!datumCollection.iterator().hasNext()) throw new NotFoundException(
                String.format("Device attribute [%s/%s] has no property[%s]", proxy.getName(), name, property));
        return new AttributeProperty(datumCollection.iterator().next());
    }

    @PUT
    @Path("/properties/{prop}")
    public DbAttribute deviceAttributePropertyPut(@PathParam("prop") final String propName,
                                                  @QueryParam("value") final String propValue,
                                                  @QueryParam("async") boolean async) throws DevFailed {
        DbAttribute dbAttribute = proxy.getProxy().toDeviceProxy().get_attribute_property(name);

        DbDatum datum = dbAttribute.datum(propName);

        if(datum != null) datum.insert(propValue);
        else dbAttribute.add(propName, propValue);

        proxy.getProxy().toDeviceProxy().put_attribute_property(dbAttribute);

        if(async) return null;
        return dbAttribute;
    }

    @DELETE
    @Path("/properties/{prop}")
    public void deviceAttributePropertyDelete(@PathParam("prop") final String propName) throws DevFailed {
        DbAttribute dbAttribute = proxy.getProxy().toDeviceProxy().get_attribute_property(name);

        for (Iterator<DbDatum> iterator = dbAttribute.iterator(); iterator.hasNext(); ) {
            DbDatum dbDatum = iterator.next();
            if (dbDatum.name.equalsIgnoreCase(propName)) {
                iterator.remove();
                break;
            }
        }
        proxy.getProxy().toDeviceProxy().put_attribute_property(dbAttribute);
    }

    @GET
    @DynamicValue
    @Path("/value")
    public Object deviceAttributeValueGet() throws Exception {
        final ValueTimeQuality<Object> result = proxy.getProxy().readAttributeValueTimeQuality(name);

        return new AttributeValue<Object>(DeviceAttribute.this.name, result.value, result.quality.toString(), result.time);
    }

    @PUT
    @Path("/value")
    public Object deviceAttributeValuePut(@QueryParam("v") String value, @QueryParam("async") boolean async) throws Exception {

        TangoAttributeInfoWrapper attributeInfo = proxy.getProxy().getAttributeInfo(name);
        Class<?> targetType = attributeInfo.getClazz();
        Object converted = ConvertUtils.convert(value, targetType);

        proxy.getProxy().writeAttribute(name, converted);
        if (async) {
            return null;
        }
        return deviceAttributeValueGet();
    }

    @GET
    @DynamicValue
    @Path("/value/plain")
    public Object deviceAttributeGetValuePlain() throws Exception {
        return proxy.getProxy().readAttribute(name);
    }

    @GET
    @DynamicValue
    @Path("/value/image")
    public Object deviceAttributeGetValueImage() throws Exception {
        //TODO may throw ClassCast in case non image attribute is requested
        final TangoImage image =  proxy.getProxy().readAttribute(name);
        return new ImageAttributeValue(image);
    }

    public class ImageAttributeValue {
        public TangoImage value;

        public ImageAttributeValue(TangoImage image) {
            this.value = image;
        }
    }
}
