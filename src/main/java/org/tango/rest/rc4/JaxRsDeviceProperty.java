package org.tango.rest.rc4;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.web.server.binding.DynamicValue;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/{prop}")
@Produces("application/json")
public class JaxRsDeviceProperty {
    @PathParam("prop") String name;
    @Context TangoProxy proxy;


    @GET
    @DynamicValue
    public Object get() throws DevFailed {
        return DeviceHelper.dbDatumToResponse(proxy.toDeviceProxy().get_property(name));
    }

    @DELETE
    public void delete() throws DevFailed {
        proxy.toDeviceProxy().delete_property(name);
    }

    @POST
    @DynamicValue
    public Object post(@FormParam("value") String[] value,
                                     @QueryParam("async") boolean async) throws DevFailed {
        return put(value, async);
    }

    @PUT
    @DynamicValue
    public Object put(@QueryParam("value") String[] value,
                                    @QueryParam("async") boolean async) throws DevFailed {
        DbDatum input = new DbDatum(name, value);

        proxy.toDeviceProxy().put_property(input);

        if (async)
            return null;
        else return DeviceHelper.dbDatumToResponse(proxy.toDeviceProxy().get_property(name));
    }
}
