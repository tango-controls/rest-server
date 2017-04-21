package org.tango.web.server.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.web.server.TangoContext;
import org.tango.web.server.cache.CachedEntity;
import org.tango.web.server.cache.SimpleBinaryCache;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Caches client GET request results
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 09.02.2015
 */
@Provider
@PreMatching
public class SimpleCacheFilter implements ContainerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(SimpleCacheFilter.class);

    private TangoContext tangoContext;
    private SimpleBinaryCache cache;

    public SimpleCacheFilter(TangoContext tangoContext, SimpleBinaryCache cache) {
        this.tangoContext = tangoContext;
        this.cache = cache;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!tangoContext.isCacheEnabled) {
            logger.debug("cache is disabled. Skipping...");
            return;
        }
        String method = requestContext.getMethod();
        if (!method.equals("GET")) {
            logger.debug("ignoring method {} for caching.", method);
        }

        String uri = requestContext.getUriInfo().getAbsolutePath().toString();

        CachedEntity cacheEntry = cache.get(uri);
        long timestamp = System.currentTimeMillis();
        if (cacheEntry == null || timestamp - cacheEntry.timestamp > tangoContext.serverSideCacheExpirationDelay) {
            logger.debug("Cache miss!");
        } else {
            logger.debug("Cache hit!");
            requestContext.abortWith(Response.ok(cacheEntry.value).build());
        }
    }
}
