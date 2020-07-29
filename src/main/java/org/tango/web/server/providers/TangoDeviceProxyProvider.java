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

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.entities.Failures;
import org.tango.web.server.TangoProxiesCache;
import org.tango.web.server.proxy.Proxies;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.proxy.TangoDeviceProxy;
import org.tango.web.server.proxy.TangoDeviceProxyImpl;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Provides {@link TangoDeviceProxyImpl} extracted from URL e.g.
 *
 * <pre>.../sys/tg_test/1/...</pre> becomes a Java object of type {@link TangoDeviceProxyImpl}
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.12.2015
 */
@Provider
@Priority(Priorities.USER + 200)
public class TangoDeviceProxyProvider implements ContainerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(TangoDeviceProxyProvider.class);

    private final ThreadLocal<TangoProxiesCache> context;

    public TangoDeviceProxyProvider(ThreadLocal<TangoProxiesCache> context) {
        this.context = context;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.trace("TangoDeviceProxyProvider");
        TangoDatabaseProxy db = ResteasyProviderFactory.getContextData(TangoDatabaseProxy.class);
        if (db == null) return;

        UriInfo uriInfo = requestContext.getUriInfo();
        MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
        String domain = pathParams.getFirst("domain");
        String family = pathParams.getFirst("family");
        String member = pathParams.getFirst("member");

        if(domain == null || family == null || member == null)
            return;

        TangoDeviceName name = new TangoDeviceName(db.getTangoHost(), domain, family, member);

        TangoDeviceProxy result = context.get().devices.getUnchecked(name.toString()).orElseGet(() -> {
            try {
                return Proxies.newTangoDeviceProxy(name.host, name.name);
            } catch (TangoProxyException e) {
                Response.Status status = Response.Status.BAD_REQUEST;
                if (e.reason.contains("DB_DeviceNotDefined"))
                    status = Response.Status.NOT_FOUND;
                requestContext.abortWith(Response.status(status).entity(Failures.createInstance(e)).build());
                return null;
            }
        });

        if (Objects.nonNull(result)) {
            ResteasyProviderFactory.pushContext(TangoDeviceProxy.class, result);
        }
    }

    private static class TangoDeviceName {
        String host;
        String name;

        public TangoDeviceName(String host, String domain, String family, String member) {
            this.name = new StringJoiner("/").add(domain).add(family).add(member).toString();
            this.host = host;
        }

        public String toString(){
            return "tango://" + host + "/" + name;
        }
    }
}
