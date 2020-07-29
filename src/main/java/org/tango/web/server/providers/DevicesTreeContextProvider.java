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

import com.google.common.collect.Lists;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.web.server.binding.RequiresDeviceTreeContext;
import org.tango.web.server.proxy.Proxies;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.tree.DeviceFilters;
import org.tango.web.server.tree.DevicesTreeContext;
import org.tango.web.server.tree.DevicesTreeContextImpl;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/14/18
 */
@Provider
@Priority(Priorities.USER + 200)
@RequiresDeviceTreeContext
public class DevicesTreeContextProvider implements ContainerRequestFilter {

    public static final String WILDCARD = "wildcard";
    public static final String HOST = "host";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        UriInfo uriInfo = requestContext.getUriInfo();

        List<TangoDatabaseProxy> dbs = getDatabases(uriInfo);

        if(dbs.isEmpty()){
            requestContext.abortWith(
                    Response.ok(Collections.EMPTY_LIST, MediaType.APPLICATION_JSON).build());
            return;
        }


        List<String> filters = uriInfo.getQueryParameters(true).get(WILDCARD);

        DeviceFilters df = new DeviceFilters(filters);

        DevicesTreeContext context = new DevicesTreeContextImpl(dbs, df);


        ResteasyProviderFactory.pushContext(DevicesTreeContext.class, context);
    }

    private List<TangoDatabaseProxy> getDatabases(UriInfo uriInfo) {
        List<PathSegment> segments = uriInfo.getPathSegments();

        if(segments.get(3).getPath().equalsIgnoreCase("tree")) {
            List<String> tango_hosts = uriInfo.getQueryParameters(true).get(HOST);
            if(tango_hosts == null) return Collections.emptyList();
            return tango_hosts.stream()
                    .filter(this::checkURISyntax)
                    .map(Proxies::getDatabaseProxy)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } else {
            Optional<TangoDatabaseProxy> contextData = Optional.ofNullable(ResteasyProviderFactory.getContextData(TangoDatabaseProxy.class));
            return contextData.<List<TangoDatabaseProxy>>map(Lists::newArrayList).orElse(Collections.emptyList());
        }
    }

    private boolean checkURISyntax(String next) {
        try {
            new URI("tango://" + next);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
