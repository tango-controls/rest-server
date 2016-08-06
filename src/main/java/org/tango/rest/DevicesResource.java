package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.entities.NamedEntity;
import org.tango.rest.response.Responses;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.providers.Partitionable;
import org.tango.web.server.providers.StaticValue;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author ingvord
 * @since 8/6/16
 */
@Path("/devices")
public class DevicesResource {
    @Context
    private UriInfo uriInfo;

    @GET
    @StaticValue
    @Partitionable
    public Object get(@QueryParam("wildcard") String wildcard,
                      @Context DatabaseDs db,
                      @Context final ServletContext context){
        try {
            List<String> result = db.getDeviceList(wildcard == null ? "*" : wildcard);
            List<NamedEntity> transform = Lists.transform(result, new Function<String, NamedEntity>() {
                @Override
                public NamedEntity apply(final String input) {
                    return new NamedEntity(input, uriInfo.getAbsolutePath() + "/" + input);
                }
            });
            return transform;
        } catch (NoSuchCommandException | TangoProxyException e) {
            return Responses.createFailureResult(e);
        }
    }
}
