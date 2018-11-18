package org.tango.web.server.providers;

import fr.esrf.Tango.DevFailed;
import fr.soleil.tango.clientapi.TangoAttribute;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.rest.entities.Failures;
import org.tango.web.server.binding.RequiresTangoAttribute;
import org.tango.web.server.proxy.TangoAttributeProxy;
import org.tango.web.server.proxy.TangoAttributeProxyImpl;
import org.tango.web.server.proxy.TangoDeviceProxy;
import org.tango.web.server.proxy.TangoDeviceProxyImpl;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Objects;

/**
 * @author ingvord
 * @since 11/18/18
 */
@Provider
@RequiresTangoAttribute
@Priority(Priorities.USER + 300)
public class TangoAttributeProxyProvider implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        UriInfo uriInfo = containerRequestContext.getUriInfo();

        String name = uriInfo.getPathParameters().getFirst("attr");
        if(Objects.isNull(name)) {
            containerRequestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity(Failures.createInstance("attribute name is null")).build());
            throw new AssertionError();
        }

        TangoDeviceProxy deviceProxy = ResteasyProviderFactory.getContextData(TangoDeviceProxy.class);
        if(Objects.isNull(deviceProxy)) {
            containerRequestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity(Failures.createInstance("deviceProxy is null")).build());
            throw new AssertionError();
        }

        try {
            TangoAttribute tangoAttribute = new TangoAttribute(deviceProxy.toUriBuilder().path(name).build().toString());
            TangoAttributeProxy proxy = new TangoAttributeProxyImpl(tangoAttribute);
            ResteasyProviderFactory.pushContext(TangoAttributeProxy.class, proxy);
        } catch (DevFailed devFailed) {
            containerRequestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity(Failures.createInstance(devFailed)).build());
        }
    }
}
