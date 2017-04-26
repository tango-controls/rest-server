package org.tango.web.server.providers;

import org.tango.TangoRestServer;
import org.tango.web.server.TangoContext;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/26/17
 */
@Provider
public class CustomProvider implements ContextResolver<TangoContext>{
    @Context
    private ServletContext servletContext;

    @Override
    public TangoContext getContext(Class<?> type) {
        return (TangoContext) servletContext.getAttribute(TangoContext.TANGO_CONTEXT);
    }
}
