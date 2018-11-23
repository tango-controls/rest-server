package org.tango.rest.rc4;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.rc4.entities.NamedEntity;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.StaticValue;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
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
                      @Context DatabaseDs db,
                      @Context final ServletContext context) throws TangoProxyException {
        try {
            List<String> result = db.getDeviceList(wildcard);
            List<NamedEntity> transform = Lists.transform(result, new Function<String, NamedEntity>() {
                @Override
                public NamedEntity apply(final String input) {
                    return new NamedEntity(input, uriInfo.getAbsolutePath() + "/" + input);
                }
            });
            return transform;
        } catch (NoSuchCommandException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    @GET
    @Path("/{domain}")
    @StaticValue
    @Partitionable
    public Object getFamilies(@PathParam("domain") String domain,
                              @DefaultValue("*") @QueryParam("wildcard") String wildcard,
                              @Context final UriInfo uriInfo,
                              @Context DatabaseDs db,
                              @Context final ServletContext context) throws TangoProxyException {
        try {
            return db.getFamiliesList(domain, wildcard);
        } catch (NoSuchCommandException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    @GET
    @Path("/{domain}/{family}")
    @StaticValue
    @Partitionable
    public Object getMembers(@PathParam("domain") String domain,
                             @PathParam("family") String family,
                             @DefaultValue("*") @QueryParam("wildcard") String wildcard,
                             @Context final UriInfo uriInfo,
                             @Context DatabaseDs db,
                             @Context final ServletContext context) throws TangoProxyException {
        try {
            return db.getMembersList(domain, family, wildcard);
        } catch (NoSuchCommandException e) {
            throw new AssertionError(e.getMessage());
        }
    }
}
