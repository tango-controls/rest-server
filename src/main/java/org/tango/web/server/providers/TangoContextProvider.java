package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.TangoRestServer;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.12.2015
 */
@Provider
@Priority(Priorities.USER)
public class TangoContextProvider implements ContainerRequestFilter {
    private final TangoRestServer tangoRestServer;

    public TangoContextProvider(TangoRestServer tangoRestServer) {
        this.tangoRestServer = tangoRestServer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ResteasyProviderFactory.pushContext(
                TangoRestServer.class,
                tangoRestServer);
    }
}
