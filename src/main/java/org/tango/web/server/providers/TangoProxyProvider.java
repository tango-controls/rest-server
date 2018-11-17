package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.exception.mapper.TangoProxyExceptionMapper;
import org.tango.web.server.util.TangoDatabase;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.12.2015
 */
@Provider
@Priority(Priorities.USER + 200)
public class TangoProxyProvider implements ContainerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(TangoProxyProvider.class);

    private final TangoRestServer tangoRestServer;

    public TangoProxyProvider(TangoRestServer tangoRestServer) {
        this.tangoRestServer = tangoRestServer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        TangoDatabase db = ResteasyProviderFactory.getContextData(TangoDatabase.class);
        if (db == null) return;

        UriInfo uriInfo = requestContext.getUriInfo();
        MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
        String domain = pathParams.getFirst("domain");
        String family = pathParams.getFirst("family");
        String member = pathParams.getFirst("member");

        if(domain == null || family == null || member == null)
            return;

        TangoProxy result = null;
        try {
            result = tangoRestServer.proxyPool.getProxy("tango://" + db.getFullTangoHost() + "/" + domain + "/" + family + "/" + member);

            ResteasyProviderFactory.pushContext(TangoProxy.class, result);
        } catch (TangoProxyException e) {
            logger.error("Failed to get proxy for {}/{}/{}", domain, family, member);
            requestContext.abortWith(new TangoProxyExceptionMapper().toResponse(e));
        }
    }
}
