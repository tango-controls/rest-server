package org.tango.web.server.providers;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.entities.Failures;
import org.tango.utils.DevFailedUtils;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.util.TangoDatabaseUtils;

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
        UriInfo uriInfo = requestContext.getUriInfo();
        List<PathSegment> pathSegments = uriInfo.getPathSegments();
        if(pathSegments.size() < 3) return;
        if (!pathSegments.get(2).getPath().equalsIgnoreCase("hosts")) return;

        if (pathSegments.size() == 3/* no host was specified*/) {
            requestContext.abortWith(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(Failures.createInstance("No Tango host was specified")).build());
            return;
        }


        PathSegment tango_host = pathSegments.get(3);
        String host = tango_host.getPath();
        String port = tango_host.getMatrixParameters().getFirst("port");
        if (port == null) port = DEFAULT_TANGO_PORT;
        final String finalPort = port;

        Optional<Database> tangoDb = TangoDatabaseUtils.getDatabase(host, finalPort);

        tangoDb.ifPresent(database -> {
            try {
                DatabaseDs db = new DatabaseDs(host, finalPort, tangoRestServer.getHostProxy(host, finalPort, database.get_name()), database);

                ResteasyProviderFactory.pushContext(DatabaseDs.class, db);
            } catch (TangoProxyException e) {
                logger.error("Failed to create a proxy for TangoDatabase {}:{} due to: {}", host, finalPort, e.getMessage());
                requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity(Failures.createInstance(e)).build());
            }
        });
    }
}
