package org.tango.rest.rc1;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.jboss.resteasy.annotations.cache.Cache;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.DeviceMapper;
import org.tango.web.server.Responses;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.11.2015
 */
@Path("/rc1")
@Produces("application/json")
public class Rc1ApiImpl {
    @GET
//    @Cache(maxAge = 10)
    @Path("devices")
    public Response devices(@QueryParam("wildcard") String wildcard,
                                      @Context final ServletContext context){
        DatabaseDs db = (DatabaseDs) context.getAttribute(DatabaseDs.TANGO_DB);

        try {
            Iterable<String> result = db.getDeviceList(wildcard == null ? "*" : wildcard);
            Iterable<Object> transform = Iterables.transform(result, new Function<String, Object>() {
                @Override
                public Object apply(final String input) {
                    return new Object() {
                        public final String name = input;
                        public final String href = context.getContextPath() + "/rest/rc1/devices/" + input;
                    };
                }
            });
            CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(10);
            return Response.ok(transform).cacheControl(cacheControl).build();
        } catch (TangoProxyException e) {
            return Response.ok(Responses.createFailureResult(e)).build();
        }
    }

    @GET
    @Path("devices/{domain}/{family}/{member}")
    public Object device(@PathParam("domain") String domain,
                         @PathParam("family") String family,
                         @PathParam("member") String member,
                         @Context ServletContext context){
        try {
            TangoProxy lookup = DeviceMapper.lookup(domain, family, member, context);
            return lookup;
        } catch (TangoProxyException e) {
            return Responses.createFailureResult(e);
        }
    }
}
