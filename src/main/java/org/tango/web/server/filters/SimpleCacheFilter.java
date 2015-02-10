package org.tango.web.server.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Caches client GET request results
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 09.02.2015
 */
public class SimpleCacheFilter implements Filter {
    public static final long DELAY = 200L;//TODO parameter

    private final ConcurrentMap<String, Long> timeCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, byte[]> cache = new ConcurrentHashMap<>();

    public void destroy() {
    }

    //TODO race conditions
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        if (httpReq.getMethod().equals("GET")) {
            long timestamp = System.currentTimeMillis();
            String URI = httpReq.getRequestURI();
            Long oldTimestamp = timeCache.putIfAbsent(URI, timestamp);
            if (oldTimestamp == null || timestamp - oldTimestamp > DELAY) {
                CachedResponseWrapper wrapper = new CachedResponseWrapper(httpResp);
                chain.doFilter(req, wrapper);

                timeCache.put(URI,timestamp);
                cache.put(URI, wrapper.cache.toByteArray());
                returnCachedValue(URI, resp);
            } else {
                returnCachedValue(URI, resp);
            }
        } else {
            chain.doFilter(req, resp);
        }
    }

    private void returnCachedValue(String URI, ServletResponse resp) throws IOException {
        try (ServletOutputStream outputStream = resp.getOutputStream()) {
            outputStream.write(cache.get(URI));
        }
    }

    public void init(FilterConfig config) throws ServletException {

    }

    private static class CachedResponseWrapper extends HttpServletResponseWrapper {
        private final PrintWriter writer;
        private final ByteArrayOutputStream cache;
        private final ServletOutputStream outputStream;

        /**
         * Creates a ServletResponse adaptor wrapping the given response object.
         *
         * @param response
         * @throws IllegalArgumentException if the response is null.
         */
        public CachedResponseWrapper(HttpServletResponse response) {
            super(response);
            cache = new ByteArrayOutputStream(/*super.getBufferSize()*/);
            writer = new PrintWriter(cache);
            outputStream = new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    cache.write(b);
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
}
