package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.TangoContext;
import org.tango.web.server.exception.mapper.GeneralExceptionMapper;
import org.tango.web.server.exception.mapper.NoSuchCommand;

import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.12.2015
 */
@Provider
public class TangoProxyProvider implements ContainerRequestFilter {
    @Context
    public ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        TangoContext tangoContext = (TangoContext) servletContext.getAttribute(TangoContext.TANGO_CONTEXT);

        UriInfo uriInfo = requestContext.getUriInfo();
        MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
        String host = pathParams.getFirst("host");
        String port = pathParams.getFirst("port");
        String domain = pathParams.getFirst("domain");
        String family = pathParams.getFirst("family");
        String member = pathParams.getFirst("member");

        if(domain == null || family == null || member == null)
            return;

        TangoProxy result = null;
        DatabaseDs db = null;
        try {
            if(host == null || port == null)
                db = new DatabaseDs(tangoContext.getHostProxy());
            else
                db = new DatabaseDs(tangoContext.getHostProxy(host, port));

            result = tangoContext.proxyPool.getProxy(db.getDeviceAddress(domain + "/" + family + "/" + member));

            ResteasyProviderFactory.pushContext(TangoProxy.class, result);
        } catch (TangoProxyException e) {
            requestContext.abortWith(new GeneralExceptionMapper().toResponse(e));
        } catch (NoSuchCommandException e) {
            assert false;
            requestContext.abortWith(new NoSuchCommand().toResponse(e));
        }
    }
}
