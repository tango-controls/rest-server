package org.tango.rest;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.web.server.providers.AttributeValue;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/properties/{prop}")
@Produces("application/json")
public class DeviceProperty {

    private final String name;

    public DeviceProperty(String name) {
        this.name = name;
    }


    @GET
    @AttributeValue
    public Object get(@Context TangoProxy proxy) throws DevFailed {
        return DeviceHelper.dbDatumToResponse(proxy.toDeviceProxy().get_property(name));
    }

    @DELETE
    public void devicePropertyDelete(@Context TangoProxy proxy) throws DevFailed {
        proxy.toDeviceProxy().delete_property(name);
    }

    @POST
    @AttributeValue
    public Object devicePropertyPost(@QueryParam("value") String[] value,
                                     @Context UriInfo uriInfo,
                                     @Context TangoProxy proxy) throws DevFailed {
        return devicePropertyPut(value, uriInfo, proxy);
    }

    @PUT
    @AttributeValue
    public Object devicePropertyPut(@QueryParam("value") String[] value,
                                    @Context UriInfo uriInfo,
                                    @Context TangoProxy proxy) throws DevFailed {
        boolean async = uriInfo.getQueryParameters().containsKey("async");

        DbDatum input = new DbDatum(name, value);

        proxy.toDeviceProxy().put_property(input);

        if (async)
            return null;
        else return DeviceHelper.dbDatumToResponse(proxy.toDeviceProxy().get_property(name));
    }
}
