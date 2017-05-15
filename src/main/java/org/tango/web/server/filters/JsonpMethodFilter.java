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
