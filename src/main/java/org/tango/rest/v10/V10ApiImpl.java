package org.tango.rest.v10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.web.server.binding.EventSystem;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.event.EventsManager;
import org.tango.web.server.event.SubscriptionsContext;

import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.*;
import java.util.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 2/14/17
 */
@Path("/rc5")
@Produces("application/json")
public class V10ApiImpl {
    private final Logger logger = LoggerFactory.getLogger(V10ApiImpl.class);

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

    @StaticValue
    @Path("/hosts/{host}")
    public JaxRsTangoHost getHost(@Context ResourceContext rc) {
        return rc.getResource(JaxRsTangoHost.class);
    }



    @Path("/devices/tree")
    public DevicesTree getDevicesTree() {
        return new DevicesTree();
    }

    @Path("/attributes")
    public JaxRsTangoAttributes getAttributes() {
        return new JaxRsTangoAttributes();
    }

    @Path("/commands")
    public JaxRsTangoCommands getCommands(@Context ResourceContext rc) {
        return rc.getResource(JaxRsTangoCommands.class);
    }

    @Path("/pipes")
    public JaxRsTangoPipes getPipes(@Context ResourceContext rc) {
        return rc.getResource(JaxRsTangoPipes.class);
    }

    @Path("/subscriptions")
    @EventSystem
    public Subscriptions subscriptions(@Context EventsManager manager, @Context SubscriptionsContext context) {
        return new Subscriptions(manager, context);
    }
}
