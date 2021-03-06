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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.12.2015
 */
@Provider
public class TangoRestFilterProvider implements ContainerResponseFilter {

    public static final String FILTER = "filter";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if(!requestContext.getUriInfo().getQueryParameters().containsKey(FILTER)) return;

        List<String> filter = requestContext.getUriInfo().getQueryParameters().get(FILTER);

        boolean inverse = filter.get(0).startsWith("!");
        ResteasyProviderFactory.pushContext(JsonFieldFilter.class, new JsonFieldFilter(inverse, inverse ? Lists.transform(filter, new Function<String, String>() {
            @Override
            public String apply(String input) {
                return input.substring(1);
            }
        }): filter));
    }


    public static class JsonFieldFilter {
        public boolean inverse;
        public Set<String> fieldNames;

        public JsonFieldFilter(boolean inverse, List<String> fieldNames) {
            this.inverse = inverse;
            this.fieldNames = new HashSet<>(fieldNames);
        }
    }
}
