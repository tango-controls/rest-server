package org.tango.web.server.providers;

import fr.esrf.Tango.DevFailed;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.TangoRestServer;
import org.tango.rest.rc4.entities.Failures;
import org.tango.web.server.binding.RequiresTangoAttribute;
import org.tango.web.server.proxy.Proxies;
import org.tango.web.server.proxy.TangoAttributeProxy;
import org.tango.web.server.proxy.TangoDeviceProxy;

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
    private final TangoRestServer tangoRestServer;

    public TangoAttributeProxyProvider(TangoRestServer tangoRestServer) {
        this.tangoRestServer = tangoRestServer;
    }

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


        TangoAttributeProxy proxy = tangoRestServer.getContext().attributes.getUnchecked(deviceProxy.getFullName() + "/" + name).orElseGet(() -> {
            try {
                return Proxies.newTangoAttributeProxy(deviceProxy.getFullName() + "/" + name);
            } catch (DevFailed devFailed) {
                Response.Status status = Response.Status.BAD_REQUEST;
                if(devFailed.errors.length >= 1 && devFailed.errors[0].reason.equalsIgnoreCase("API_AttrNotFound"))
                    status = Response.Status.NOT_FOUND;
                containerRequestContext.abortWith(Response.status(status).entity(Failures.createInstance(devFailed)).build());
                return null;
            }
        }) ;
        ResteasyProviderFactory.pushContext(TangoAttributeProxy.class, proxy);
    }
}
