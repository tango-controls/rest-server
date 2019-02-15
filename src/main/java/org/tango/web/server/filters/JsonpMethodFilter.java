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

package org.tango.web.server.filters;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 09.02.2015
 */
@Provider
@PreMatching
public class JsonpMethodFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final List<String> ALLOWED_METHODS = Arrays.asList(
            "get", "put", "post", "delete"
    );

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = String.valueOf(
                requestContext.getUriInfo().getQueryParameters().getFirst("_method"));

        if (method != null && ALLOWED_METHODS.contains(method)) {
            requestContext.setMethod(method.toUpperCase());
            requestContext.setProperty("org.tango.web.server.jsonp", new Object());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (requestContext.getProperty("org.tango.web.server.jsonp") != null) {
            responseContext.getHeaders().add("Content-Type", "application/javascript");
        }
    }
}
