package org.tango.web.server.filters;

import org.tango.web.rest.Response;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Caches client GET request results
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 09.02.2015
 */
public class LruCacheFilter implements Filter {
    public static final long DELAY = 1000L;//TODO parameter
    private final ConcurrentMap<String, Long> timeCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        if(httpReq.getMethod().equals("GET")) {
            long timestamp = System.currentTimeMillis();
            String URI = httpReq.getRequestURI();
            long oldTimestamp = timeCache.put(URI, timestamp);
            if(timestamp - oldTimestamp < DELAY)
                chain.doFilter(req, resp);//TODO return caches value
            else
                chain.doFilter(req, resp);//TODO cache new value
        } else {
            chain.doFilter(req, resp);
        }
    }

    public void init(FilterConfig config) throws ServletException {

    }


}
