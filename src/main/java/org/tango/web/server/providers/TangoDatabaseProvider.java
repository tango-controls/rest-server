package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.TangoRestServer;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.entities.Failures;
import org.tango.web.server.DatabaseDs;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
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
    private final TangoRestServer tangoRestServer;

    public TangoDatabaseProvider(TangoRestServer tangoRestServer) {
        this.tangoRestServer = tangoRestServer;
    }


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();
        MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
        String host = pathParams.getFirst("host");
        String port = pathParams.getFirst("port");

        if(host == null || port == null) return;

        DatabaseDs db = null;
        try{
            db = new DatabaseDs(tangoRestServer.getHostProxy(host, port, /*TODO matrix param*/"sys/database/2"));

            ResteasyProviderFactory.pushContext(DatabaseDs.class, db);
        } catch (TangoProxyException e){
            requestContext.abortWith(Response.serverError().entity(Failures.createInstance(e)).build());
        }
    }
}
