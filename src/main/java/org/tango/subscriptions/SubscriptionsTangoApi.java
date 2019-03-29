package org.tango.subscriptions;

import org.tango.rest.EntryPoint;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.LinkedHashSet;
import java.util.Set;

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
}
