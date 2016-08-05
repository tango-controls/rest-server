package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.response.Responses;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.DeviceMapper;
import org.tango.web.server.TangoContext;

import javax.servlet.ServletContext;
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
public class TangoDatabaseFromPathProvider implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();
        MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
        String host = pathParams.getFirst("host");
        String port = pathParams.getFirst("port");


        DatabaseDs db = null;
        TangoProxy result = null;
        try{
            String tangoDbName = ((TangoContext)ResteasyProviderFactory.getContextData(ServletContext.class).getAttribute(TangoContext.TANGO_CONTEXT)).tangoDbName;

            if(host != null && port != null)
                result = TangoProxies.newDeviceProxyWrapper("tango://" + host + ":" + port + "/" + tangoDbName);

            db = new DatabaseDs(result);
            ResteasyProviderFactory.pushContext(DatabaseDs.class, db);
        } catch (TangoProxyException e){
            requestContext.abortWith(Response.ok(Responses.createFailureResult(e)).build());
        }
    }
}
