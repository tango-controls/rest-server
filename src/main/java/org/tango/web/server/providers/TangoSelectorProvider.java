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
import org.tango.rest.entities.Failures;
import org.tango.web.server.TangoProxiesCache;
import org.tango.web.server.binding.RequiresTangoSelector;
import org.tango.web.server.util.TangoSelector;
import org.tango.web.server.util.Wildcard;
import org.tango.web.server.util.WildcardExtractor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
@Provider
@RequiresTangoSelector
public class TangoSelectorProvider implements ContainerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(TangoSelectorProvider.class);
    public static final String WILDCARD = "wildcard";
    private final ThreadLocal<TangoProxiesCache> context;

    public TangoSelectorProvider(ThreadLocal<TangoProxiesCache> context) {
        this.context = context;
    }


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.trace("TangoSelectorProvider");
        UriInfo uriInfo = requestContext.getUriInfo();

        List<String> queryWildcards = uriInfo.getQueryParameters().get(WILDCARD);

        List<Wildcard> wildcards = new WildcardExtractor().extractWildcards(queryWildcards);

        if(wildcards.isEmpty()) {
            requestContext.abortWith(
                    Response.status(Response.Status.BAD_REQUEST).entity(Failures.createInstance("wildcard parameter is missing!")).build());
            return;
        }

        ResteasyProviderFactory.pushContext(TangoSelector.class, new TangoSelector(wildcards, context.get()));

    }


}
