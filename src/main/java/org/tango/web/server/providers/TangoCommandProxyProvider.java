package org.tango.web.server.providers;

import fr.esrf.Tango.DevFailed;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.rest.entities.Failures;
import org.tango.web.server.binding.RequiresTangoCommand;
import org.tango.web.server.binding.RequiresTangoSelector;
import org.tango.web.server.proxy.*;

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
@RequiresTangoCommand
@Priority(Priorities.USER + 300)
public class TangoCommandProxyProvider implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        UriInfo uriInfo = containerRequestContext.getUriInfo();

        String name = uriInfo.getPathParameters().getFirst("cmd");
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
            TangoCommand tangoCommand = new TangoCommand(deviceProxy.toUriBuilder().build().toString(), name);
            TangoCommandProxy proxy = new TangoCommandProxyImpl(tangoCommand);
            ResteasyProviderFactory.pushContext(TangoCommandProxy.class, proxy);
        } catch (DevFailed devFailed) {
            containerRequestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity(Failures.createInstance(devFailed)).build());
        }
    }
}
