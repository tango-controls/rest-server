/*
 * Copyright 2019 Tango Controls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tango.web.server.providers;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.CommunicationTimeout;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(TangoAttributeProxyProvider.class);

    public TangoAttributeProxyProvider(TangoRestServer tangoRestServer) {
        this.tangoRestServer = tangoRestServer;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        logger.trace("TangoAttributeProxyProvider");
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
                if (CommunicationTimeout.class.isAssignableFrom(devFailed.getClass()))
                    status = Response.Status.GATEWAY_TIMEOUT;
                if (devFailed.errors.length >= 1 && devFailed.errors[0].reason.equalsIgnoreCase("API_AttrNotFound"))
                    status = Response.Status.NOT_FOUND;
                containerRequestContext.abortWith(Response.status(status).entity(Failures.createInstance(devFailed)).build());
                return null;
            }
        }) ;
        ResteasyProviderFactory.pushContext(TangoAttributeProxy.class, proxy);
    }
}
