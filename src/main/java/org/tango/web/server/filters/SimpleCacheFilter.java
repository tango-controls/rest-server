package org.tango.web.server.filters;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.web.server.TangoContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Caches client GET request results
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 09.02.2015
 */
public class SimpleCacheFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(SimpleCacheFilter.class);

    public static final int CAPACITY = 1000;//TODO parameter

    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentLinkedHashMap.Builder<String, CacheEntry>()
            .maximumWeightedCapacity(CAPACITY)
            .build();

    public void destroy() {
    }

    //TODO race conditions
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        if(!tangoContext.isCacheEnabled) {
            logger.debug("Skipping cache due to cache is not enabled.");
            chain.doFilter(req, resp);
            return;
        }

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        String URI = httpReq.getRequestURI();
        long timestamp = System.currentTimeMillis();

        if (httpReq.getMethod().equals("GET") && !URI.contains("=")) {
            CacheEntry cacheEntry = cache.get(URI);
            if (cacheEntry == null || timestamp - cacheEntry.timestamp > tangoContext.serverSideCacheExpirationDelay) {
                logger.debug("Cache miss!");
                CachedResponseWrapper wrapper = new CachedResponseWrapper(httpResp);
                chain.doFilter(req, wrapper);

                cache.put(URI, cacheEntry = new CacheEntry(timestamp, wrapper.cached.toByteArray()));
                returnCachedValue(cacheEntry, resp);
            } else {
                logger.debug("Cache hit!");
                returnCachedValue(cacheEntry, resp);
            }
        } else {
            chain.doFilter(req, resp);
        }
    }

    private void returnCachedValue(CacheEntry cacheEntry, ServletResponse resp) throws IOException {
        ServletOutputStream outputStream = resp.getOutputStream();
        outputStream.write(cacheEntry.value);//byte[]
    }

    private TangoContext tangoContext;

    public void init(FilterConfig config) throws ServletException {
        tangoContext = (TangoContext) config.getServletContext().getAttribute(TangoContext.TANGO_CONTEXT);
    }

    private static class CachedResponseWrapper extends HttpServletResponseWrapper {
        private final PrintWriter writer;
        private final ByteArrayOutputStream cached;
        private final ServletOutputStream outputStream;

        /**
         * Creates a ServletResponse adaptor wrapping the given response object.
         *
         * @param response
         * @throws IllegalArgumentException if the response is null.
         */
        public CachedResponseWrapper(HttpServletResponse response) {
            super(response);
            cached = new ByteArrayOutputStream(/*super.getBufferSize()*/);
            writer = new PrintWriter(cached);
            outputStream = new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    cached.write(b);
                }
            };
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return writer;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return outputStream;
        }
    }

    private static class CacheEntry{
        private final byte[] value;
        private final long timestamp;

        private CacheEntry(long timestamp, byte[] value) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
