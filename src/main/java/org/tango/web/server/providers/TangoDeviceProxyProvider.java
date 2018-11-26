package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.rc4.entities.Failures;
import org.tango.web.server.proxy.Proxies;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.proxy.TangoDeviceProxy;
import org.tango.web.server.proxy.TangoDeviceProxyImpl;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Provides {@link TangoDeviceProxyImpl} extracted from URL e.g.
 *
 * <pre>.../sys/tg_test/1/...</pre> becomes a Java object of type {@link TangoDeviceProxyImpl}
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.12.2015
 */
@Provider
@Priority(Priorities.USER + 200)
public class TangoDeviceProxyProvider implements ContainerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(TangoDeviceProxyProvider.class);

    private final TangoRestServer tangoRestServer;

    public TangoDeviceProxyProvider(TangoRestServer tangoRestServer) {
        this.tangoRestServer = tangoRestServer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.trace("TangoDeviceProxyProvider");
        TangoDatabaseProxy db = ResteasyProviderFactory.getContextData(TangoDatabaseProxy.class);
        if (db == null) return;

        UriInfo uriInfo = requestContext.getUriInfo();
        MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
        String domain = pathParams.getFirst("domain");
        String family = pathParams.getFirst("family");
        String member = pathParams.getFirst("member");

        if(domain == null || family == null || member == null)
            return;

        TangoDeviceName name = new TangoDeviceName(db.getTangoHost(), domain, family, member);

        TangoDeviceProxy result = tangoRestServer.getContext().devices.getUnchecked(name.toString()).orElseGet(() -> {
            try {
                return Proxies.newTangoDeviceProxy(name.host, name.name);
            } catch (TangoProxyException e) {
                Response.Status status = Response.Status.BAD_REQUEST;
                if(e.reason.contains("DB_DeviceNotDefined"))
                    status = Response.Status.NOT_FOUND;
                requestContext.abortWith(Response.status(status).entity(Failures.createInstance(e)).build());
                return null;
            }
        } );

        if (Objects.nonNull(result)) {
            ResteasyProviderFactory.pushContext(TangoDeviceProxy.class, result);
            //TODO rc4 migrate to TangoDeviceProxy

            ResteasyProviderFactory.pushContext(TangoProxy.class, result.getProxy());
        }
    }

    private static class TangoDeviceName {
        String host;
        String name;

        public TangoDeviceName(String host, String domain, String family, String member) {
            this.name = new StringJoiner("/").add(domain).add(family).add(member).toString();
            this.host = host;
        }

        public String toString(){
            return "tango://" + host + "/" + name;
        }
    }
}
