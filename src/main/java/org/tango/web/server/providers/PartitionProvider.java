package org.tango.web.server.providers;

import org.tango.web.server.Responses;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.12.2015
 */
@Provider
public class PartitionProvider implements ContainerResponseFilter {

    public static final String RANGE = "range";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final UriInfo uriInfo = requestContext.getUriInfo();
        if (!List.class.isAssignableFrom(responseContext.getEntity().getClass())
                || !uriInfo.getQueryParameters().containsKey(RANGE)) {
            return;
        }

        String[] split = uriInfo.getQueryParameters().getFirst(RANGE).split("-");
        final int start = Integer.parseInt(split[0]);
        final int end =  Integer.parseInt(split[1]);
        final int range = end - start;

        List<?> entity = (List<?>) responseContext.getEntity();

        final int size = entity.size();
        final int _limit = (end < size) ? end : size;


        List<Object> partition = new ArrayList<>(entity.subList(start, _limit));

        partition.add(new Object(){
            public String name = "partial content";
            public int total = size;
            public int offset = start;
            public int limit = _limit;
            public Object _links = new Object(){
                public String _prev =  (start > 0) ? uriInfo.getPath() + "?range=" + Math.max(0,start - range)+ "-" + start : null;
                public String _next = (end < size) ? uriInfo.getPath() + "?range=" + end + "-" + Math.min(end + range, size) : null;
                public String _first = uriInfo.getPath() + "?range=0-" + range;
                public String _last = uriInfo.getPath() + "?range=" + range*(int)Math.ceil(size/range) + "-" + (range*(int)Math.ceil(size/range) + range);
            };
        });

        responseContext.setStatus(206);
        responseContext.setEntity(partition);
    }
}
