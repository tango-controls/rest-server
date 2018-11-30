package org.tango.rest.rc4;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.PipeBlob;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.proxy.TangoDeviceProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/{pipe}")
@Produces("application/json")
public class DevicePipe {
    @Context TangoDeviceProxy proxy;
    @PathParam("pipe") String name;

    @GET
    @DynamicValue
    public Object get(@Context UriInfo uriInfo) throws DevFailed {
        final String href = uriInfo.getAbsolutePath().toString();
        final fr.esrf.TangoApi.DevicePipe result = proxy.getProxy().toDeviceProxy().readPipe(name);
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
        proxy.getProxy().toDeviceProxy().writePipe(name, blob);
        if (async) return null;
        else return get(info);
    }
}
