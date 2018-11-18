package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import org.tango.rest.entities.NamedEntity;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.proxy.TangoDatabase;
import org.tango.web.server.proxy.TangoDeviceProxy;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 5/15/17
 */
@Path("/devices")
@Produces("application/json")
public class Devices {
    @GET
    @StaticValue
    @Partitionable
    public Object get(@DefaultValue("*") @QueryParam("wildcard") String wildcard,
                      @Context final UriInfo uriInfo,
                      @Context TangoDatabase db,
                      @Context final ServletContext context) throws DevFailed {
        List<String> result = Arrays.asList(db.asEsrfDb().get_device_list(wildcard));
        List<NamedEntity> transform = Lists.transform(result, new Function<String, NamedEntity>() {
            @Override
            public NamedEntity apply(final String input) {
                return new NamedEntity(input, uriInfo.getAbsolutePath() + "/" + input);
            }
        });
        return transform;
    }

    @GET
    @Path("/{domain}")
    @StaticValue
    @Partitionable
    public Object getFamilies(@PathParam("domain") String domain,
                              @DefaultValue("*") @QueryParam("wildcard") String wildcard,
                              @Context final UriInfo uriInfo,
                              @Context TangoDatabase db,
                              @Context final ServletContext context) throws DevFailed {
        return db.asEsrfDb().get_device_family(domain + "/" + wildcard);
    }

    @GET
    @Path("/{domain}/{family}")
    @StaticValue
    @Partitionable
    public Object getMembers(@PathParam("domain") String domain,
                             @PathParam("family") String family,
                             @DefaultValue("*") @QueryParam("wildcard") String wildcard,
                             @Context final UriInfo uriInfo,
                             @Context TangoDatabase db,
                             @Context final ServletContext context) throws DevFailed {
        return db.asEsrfDb().get_device_member(domain + "/" + family + "/" + wildcard);
    }

    @Path("/{domain}/{family}/{member}")
    public JaxRsDevice getDevice(@Context TangoDeviceProxy proxy) {
        return new JaxRsDevice(proxy);
    }
}
