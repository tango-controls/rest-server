package org.tango.subscriptions;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.tango.web.server.providers.EventSystemProvider;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.tango.rest.TangoRestApi.getCorsFilter;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 3/29/19
 */
@ApplicationPath("/subscriptions")
public class SubscriptionsTangoApi extends Application {
    @Context
    private ServletContext servletContext;

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new LinkedHashSet<>();

        classes.add(JaxRsSubscriptions.class);

        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new LinkedHashSet<>();

        // = = = CORS = = =
        CorsFilter cors = getCorsFilter();
        singletons.add(cors);

        singletons.add(new EventSystemProvider());

        return singletons;
    }

}
