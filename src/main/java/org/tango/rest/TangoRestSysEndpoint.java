package org.tango.rest;

import org.tango.web.server.TangoRestContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.07.2020
 */
@Path("/sys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TangoRestSysEndpoint {

    private final TangoRestContext context;

    public TangoRestSysEndpoint(TangoRestContext context) {
        this.context = context;
    }

    @GET
    public TangoRestContext get() {
        return context;
    }

    @PUT
    public void setCacheEnabled(Map<String, Object> v) {
        context.cacheEnabled = (boolean) v.getOrDefault("cacheEnabled", context.cacheEnabled);
        context.capacity.set((int) v.getOrDefault("capacity", context.capacity.get()));
        context.dynamicValueExpirationDelay.set((long) v.getOrDefault("dynamicValueExpirationDelay", context.dynamicValueExpirationDelay.get()));
        context.staticValueExpirationDelay.set((long) v.getOrDefault("staticValueExpirationDelay", context.staticValueExpirationDelay.get()));
    }
}
