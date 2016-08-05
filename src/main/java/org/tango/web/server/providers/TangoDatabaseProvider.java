package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.web.server.DatabaseDs;

import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.12.2015
 */
@Provider
@TangoDatabaseBackend
public class TangoDatabaseProvider implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        DatabaseDs result = null;
        result = ((DatabaseDs) ResteasyProviderFactory.getContextData(ServletContext.class).getAttribute(DatabaseDs.TANGO_DB));

        ResteasyProviderFactory.pushContext(DatabaseDs.class, result);
    }
}
