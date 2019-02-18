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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.rc4.Rc4ApiImpl;
import org.tango.rest.rc4.entities.Failures;
import org.tango.web.server.binding.Partitionable;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static javax.ws.rs.core.Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.12.2015
 */
@Partitionable
@Provider
public class PartitionProvider implements
        ContainerRequestFilter,
        ContainerResponseFilter {
    public static final String RANGE = "range";
    private final Logger logger = LoggerFactory.getLogger(PartitionProvider.class);

    private final Cache<URI, Object> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30L, TimeUnit.SECONDS)
            .build();

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        URI path = getRequestURI(containerRequestContext);
        Object entity = cache.getIfPresent(path);
        if(entity != null){
            logger.debug("PartitionProvider.cache hit");
            containerRequestContext.abortWith(Response.ok().entity(entity).build());
        }
    }

    private URI getRequestURI(ContainerRequestContext containerRequestContext) {
        return containerRequestContext.getUriInfo().getRequestUriBuilder().replaceQueryParam(RANGE,null).build();
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Object entity = responseContext.getEntity();
        if(entity == null || !List.class.isAssignableFrom(entity.getClass())) return;


        List<?> entityAsList = (List<?>) entity;
        final int size = entityAsList.size();

        responseContext.getHeaders().add("Accept-Ranges","items");


        String queryParam = requestContext.getUriInfo().getQueryParameters().getFirst(RANGE);
        if (queryParam == null) {
            responseContext.getHeaders().add("X-size",size);
            return;
        }


        PartitionRange range = null;
        try {
            range = PartitionRange.fromString(queryParam, size);
        } catch (IllegalArgumentException e) {
            responseContext.setStatus(REQUESTED_RANGE_NOT_SATISFIABLE.getStatusCode());
            responseContext.setEntity(Failures.createInstance(e));
            return;
        }


        List<Object> partition = new ArrayList<>(entityAsList.subList(range.start, range.limit));


        UriInfo uriInfo = requestContext.getUriInfo();

        if(uriInfo.getPathSegments().get(1).getPath().equalsIgnoreCase(Rc4ApiImpl.RC_4)) {
            PartitionRange finalRange = range;
            partition.add(new Object(){
                public String name = "partial_content";
                public int total = size;
                public int offset = finalRange.start;
                public int limit = finalRange.limit;
                public Object _links = new Object(){
                    public String _prev =  (finalRange.start > 0) ? uriInfo.getAbsolutePath() + "?range=" + Math.max(0, finalRange.start - finalRange.range)+ "-" + finalRange.start : null;
                    public String _next = (finalRange.end < size) ? uriInfo.getAbsolutePath() + "?range=" + finalRange.end + "-" + Math.min(finalRange.end + finalRange.range, size) : null;
                    public String _first = uriInfo.getAbsolutePath() + "?range=0-" + finalRange.range;
                    public String _last = uriInfo.getAbsolutePath() + "?range=" + finalRange.range*(int)Math.ceil(size/ finalRange.range) + "-" + (finalRange.range*(int)Math.ceil(size/ finalRange.range) + finalRange.range);
                };
            });
        }


        responseContext.setStatus(206);
        responseContext.getHeaders().add("Content-Range",
                String.format("items %d-%d/%d",range.start,range.end,size));

        responseContext.setEntity(partition);


        cache.put(getRequestURI(requestContext), entity);
        logger.debug("Done partitioning.");
    }

    public static class PartitionRange {
        int start;
        int end;
        int range;
        int size;
        int limit;

        public PartitionRange(int start, int end, int range, int size, int limit) {
            this.start = start;
            this.end = end;
            this.range = range;
            this.size = size;
            this.limit = limit;
        }

        static PartitionRange fromString(String queryParam, int size){
            String[] tmp = queryParam.split("-");
            int start = Integer.parseInt(tmp[0]);
            if(start < 0)
                throw new IllegalArgumentException("start of the range can not be less than 0");
            int end = Integer.parseInt(tmp[1]);
            if(end > size)
                throw new IllegalArgumentException("end of the range can not be greater than total size=" + size);
            if(start >= end)
                throw new IllegalArgumentException("start of the range can not be greater or equal to its end");
            return new PartitionRange(start,end,end-start,size, (end < size) ? end : size);

        }
    }
}
