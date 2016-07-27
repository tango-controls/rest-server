package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.web.server.TangoContext;

import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.12.2015
 */
@Provider
public class TangoContextProvider implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ResteasyProviderFactory.pushContext(
                TangoContext.class,
                (TangoContext) ResteasyProviderFactory.getContextData(ServletContext.class).getAttribute(TangoContext.TANGO_CONTEXT));
    }
}
