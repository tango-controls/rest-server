package org.tango.web.server.providers;

import org.codehaus.jackson.map.ObjectMapper;
import org.tango.rest.entities.Subscription;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.ws.rs.sse.Sse;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/8/18
 */
@Provider
public class EventsManagerProvider implements ContainerRequestFilter {
    @Context
    private Sse sse;

    @Context
    private Providers providers;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ObjectMapper objectMapper = providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE)
                .getContext(Subscription.Event[].class);
        System.out.println("blah");
    }
}
