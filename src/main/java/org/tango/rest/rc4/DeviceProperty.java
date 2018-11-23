package org.tango.rest.rc4;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.web.server.binding.DynamicValue;

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
    private final UriInfo uriInfo;

    public DeviceProperty(String name, UriInfo uriInfo) {
        this.name = name;
        this.uriInfo = uriInfo;
    }


    @GET
    @DynamicValue
    public Object get(@Context TangoProxy proxy) throws DevFailed {
        return DeviceHelper.dbDatumToResponse(proxy.toDeviceProxy().get_property(name));
    }

    @DELETE
    public void devicePropertyDelete(@Context TangoProxy proxy) throws DevFailed {
        proxy.toDeviceProxy().delete_property(name);
    }

    @POST
    @DynamicValue
    public Object devicePropertyPost(@FormParam("value") String[] value,
                                     @QueryParam("async") boolean async,
                                     @Context TangoProxy proxy) throws DevFailed {
        return devicePropertyPut(value, async, proxy);
    }

    @PUT
    @DynamicValue
    public Object devicePropertyPut(@QueryParam("value") String[] value,
                                    @QueryParam("async") boolean async,
                                    @Context TangoProxy proxy) throws DevFailed {
        DbDatum input = new DbDatum(name, value);

        proxy.toDeviceProxy().put_property(input);

        if (async)
            return null;
        else return DeviceHelper.dbDatumToResponse(proxy.toDeviceProxy().get_property(name));
    }
}
