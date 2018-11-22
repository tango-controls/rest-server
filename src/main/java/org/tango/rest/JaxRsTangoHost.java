package org.tango.rest;

import fr.esrf.Tango.DevFailed;
import org.tango.rest.rc5.entities.NamedEntity;
import org.tango.rest.rc5.entities.TangoHost;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.proxy.TangoDatabaseProxy;

import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/19/18
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/hosts/{var:.+}")
public class JaxRsTangoHost {
    @Context TangoDatabaseProxy database;

    @GET
    public TangoHost get(@Context UriInfo uriInfo) throws DevFailed {
        return new TangoHost(database.getHost(), database.getPort(), database.getName(), database.getInfo(), uriInfo.getAbsolutePath());
    }

    @GET
    @StaticValue
    @Partitionable
    @Path("/devices")
    public List<NamedEntity> getDevices(@DefaultValue("*/*/*") @QueryParam("wildcard") String wildcard,
                                        @Context UriInfo uriInfo) {
        return database.getDeviceNames(wildcard).stream()
                .map(s -> new NamedEntity(s, database.getDeviceAlias(s), uriInfo.getAbsolutePathBuilder().path(s).build()))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/devices/{domain}")
    @StaticValue
    @Partitionable
    public Object getFamilies(@PathParam("domain") String domain,
                              @DefaultValue("*") @QueryParam("wildcard") String wildcard,
                              @Context final UriInfo uriInfo) throws DevFailed {
        return database.asEsrfDatabase().get_device_family(domain + "/" + wildcard);
    }

    @GET
    @Path("/devices/{domain}/{family}")
    @StaticValue
    @Partitionable
    public Object getMembers(@PathParam("domain") String domain,
                             @PathParam("family") String family,
                             @DefaultValue("*") @QueryParam("wildcard") String wildcard,
                             @Context final UriInfo uriInfo) throws DevFailed {
        return database.asEsrfDatabase().get_device_member(domain + "/" + family + "/" + wildcard);
    }

    @Path("/devices/{domain}/{family}/{member}")
    public JaxRsDevice getDevice(@Context ResourceContext rc) {
        return rc.getResource(JaxRsDevice.class);
    }



    @Path("/devices/tree")
    public DevicesTree getDevicesTreeForHost() {
        return new DevicesTree();
    }
}
