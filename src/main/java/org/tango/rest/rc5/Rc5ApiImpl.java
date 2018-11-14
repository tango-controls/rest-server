package org.tango.rest.rc5;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.esrf.TangoApi.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.client.database.DatabaseFactory;
import org.tango.client.database.ITangoDB;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.*;
import org.tango.rest.entities.DeviceFilters;
import org.tango.rest.entities.Failures;
import org.tango.rest.entities.TangoHost;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.binding.EventSystem;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.event.EventsManager;
import org.tango.web.server.event.SubscriptionsContext;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
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
    public TangoHost getHost(@Context DatabaseDs db,
                          @Context final UriInfo uriInfo,
                          @Context final ServletContext context) throws Exception {
        return new TangoHost(db.getHost(), db.getPort(), db.getName(),db.getInfo(),uriInfo.getAbsolutePath() + "/devices",uriInfo.getAbsolutePath() + "/tree");
    }

    @Path("/hosts/{var:.+}/devices")
    public Object get() {
        return new Devices();
    }

    @Path("/hosts/{var:.+}/devices/{domain}/{family}/{member}")
    public Device getDevice() {
        return new Device();
    }

    //    @Path("/hosts/{var:tango_host}/devices/tree")
//    public DevicesTree getDevicesTree(@PathParam("var") PathSegment tango_host,
//                                      @QueryParam("f") List<String> filters) {
    @Path("/hosts/{host}/{port}/devices/tree")
    public DevicesTree getDevicesTree(@PathParam("host") String host,
                                      @PathParam("port") String port,
                                      @QueryParam("f") List<String> filters) {
        return getDevicesTree(Lists.newArrayList(host + ":" + port), filters);
    }

    @Path("/hosts/tree")
    public DevicesTree getDevicesTree(@QueryParam("v") List<String> tango_hosts,
                                      @QueryParam("f") List<String> filters) {
        DeviceFilters filter = new DeviceFilters(filters.toArray(new String[filters.size()]));

        Iterable<String> it =
                new LinkedList<>(tango_hosts);

        return new DevicesTree(it, filter);
    }

    @Path("/subscriptions")
    @EventSystem
    public Subscriptions subscriptions(@Context EventsManager manager, @Context SubscriptionsContext context) {
        return new Subscriptions(manager, context);
    }
}
