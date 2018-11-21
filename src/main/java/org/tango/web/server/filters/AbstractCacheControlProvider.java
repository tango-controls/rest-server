package org.tango.web.server.filters;

import org.tango.TangoRestServer;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.12.2015
 */
public abstract class AbstractCacheControlProvider implements ContainerResponseFilter {
    private SimpleDateFormat dateFormat;

    protected final TangoRestServer tangoRestServer;

    public AbstractCacheControlProvider(TangoRestServer tangoRestServer) {
        dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.tangoRestServer = tangoRestServer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if(responseContext.getStatus() >= 400) return;

        if (!tangoRestServer.isCacheEnabled()) return;


        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.putSingle("Expires",
                dateFormat.format(new Date(System.currentTimeMillis()/*TODO last modified*/ + getDelay())));

        CacheControl cc = new CacheControl();
        cc.setPrivate(false);
        cc.setMaxAge((int) TimeUnit.SECONDS.convert(getDelay(), TimeUnit.MILLISECONDS));
        cc.getCacheExtension().put("max-age-millis", String.valueOf(getDelay()));

        headers.put("Cache-Control", Collections.singletonList(cc));
    }

    protected abstract long getDelay();
}
