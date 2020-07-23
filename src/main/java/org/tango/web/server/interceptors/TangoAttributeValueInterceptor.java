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

package org.tango.web.server.interceptors;

import fr.esrf.Tango.DevFailed;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.entities.Failures;
import org.tango.web.server.binding.RequiresTangoAttribute;
import org.tango.web.server.binding.TangoAttributeValue;
import org.tango.web.server.proxy.TangoAttributeProxy;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Date;

/**
 * @author ingvord
 * @since 11/25/18
 */
@Provider
@RequiresTangoAttribute
@TangoAttributeValue
@Priority(Priorities.USER + 400)
public class TangoAttributeValueInterceptor implements ContainerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(TangoAttributeValueInterceptor.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        TangoAttributeProxy tangoAttributeProxy = ResteasyProviderFactory.getContextData(TangoAttributeProxy.class);

        try {
            Date lastModified = new Date(tangoAttributeProxy.getTimestamp());
            Response.ResponseBuilder responseBuilder = requestContext.getRequest().evaluatePreconditions(lastModified);
            if(responseBuilder != null) requestContext.abortWith(responseBuilder.build());
        } catch (DevFailed devFailed) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity(Failures.createInstance(devFailed)).build());
        }
    }
}
