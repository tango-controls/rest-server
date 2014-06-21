package org.tango.web.server;

/**
 * @author Ingvord
 * @since 21.06.14
 */

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Arrays;
import java.util.Collection;

@Path("/")
public class TangoGateway {

    @GET
    @Path("devices")
    public Collection<String> getDevices() {
        return Arrays.asList("1", "2", "3");
    }

    @GET
    @Path("device/{domain}/{name}/{instance}")
    public String getDeviceInfo(@PathParam("domain") String domain,
                                @PathParam("name") String name,
                                @PathParam("instance") String instance) {
        return String.format("%s/%s/%s", domain, name, instance);
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/attributes")
    public String getDeviceAttributes(@PathParam("domain") String domain,
                                      @PathParam("name") String name,
                                      @PathParam("instance") String instance) {
        return "List of device attributes";
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/commands")
    public String getDeviceCommands(@PathParam("domain") String domain,
                                    @PathParam("name") String name,
                                    @PathParam("instance") String instance) {
        return "List of device commands";
    }
}
