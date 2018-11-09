package org.tango.rest.rc4;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.*;
import org.tango.rest.entities.DeviceFilters;
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
@Path("/rc4")
@Produces("application/json")
public class Rc4ApiImpl {
    public static final String DEFAULT_TANGO_PORT = "10000";
    private final Logger logger = LoggerFactory.getLogger(Rc4ApiImpl.class);

    @GET
    public Map<String, String> authentication(@Context UriInfo uriInfo, @Context ServletContext context, @Context TangoRestServer tangoContext) {
        Map<String, String> result = new HashMap<>();

        result.put("hosts", uriInfo.getAbsolutePath() + "/hosts");
        result.put("x-auth-method", SupportedAuthentication.VALUE);

        return result;
    }

    @GET
    @Path("/hosts")
    public Map<String, String> getHosts(@Context final UriInfo uriInfo, @Context TangoRestServer tangoContext) throws TangoProxyException {
        Map<String, String> result = Maps.newHashMap();

        for (Map.Entry<String, String> entry : Collections2.transform(tangoContext.hostsPool.proxies(), new Function<String, Map.Entry<String, String>>() {
            @Override
            public Map.Entry<String, String> apply(@Nullable String input) {
                if (input == null) return null;
                URI uri = URI.create(input);
                return new AbstractMap.SimpleEntry<>(
                        String.format("%s:%d", uri.getHost(), uri.getPort()),
                        String.format("%s%s/%d", uriInfo.getAbsolutePath(), uri.getHost(), uri.getPort()));
            }
        }))
            result.put(entry.getKey(), entry.getValue());

        return result;
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/hosts/{host}/{port}")
    public Object getHost(@Context final UriInfo uriInfo,
                          @Context final DatabaseDs db,
                          @Context final ServletContext context) throws Exception {
        final String[] tangoHost = db.toDeviceProxy().get_tango_host().split(":");
        return new Object() {
            public String name = db.toDeviceProxy().get_name();
            public String host = tangoHost[0];
            public int port = Integer.parseInt(tangoHost[1]);
            public List<String> info = db.getInfo();
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
    public Subscriptions subscriptions(@Context EventsManager manager, @Context SubscriptionsContext context) {
        return new Subscriptions(manager, context);
    }
}
