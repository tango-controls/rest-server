package org.tango.web.server.filters;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.entities.Failures;
import org.tango.web.server.binding.Partitionable;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
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

    private final Cache<String, Object> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30L, TimeUnit.SECONDS)
            .build();

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String path = containerRequestContext.getUriInfo().getPath();
        Object entity = cache.getIfPresent(path);
        if(entity != null){
            logger.debug("PartitionProvider.cache hit");
            containerRequestContext.abortWith(Response.status(NOT_MODIFIED).entity(entity).build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        List<?> entity = (List<?>) responseContext.getEntity();

        final int size = entity.size();

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


        List<Object> partition = new ArrayList<>(entity.subList(range.start, range.limit));

        responseContext.setStatus(206);
        responseContext.getHeaders().add("Content-Range",
                String.format("items %d-%d/%d",range.start,range.end,size));

        responseContext.setEntity(partition);


        cache.put(requestContext.getUriInfo().getPath(), entity);
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
