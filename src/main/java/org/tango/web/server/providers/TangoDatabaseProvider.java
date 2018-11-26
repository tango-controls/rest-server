package org.tango.web.server.providers;

import fr.esrf.Tango.DevFailed;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.rest.rc4.Rc4ApiImpl;
import org.tango.rest.rc4.entities.Failures;
import org.tango.web.server.proxy.Proxies;
import org.tango.web.server.proxy.TangoDatabaseProxy;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/5/16
 */
@Provider
@Priority(Priorities.USER + 100)
public class TangoDatabaseProvider implements ContainerRequestFilter {
    public static final String DEFAULT_TANGO_PORT = "10000";
    private final Logger logger = LoggerFactory.getLogger(TangoDatabaseProvider.class);

    private final TangoRestServer tangoRestServer;

    public TangoDatabaseProvider(TangoRestServer tangoRestServer) {
        this.tangoRestServer = tangoRestServer;
    }


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.trace("TangoDatabaseProvider");
        UriInfo uriInfo = requestContext.getUriInfo();
        List<PathSegment> pathSegments = uriInfo.getPathSegments();
        if (pathSegments.size() < 3) return;
        if (!pathSegments.get(2).getPath().equalsIgnoreCase("hosts")) return;

        if (pathSegments.size() == 3/* no host was specified*/) {
            requestContext.abortWith(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(Failures.createInstance("No Tango host was specified")).build());
            return;
        }

        TangoHostExtractor tangoHostExtractor =
                pathSegments.get(1).getPath().equalsIgnoreCase(Rc4ApiImpl.RC_4) ?
                        uriInfo1 -> new TangoHost(uriInfo1.getPathSegments().get(3).getPath(), uriInfo1.getPathSegments().get(4).getPath()) : //assume rc5
                        uriInfo12 -> {
                            PathSegment pathSegment = uriInfo12.getPathSegments().get(3);
                            return new TangoHost(pathSegment.getPath(), Optional.ofNullable(
                                    pathSegment.getMatrixParameters().getFirst("port"))
                                    .orElse(DEFAULT_TANGO_PORT));
                        };


        TangoHost tangoHost = tangoHostExtractor.apply(uriInfo);

        TangoDatabaseProxy tangoDb = tangoRestServer.getContext().hosts.
                getUnchecked(tangoHost.toString()).orElseGet(() -> {
            try {
                return Proxies.getDatabase(tangoHost.host, tangoHost.port);
            } catch (DevFailed devFailed) {
                Response.Status status = Response.Status.BAD_REQUEST;
                if (devFailed.errors.length >= 1 && devFailed.errors[0].reason.equalsIgnoreCase("Api_GetCanonicalHostNameFailed"))
                    status = Response.Status.NOT_FOUND;
                requestContext.abortWith(Response.status(status).entity(Failures.createInstance(devFailed)).build());
                return null;
            }
        });

        ResteasyProviderFactory.pushContext(TangoDatabaseProxy.class, tangoDb);
    }

    @FunctionalInterface
    private static interface TangoHostExtractor {
        TangoHost apply(UriInfo uriInfo) throws IllegalArgumentException;
    }

    private static class TangoHost {
        String host;
        String port;

        public TangoHost(String host, String port) {
            this.host = host;
            this.port = port;
        }

        public String toString() {
            return host + ":" + port;
        }
    }
}
