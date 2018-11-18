package org.tango.rest.rc5;

import fr.esrf.Tango.DevFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.*;
import org.tango.rest.entities.TangoHost;
import org.tango.web.server.binding.EventSystem;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.event.EventsManager;
import org.tango.web.server.event.SubscriptionsContext;
import org.tango.web.server.proxy.TangoDatabase;
import org.tango.web.server.proxy.TangoDeviceProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 2/14/17
 */
@Path("/rc5")
@Produces("application/json")
public class Rc5ApiImpl {
    private final Logger logger = LoggerFactory.getLogger(Rc5ApiImpl.class);

    @GET
    public Response authentication(@Context UriInfo uriInfo) {
        Map<String, String> result = new HashMap<>();

        result.put("hosts", uriInfo.getAbsolutePath() + "/hosts");

        Response.ResponseBuilder responseBuilder = Response.ok(result)
                .header("WWW-Authenticate", "Basic realm='Tango-Controls Realm'");

        return responseBuilder.build();
    }

    @GET
    @Path("/hosts")
    public void getHost() {
        throw new AssertionError("May not happen due to TangoDatabaseProvider");
    }

    @GET
    @StaticValue
    @Path("/hosts/{host}")
    public TangoHost getHost(@Context TangoDatabase db,
                          @Context final UriInfo uriInfo) throws DevFailed {
        return new TangoHost(db.getHost(), db.getPort(), db.asEsrfDb().get_name(),db.getInfo(),uriInfo.getAbsolutePath());
    }

    @Path("/hosts/{var:.+}/devices")
    public Object get() {
        return new Devices();
    }



    @Path("/hosts/{var:.+}/devices/tree")
    public DevicesTree getDevicesTreeForHost() {
        return new DevicesTree();
    }

    @Path("/devices/tree")
    public DevicesTree getDevicesTree() {
        return new DevicesTree();
    }

    @Path("/attributes")
    public Attributes getAttributes() {
        return new Attributes();
    }

    @Path("/subscriptions")
    @EventSystem
    public Subscriptions subscriptions(@Context EventsManager manager, @Context SubscriptionsContext context) {
        return new Subscriptions(manager, context);
    }
}
