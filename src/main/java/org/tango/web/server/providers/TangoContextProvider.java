package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.web.server.TangoContext;

import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.12.2015
 */
@Provider
public class TangoContextProvider implements ContainerRequestFilter {
    @Context
    private Providers providers;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ResteasyProviderFactory.pushContext(
                TangoContext.class,
                providers.getContextResolver(TangoContext.class, MediaType.WILDCARD_TYPE).getContext(TangoContext.class));
    }
}
