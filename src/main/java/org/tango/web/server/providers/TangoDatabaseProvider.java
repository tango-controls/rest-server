package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.response.Responses;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.TangoContext;

import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/5/16
 */
@Provider
public class TangoDatabaseProvider implements ContainerRequestFilter {
    @Context
    private ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        TangoContext tangoContext = (TangoContext) servletContext.getAttribute(TangoContext.TANGO_CONTEXT);

        UriInfo uriInfo = requestContext.getUriInfo();
        MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
        String host = pathParams.getFirst("host");
        String port = pathParams.getFirst("port");

        if(host == null || port == null) return;

        DatabaseDs db = null;
        try{
            db = new DatabaseDs(tangoContext.getHostProxy(host,  port));

            ResteasyProviderFactory.pushContext(DatabaseDs.class, db);
        } catch (TangoProxyException e){
            requestContext.abortWith(Response.ok(Responses.createFailureResult(e)).build());
        }
    }
}
