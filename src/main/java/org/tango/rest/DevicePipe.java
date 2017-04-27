package org.tango.rest;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.PipeBlob;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.web.server.binding.DynamicValue;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/pipes/{pipe}")
@Produces("application/json")
public class DevicePipe {
    private final TangoProxy proxy;
    private final String name;

    public DevicePipe(TangoProxy proxy, String name) {
        this.proxy = proxy;
        this.name = name;
    }

    @GET
    @DynamicValue
    public Object get(@Context UriInfo uriInfo) throws DevFailed {
        final String href = uriInfo.getAbsolutePath().toString();
        final fr.esrf.TangoApi.DevicePipe result = proxy.toDeviceProxy().readPipe(name);
        return new Object() {
            public String name = DevicePipe.this.name;
            public int size = result.getPipeBlob().size();
            public long timestamp = result.getTimeValMillisSec();
            public PipeBlob data = result.getPipeBlob();
            public Object _links = new Object() {
                public String _self = href;
            };
        };
    }

    @PUT
    @Consumes("application/json")
    @DynamicValue
    public Object devicePipePut(@QueryParam("async") boolean async, @Context UriInfo info, PipeBlob blob) throws DevFailed {
        proxy.toDeviceProxy().writePipe(name, blob);
        if (async) return null;
        else return get(info);
    }
}
