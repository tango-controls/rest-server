package org.tango.rest.rc4;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.rc4.entities.DeviceFilters;
import org.tango.rest.v10.JaxRsSubscriptions;
import org.tango.web.server.binding.EventSystem;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.event.EventsManager;
import org.tango.web.server.event.SubscriptionsContext;
import org.tango.web.server.proxy.TangoDatabaseProxy;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 2/14/17
 */
@Path("/rc4")
@Produces("application/json")
public class Rc4ApiImpl {
    public static final String RC_4 = "rc4";
    private final Logger logger = LoggerFactory.getLogger(Rc4ApiImpl.class);

    @GET
    public Map<String, String> authentication(@Context UriInfo uriInfo, @Context ServletContext context, @Context TangoRestServer tangoContext) {
        Map<String, String> result = new HashMap<>();

        result.put("hosts", uriInfo.getAbsolutePath() + "/hosts");
        result.put("x-auth-method", "basic");

        return result;
    }

    @GET
    @Path("/hosts")
    public List<Map.Entry<String, String>> getHosts(@Context final UriInfo uriInfo, @Context TangoRestServer tangoContext) throws TangoProxyException {
        return Lists.newArrayList(Iterables.transform(tangoContext.getContext().hosts.asMap().values(), input -> new AbstractMap.SimpleEntry<String, String>(
                input.get().getTangoHost(),
                String.format("%s%s/%s", uriInfo.getAbsolutePath(), input.get().getHost(), input.get().getPort()))));
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/hosts/{host}/{port}")
    public Object getHost(@Context final UriInfo uriInfo,
                          @Context final TangoDatabaseProxy db,
                          @Context final ServletContext context) throws Exception {
        return new Object() {
            public String name = db.getName();
            public String host = db.getHost();
            public String port = db.getPort();
            public String[] info = db.getInfo();
            public String devices = uriInfo.getAbsolutePath() + "/devices";
        };
    }

    @Path("/hosts/{host}/{port}/devices")
    public Object get() {
        return new Devices();
    }

    @Path("/hosts/{host}/{port}/devices/{domain}/{family}/{member}")
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
    public JaxRsSubscriptions subscriptions(@Context EventsManager manager, @Context SubscriptionsContext context) {
        return new JaxRsSubscriptions(manager, context);
    }
}
