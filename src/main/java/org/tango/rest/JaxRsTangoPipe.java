package org.tango.rest;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.PipeBlob;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.rest.entities.Pipe;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.RequiresTangoPipe;
import org.tango.web.server.binding.RequiresTangoSelector;
import org.tango.web.server.proxy.TangoPipeProxy;
import org.tango.web.server.util.TangoRestEntityUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/pipes/{pipe}")
@Produces("application/json")
@RequiresTangoPipe
public class JaxRsTangoPipe {
    @PathParam("pipe") String name;
    @Context TangoPipeProxy proxy;

    @GET
    @DynamicValue
    public Pipe get(@Context UriInfo uriInfo) {
        return TangoRestEntityUtils.newPipe(proxy, uriInfo);
    }

    @GET
    @DynamicValue
    @Path("/value")
    public Object getValue() throws DevFailed {
        return proxy.read();
    }

    @PUT
    @Consumes("application/json")
    @DynamicValue
    @Path("/value")
    public Object devicePipePut(@QueryParam("async") boolean async, @Context UriInfo info, PipeBlob blob) throws DevFailed {
        proxy.write(blob);
        if (async) return null;
        else return get(info);
    }
}
