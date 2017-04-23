package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.web.server.TangoContext;

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
public abstract class AbstractCacheProvider implements ContainerResponseFilter {
    public static final SimpleDateFormat DATE_FORMAT;
    static {
        Calendar calendar = Calendar.getInstance();
        DATE_FORMAT = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if(responseContext.getStatus() != 200) return;

        TangoContext context = ResteasyProviderFactory.getContextData(TangoContext.class);
        if(!context.isCacheEnabled) return;


        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.putSingle("Expires",
                DATE_FORMAT.format(new Date(System.currentTimeMillis()/*TODO last modified*/ + getDelay(context))));

        CacheControl cc = new CacheControl();
        cc.setPrivate(false);
        cc.setMaxAge((int) TimeUnit.SECONDS.convert(getDelay(context), TimeUnit.MILLISECONDS));
        cc.getCacheExtension().put("max-age-millis", String.valueOf(getDelay(context)));

        headers.put("Cache-Control", Arrays.<Object>asList(cc));
    }

    protected abstract long getDelay(TangoContext context);
}
